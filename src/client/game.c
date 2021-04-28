#include <errno.h>
#include <stdio.h>
#include <stdlib.h>
#include <stdint.h>
#include <assert.h>
#include <stdbool.h>
#include <inttypes.h>

#include <glob.h>
#include <poll.h>
#include <fcntl.h>
#include <netdb.h>
#include <unistd.h>
#include <getopt.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <sys/timerfd.h>

#include <X11/Xlib.h>

#include <SDL2/SDL.h>
#include <SDL2/SDL_image.h>
#include <SDL2/SDL_syswm.h>

#include <json-c/json_object.h>
#include <json-c/json_tokener.h>

#include "util.h"
#include "constants.h"
#include "donceykongjr.h"

/**
 * @brief Describe el estado del cliente. Los valores
 * puestos saquí son los iniciales.
 */
struct game game =
{
	.state       = GAME_STATE_HANDSHAKE_WHOAMI,
	.flags       = GAME_FLAG_ZERO,
	.net_fd      = FD_INVALID,
	.x11_fd      = FD_INVALID,
	.timer_fd    = FD_INVALID,
	.net_file    = NULL,
	.window      = NULL,
	.renderer    = NULL,
	.font        = NULL,
	.ticks       = 0,
	.stats_label = { .texture = NULL, .surface = NULL },
	.max_depth   = 0
};

/**
 * @brief Mueve una entidad, dada la cantidad correcta de ticks transcurridos 
 * 
 * Dada una coordenada y la velocidad(cantidad movimiento/ticks) en esa 
 * coordenada de una entidad, decide si dicha entidad debe moverse o no. 
 * De ser el número de ticks factor del denominador de la velocidad, mueve
 * la entidad la cantidad unidades de longitud específicadas por el 
 * numerador de la velocidad. Retorna un valor booleano que indica si 
 * hubo movimiento o no
 * 
 * @param coordinate Puntero a un campo de eje coordenado de una entidad (x o y)
 * @param speed Fracción de velocidad expresada como (cantidad movimiento/ticks)
 * @return true La entidad se movió en este tick
 * @return false La entidad no se movió en este tick
 */
bool move_on_tick(int *coordinate, const struct ratio *speed)
{
	// El numerador es una distancia y el denominador un tiempo en ticks
	if(speed->denominator > 0 && game.ticks % speed->denominator == 0)
	{
		// Cada speed->denominator ticks se agrega el numerador
		int jump = abs(speed->numerator);
		jump = jump > 0 ? jump : JUMP_DEFAULT; // Caso 0/n
		jump = speed->numerator > 0 ? jump : -jump;

		*coordinate += jump;
		return true;
	}

	return false;
}

/**
 * @brief Actualiza la etiqueta de estadísticas en pantalla.
 *
 * @param lives cantidad de vidas restantes
 * @param score puntaje
 */
void update_stats(int lives, int score)
{
	// Se libera el label viejo
	if(game.stats_label.texture)
	{
		SDL_DestroyTexture(game.stats_label.texture);
		SDL_FreeSurface(game.stats_label.surface);
	}

	// Se genera el texto del label y se renderiza
	char text[STATS_LABEL_MAX_CHARS];
	snprintf(text, sizeof text, STATS_LABEL_FORMAT, lives, score);

	SDL_Color fg = STATS_LABEL_COLOR;
	SDL_Color bg = { .r = COLOR_BLACK, .g = COLOR_BLACK, .b = COLOR_BLACK, .a = COLOR_BLACK };
	if(!(game.stats_label.surface = TTF_RenderUTF8_Shaded(game.font, text, fg, bg)))
	{
		sdl_ttf_fatal();
	} else if(!(game.stats_label.texture = SDL_CreateTextureFromSurface(game.renderer, game.stats_label.surface)))
	{
		sdl_fatal();
	}
}

/**
 * @brief Reponsable de manejar los eventos de presión de teclas  
 * 
 * Dado un evento de presión de tecla, de ser dicha tecla parte de los controles de juego, envía al servidor
 * un mensaje que comunica el tipo de evento y la tecla que lo causó, de forma que el servidor pueda resolver
 * las instrucciones a emitir basado en dicho evento
 * @param event Puntero a la estructura que describe el evento de presión de teclado
 */
void handle_key(const SDL_KeyboardEvent *event)
{
	// Solo se maneja el momento de la presión, no repeticiones
	if(event->repeat > 0)
	{
		return;
	}

	const char *operation = event->state == SDL_PRESSED ? CMD_PRESS : CMD_RELEASE;
	const char *key = CMD_UNKNOWN;

	switch(event->keysym.sym)
	{
		case SDLK_LEFT:
		case SDLK_a:
			key = KEY_LEFT;
			break;

		case SDLK_RIGHT:
		case SDLK_d:
			key = KEY_RIGHT;
			break;

		case SDLK_UP:
		case SDLK_w:
			key = KEY_UP;
			break;

		case SDLK_DOWN:
		case SDLK_s:
			key = KEY_DOWN;
			break;

		case SDLK_SPACE:
			key = KEY_JUMP;
			break;

		default:
			return;
	}

	struct key_value items[] =
	{
		{CMD_OP,  json_object_new_string(operation)},
		{CMD_KEY, json_object_new_string(key)},
		{NULL,    NULL}
	};

	transmit(items);
}

/**
 * @brief Reacciona a un click (enumeración de IDs).
 *
 * Entradas: evento de mouse.
 */
void handle_click(const SDL_MouseButtonEvent *event)
{
	// Solo se aceptan clicks izquierdos
	if(event->button != SDL_BUTTON_LEFT)
	{
		return;
	}

	// En fullscreen cambia la escala
	float scale_x;
	float scale_y;
	SDL_RenderGetScale(game.renderer, &scale_x, &scale_y);

	// Se ajusta según escala
	int click_x = event->x / scale_x;
	int click_y = event->y / scale_y;

	bool found = false;
	for(struct hash_map_iter iter = hash_map_iter(&game.entities); iter.cell; hash_map_iter_next(&iter))
	{
		int id = hash_map_iter_key(&iter);
		struct entity *entity = hash_map_iter_value(&iter);
		struct sprite *sprite = hash_map_get(&game.sprites, *(int*)vec_get(&entity->sequence, entity->next_sprite));

		if(click_x < entity->x || click_x >= entity->x + sprite->surface->w
		|| click_y < entity->y || click_y >= entity->y + sprite->surface->h)
		{
			// Fuera del área de la entidad
			continue;
		}

		if(!found)
		{
			found = true;
			printf("Click at (%d, %d) matches these IDs: %d", click_x, click_y, id);
		} else
		{
			printf(", %d", id);
		}
	}

	found ? putchar(NEWLINE) : printf("No IDs match click at (%d, %d)\n", click_x, click_y);
}

/**
 * @brief Pregunta al usuario por un id de juego para inicializar el cliente
 * 
 * Dado un id de cliente y una serie de id's que identifican los juegos activos, se le pregunta
 * al usuario por el id de juego al que quiere unirse. Si el usuario escribe un id registrado 
 * como juego, el usuario se une a una partida ya activa como espectador. Si coloca su propio id,
 * trata de iniciar un juego como jugador. Dada una entrada inválida, levantará un error y cerrará
 * el juego
 * @param client_id Id asignado al cliente actual 
 * @param games Objeto JSON que contiene la lista de juegos activos
 * @return int32_t Id del juego asociado al cliente
 */
static int32_t select_game(int32_t client_id, struct json_object *games)
{
	for(size_t i = 0; i < json_object_array_length(games); ++i)
	{
		struct json_object *game = json_object_array_get_idx(games, i);
		if(json_object_get_type(game) != json_type_int)
		{
			fprintf(stderr, "Error: 'games[%zu]' is not an integer\n", i);
			quit(EXIT_FAILURE);
		}

		printf("- Game %d is running\n", json_object_get_int(game));
	}

	putchar(NEWLINE);

	int32_t game_id;
	while(true)
	{
		printf("Enter a game ID to watch, or %d to start a new game\n> ", client_id);

		// SCNd32 se requiere por ser int32_t
		int scanned = scanf(" %" SCNd32, &game_id);
		if(scanned == EOF)
		{
			bye(); //no se quiere empezar un juego, se cierra sin error
			quit(EXIT_SUCCESS);
		}

		// Se lee y descarta el resto de la línea de entrada
		while(getchar() != NEWLINE)
		{
			continue;
		}

		bool valid = scanned == 1 && game_id == client_id;
		if(scanned == 1 && !valid)
		{
			for(size_t i = 0; i < json_object_array_length(games); ++i) //
			{
				if(json_object_get_int(json_object_array_get_idx(games, i)) == game_id)
				{
					game.flags |= GAME_FLAG_SPECTATOR;
					puts("This client is a spectator");

					valid = true;
					break;
				}
			}
		}

		if(valid)
		{
			break;
		}

		fputs("Error: bad input\n", stderr);
	}

	return game_id;
}

/**
 * @brief Emite el mensaje que le indica al servidor el modo de operación del cliente
 * y a qué partida quiere unirse el mismo
 *
 * Controla la rutina de inicio del cliente una vez recibido el mensaje inicial del Servidor. 
 * Dado el estado del servidor, da ciertas opciones al usuario para inicializalizar un juego.
 * @param message Mensaje inicial enviado del Servidor al cliente
 */
void start_or_watch_game(struct json_object *message)
{
	int32_t client_id = json_object_get_int(expect_key(message, CMD_WHOAMI, json_type_int, true));
	struct json_object *games = expect_key(message, CMD_GAMES, json_type_array, true);

	// PRId32 se ocupa por ser int32_t
	printf("This is client %" PRId32 "\n", client_id);

	int32_t game_id;
	if(json_object_array_length(games) == 0)
	{
		printf("No games are currently running, starting game %d...\n", client_id);
		game_id = client_id;
	} else
	{
		game_id = select_game(client_id, games);
	}

	struct key_value items[] =
	{
		{CMD_INIT, json_object_new_int(game_id)},
		{NULL,     NULL}
	};

	transmit(items);
}
