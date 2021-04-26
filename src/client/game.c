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
 * @brief Describe el estado del cliente
 *
 */
struct game game =
{
	.state      = HANDSHAKE_WHOAMI,
	.net_fd     = -1,
	.x11_fd     = -1,
	.timer_fd   = -1,
	.net_file   = NULL,
	.window     = NULL,
	.renderer   = NULL,
	.ticks      = 0,
	.fullscreen = false
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
	if(speed->denominator > 0 && game.ticks % speed->denominator == 0)
	{
		int jump = abs(speed->numerator);
		jump = jump > 0 ? jump : 1;
		jump = speed->numerator > 0 ? jump : -jump;

		*coordinate += jump;
		return true;
	}

	return false;
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
	if(event->repeat > 0)
	{
		return;
	}

	const char *operation = event->state == SDL_PRESSED ? "press" : "release";
	const char *key = "unknown";

	switch(event->keysym.sym)
	{
		case SDLK_LEFT:
		case SDLK_a:
			key = "left";
			break;

		case SDLK_RIGHT:
		case SDLK_d:
			key = "right";
			break;

		case SDLK_UP:
		case SDLK_w:
			key = "up";
			break;

		case SDLK_DOWN:
		case SDLK_s:
			key = "down";
			break;

		case SDLK_SPACE:
			key = "jump";
			break;

		default:
			return;
	}

	struct key_value items[] =
	{
		{"op",  json_object_new_string(operation)},
		{"key", json_object_new_string(key)},
		{NULL,  NULL}
	};

	transmit(items);
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
			quit(1);
		}

		printf("- Game %d is running\n", json_object_get_int(game));
	}

	puts("");

	int32_t game_id;
	while(true)
	{
		printf("Enter a game ID to watch, or %d to start a new game\n> ", client_id);

		int scanned = scanf(" %" SCNd32, &game_id);
		if(scanned == EOF && feof(stdin))
		{
			bye(); //no se quiere empezar un juego, se cierra sin error
			quit(0);
		} else if(scanned == EOF)
		{
			sys_fatal();
		}

		while(getchar() != '\n')
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
	int32_t client_id = json_object_get_int(expect_key(message, "whoami", json_type_int, true));
	struct json_object *games = expect_key(message, "games", json_type_array, true);

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
		{"init", json_object_new_int(game_id)},
		{NULL,   NULL}
	};

	transmit(items);
}
