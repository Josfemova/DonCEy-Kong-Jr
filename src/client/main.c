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

#define CLOCK_HZ       30
#define NANOS_PER_TICK (1000000000 / CLOCK_HZ)

/**
 * @brief Implementación de un array dinámico
 * 
 */
struct vec
{
	void  *data;
	size_t length;
	size_t capacity;
	size_t element_size;
};

/**
 * @brief  Implementación de hash map
 * 
 */
struct hash_map
{
	struct vec buckets;
	unsigned   order;
	size_t     value_size;
};

/**
 * @brief Estados posibles en los que se puede encontrar un jugador
 * 
 */
enum client_state
{
	HANDSHAKE_WHOAMI,
	HANDSHAKE_INIT,
	READY
};

/**
 * @brief Representa los componentes de un sprite
 * 
 */
struct sprite
{
	SDL_Surface *surface;
	SDL_Texture *texture;
};

struct ratio
{
	int      numerator;
	unsigned denominator;
};

/**
 * @brief Representa una entidad
 * 
 */
struct entity
{
	int          id;
	int          x;
	int          y;
	struct vec   sequence;
	size_t       next_sprite;
	struct ratio speed_x;
	struct ratio speed_y;
};

/**
 * @brief Describe el estado del cliente
 *
 */
static struct
{
	enum client_state state;
	int               net_fd;
	int               x11_fd;
	int               timer_fd;
	FILE             *net_file;
	SDL_Window       *window;
	SDL_Renderer     *renderer;
	size_t            ticks;
	struct hash_map   sprites;
	struct hash_map   entities;
	bool              fullscreen;
} game =
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
 * @brief Representa un par llave-valor
 * 
 */
struct key_value
{
	const char         *key;
	struct json_object *value;
};

/**
 * @brief Crea e inicializa un nuevo vector
 * 
 * @param element_size Tamaño en memoria de cada elemento
 * @return struct vec Vector inicializado que almacena vectores de tamaño element_size
 */
static struct vec vec_new(size_t element_size)
{
	struct vec empty =
	{
		.data         = NULL,
		.length       = 0,
		.capacity     = 0,
		.element_size = element_size,
	};

	return empty;
}

/**
 * @brief Elimina todos los elementos de un vector 
 * 
 * @param vec puntero al vector cuyos elementos se quieren eliminar 
 */
static void vec_clear(struct vec *vec)
{
	free(vec->data);

	vec->data = NULL;
	vec->length = vec->capacity = 0;
}

/**
 * @brief Obtiene el elemento ubicado en un índice dado de un vector
 * 
 * @param vec Puntero al vector en el que se encuentra el elemento
 * @param index Índice del elemento
 * @return void* puntero al vector del cual se quiere obtener el elemento
 */
static void *vec_get(struct vec *vec, size_t index)
{
	return (char*)vec->data + vec->element_size * index;
}

/**
 * @brief Expande la capacidad máxima de un vector
 * 
 * @param vec Vector cuya capacidad quiere aumentarse
 * @param required Capacidad a la que se requiere expandir el vector 
 */
static void vec_require_capacity(struct vec *vec, size_t required)
{
	if(required > vec->capacity)
	{
		do
		{
			vec->capacity = vec->capacity > 0 ? 2 * vec->capacity : 4;
		} while(required > vec->capacity);

		vec->data = realloc(vec->data, vec->element_size * vec->capacity);
		if(!vec->data)
		{
			perror("realloc");
			abort();
		}
	}
}

/**
 * @brief Agrega un espacio a un vector y retorna un puntero al nuevo elemento  
 * 
 * @param vec vector en el cual se quiere realizar un emplace 
 * @return void* puntero al nuevo espacio agregado al vector 
 */
static void *vec_emplace(struct vec *vec)
{
	vec_require_capacity(vec, vec->length + 1);
	return (char*)vec->data + vec->length++ * vec->element_size;
}

/**
 * @brief Elimina un elemento en el índice dado de un vector
 * 
 * @param vec vector del cual se quiere eliminar el elemento
 * @param index índice del elemento
 */
static void vec_delete(struct vec *vec, size_t index)
{
	assert(index < vec->length); //verifica que el índice sea válido

	void *target = (char*)vec->data + vec->element_size * index;
	void *source = (char*)target + vec->element_size;

	memmove(target, source, (vec->length-- - index - 1) * vec->element_size);
}

/**
 * @brief Redimensiona un vector a un nuevo tamaño dado
 * 
 * @param vec Vector a redimensionar
 * @param new_size Nuevo tamaño del vector 
 */
static void vec_resize(struct vec *vec, size_t new_size)
{
	vec_require_capacity(vec, new_size);
	if(new_size > vec->length)
	{
		memset(vec_get(vec, vec->length), 0, vec->element_size * (new_size - vec->length));
	}

	vec->length = new_size;
}

/**
 * @brief Busca por un par llave-valor en un bucket dado
 * 
 * @param bucket Puntero al bucket del hash map en que se quiere buscar el par
 * @param lookup Llave que identifica el par buscado
 * @return void* Puntero al par si el mismo es encontrado en el bucket, NULL de lo contrario
 */
static void *bucket_get_pair(struct vec *bucket, int lookup)
{
	if(bucket)
	{
		for(size_t i = 0; i < bucket->length; ++i)
		{
			char *pair = vec_get(bucket, i); //Obtiene elemento en índice i del vector bucket
			int *key = (int*)pair;

			if(lookup == *key)
			{
				return pair;
			}
		}
	}

	return NULL;
}

/**
 * @brief Crea un nuevo hashmap y retorna el mismo 
 * 
 * @param order Orden del hashmap
 * @param value_size Tamaño de una entrada en el hashmap 
 * @return struct hash_map Hash map creado 
 */
static struct hash_map hash_map_new(unsigned order, size_t value_size)
{
	assert(order > 0); //orden no puede ser nulo

	struct hash_map empty =
	{
		.buckets    = vec_new(sizeof(struct vec)),
		.order      = order,
		.value_size = value_size
	};

	return empty;
}

/**
 * @brief Elimina todos los elementos de un hash map dado
 * 
 * @param map puntero al hash map cuyos elementos quieren eliminarse
 */
static void hash_map_clear(struct hash_map *map)
{
	for(size_t i = 0; i < map->buckets.length; ++i)
	{
		vec_clear(vec_get(&map->buckets, i));
	}

	vec_clear(&map->buckets);
}

/**
 * @brief Obtiene el tamaño de celda de un elemento de hash map
 *
 * @param map Mapa cuyo tamaño de celda quiere obtenerse
 */
static size_t hash_map_cell_size(struct hash_map *map)
{
	return map->value_size > sizeof(int) ? map->value_size : sizeof(int);
}

/**
 * @brief Obtiene el bucket que contiene el registro con la llave dada
 * 
 * @param map Hash map en el que se encuentra el bucket
 * @param key Llave del registro a buscar
 * @return struct vec* bucket que contiene el registro identificado por la llave, o NULL si el registro no existe
 */
static struct vec *hash_map_bucket_for(struct hash_map *map, int key)
{
	if(!map->buckets.data)
	{
		return NULL;
	}

	return vec_get(&map->buckets, (unsigned)key & ((1u << map->order) - 1));
}

/**
 * @brief Obtiene un par llave-valor de un hash map
 * 
 * @param map Hash map del cual se quiere obtener el par llave-valor
 * @param lookup Llave que identifica al para 
 * @return void* Puntero al par llave-valor
 */
static void *hash_map_get(struct hash_map *map, int lookup)
{
	struct vec *bucket = hash_map_bucket_for(map, lookup);
	char *pair = bucket_get_pair(bucket, lookup);

	return pair ? pair + hash_map_cell_size(map) : NULL;
}

/**
 * @brief Agrega un par llave-valor a un hash map
 * 
 * @param map Hash map al que se quiere agregar el registro
 * @param key Llave que identifica el registro
 * @param value Valor del registro
 * @return void* puntero al valor insertado
 */
static void *hash_map_put(struct hash_map *map, int key, const void *value)
{
	if(!map->buckets.data)
	{
		vec_resize(&map->buckets, 1lu << map->order);

		struct vec empty_bucket = vec_new(2 * hash_map_cell_size(map));
		for(size_t i = 0; i < 1lu << map->order; ++i)
		{
			*((struct vec*)vec_get(&map->buckets, i)) = empty_bucket;
		}
	}

	struct vec *bucket = hash_map_bucket_for(map, key);
	char *pair = bucket_get_pair(bucket, key);

	if(!pair)
	{
		pair = vec_emplace(bucket);
	}

	int *stored_key = (int*)pair;
	void *stored_value = pair + hash_map_cell_size(map);

	*stored_key = key;
	return memcpy(stored_value, value, map->value_size);
}

/**
 * @brief Remueve un registro identificado por una llave de un hash map
 * 
 * @param map Hash map del que se quiere remover el registro
 * @param key llave que identifica el registro
 */
static void hash_map_delete(struct hash_map *map, int key)
{
	struct vec *bucket = hash_map_bucket_for(map, key);
	char *pair = bucket_get_pair(bucket, key);

	if(pair)
	{
		vec_delete(bucket, (pair - (char*)bucket->data) / bucket->element_size);
	}
}

/**
 * @brief Obtiene un valor en el índice dado de un bucket de un hash map
 * 
 * @param map Hash map en el que se encuentra el valor
 * @param bucket Bucket del hash map en el que se encuentra el valor
 * @param index Índice en el bucket del valor que se quiere obtener
 * @return void* puntero al valor obtenido
 */
static void *bucket_get_value(struct hash_map *map, struct vec *bucket, size_t index)
{
	return (char*)vec_get(bucket, index) + hash_map_cell_size(map);
}

/**
 * @brief Maneja el cierre de la ventana de juego
 * 
 * @param exit_code Código que comunica la causa del cierre de la ventana de juego
 */
static void quit(int exit_code)
{
	close(game.timer_fd);

	if(game.net_file)
	{
		fclose(game.net_file);
	}

	if(game.window)
	{
		SDL_DestroyWindow(game.window);
	}

	for(size_t i = 0; i < game.sprites.buckets.length; ++i)
	{
		struct vec *bucket = vec_get(&game.sprites.buckets, i);
		for(size_t j = 0; j < bucket->length; ++j)
		{
			struct sprite *sprite = bucket_get_value(&game.sprites, bucket, j);

			SDL_DestroyTexture(sprite->texture);
			SDL_FreeSurface(sprite->surface);
		}
	}

	hash_map_clear(&game.sprites);
	hash_map_clear(&game.entities);

	IMG_Quit();
	SDL_Quit();

	exit(exit_code);
}

/**
 * @brief Escribe en la salida estándar de error un error encontrado por SDL 
 * 
 */
static void sdl_fatal(void)
{
	fprintf(stderr, "Fatal SDL error: %s\n", SDL_GetError());
	quit(1);
}

/**
 * @brief Escribe en la salida estándar de error un un error de SDL asociado a una imagen
 * 
 */
static void sdl_image_fatal(void)
{
	fprintf(stderr, "Fatal SDL_image error: %s\n", IMG_GetError());
	quit(1);
}

/**
 * @brief Comunica un estado de error fatal y cierra la aplicación inmediatamente 
 * 
 */
static void sys_fatal(void)
{
	perror("Fatal error");
	quit(1);
}

/**
 * @brief  Envía un mensaje en forma de Objeto JSON al servidor
 * 
 * Escribe Un mensaje como objeto JSON al archivo que representa el stream de salida al socket Servidor  
 * @param items Pares llave-valor que conforman el objeto a enviar 
 */
static void transmit(const struct key_value *items)
{
	struct json_object *root = json_object_new_object();
	for(; items->key; ++items)
	{
		json_object_object_add(root, items->key, items->value);
	}

	fprintf(game.net_file, "%s\n", json_object_to_json_string(root));
	fflush(game.net_file);

	json_object_put(root);
}

static bool move_on_tick(int *coordinate, const struct ratio *speed)
{
	if(speed->denominator > 0 && game.ticks % speed->denominator < abs(speed->numerator))
	{
		int jump = roundf((float)abs(speed->numerator) / speed->denominator);
		jump = jump > 0 ? jump : 1;
		jump = speed->numerator > 0 ? jump : -jump;

		*coordinate += jump;
		return true;
	}

	return false;
}

/**
 * @brief redibuja la pantalla de juego  
 * 
 * Redibuja todas las entidades de juego registradas en el hash map de entities pertenecientes al estado de juego game 
 */
static void redraw(void)
{
	if(SDL_RenderClear(game.renderer) < 0)
	{
		sdl_fatal();
	}

	for(size_t i = 0; i < game.entities.buckets.length; ++i)
	{
		struct vec *bucket = vec_get(&game.entities.buckets, i);
		for(size_t j = 0; j < bucket->length; ++j)
		{
			struct entity *entity = bucket_get_value(&game.entities, bucket, j);

			// Evita lógica de corto-circuito
			bool moved = move_on_tick(&entity->x, &entity->speed_x);
			moved = move_on_tick(&entity->y, &entity->speed_y) || moved;

			int sprite_id = *(int*)vec_get(&entity->sequence, entity->next_sprite);
			struct sprite *sprite = hash_map_get(&game.sprites, sprite_id);
			assert(sprite);

			if(moved && ++entity->next_sprite == entity->sequence.length)
			{
				entity->next_sprite = 0;
			}

			struct SDL_Rect destination =
			{
				.x = entity->x,
				.y = entity->y,
				.w = sprite->surface->w,
				.h = sprite->surface->h
			};

			if(SDL_RenderCopy(game.renderer, sprite->texture, NULL, &destination) < 0)
			{
				sdl_fatal();
			}

			if(moved)
			{
				struct key_value items[] =
				{
					{"op", json_object_new_string("move")},
					{"id", json_object_new_int(entity->id)},
					{"x",  json_object_new_int(entity->x)},
					{"y",  json_object_new_int(entity->y)},
					{NULL, NULL}
				};

				transmit(items);
			}
		}
	}

	SDL_RenderPresent(game.renderer);
	game.state = READY;
}

/**
 * @brief Comunica una despedida al Servidor
 * 
 * Escribe un objeto JSON que comunica una despedida al archivo que representa el stream de salida
 * al socket Servidor
 */
static void bye(void)
{
	struct key_value items[] =
	{
		{"op", json_object_new_string("bye")},
		{NULL, NULL}
	};

	transmit(items);
}

/**
 * @brief Reponsable de manejar los eventos de presión de teclas  
 * 
 * Dado un evento de presión de tecla, de ser dicha tecla parte de los controles de juego, envía al servidor
 * un mensaje que comunica el tipo de evento y la tecla que lo causó, de forma que el servidor pueda resolver
 * las instrucciones a emitir basado en dicho evento
 * @param event Puntero a la estructura que describe el evento de presión de teclado
 */
static void handle_key(const SDL_KeyboardEvent *event)
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
			key = "left";
			break;

		case SDLK_RIGHT:
			key = "right";
			break;

		case SDLK_UP:
			key = "up";
			break;

		case SDLK_DOWN:
			key = "down";
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
 * @brief  Obtiene un registro llave-valor de un objeto JSON y lo retorna como un objeto JSON separado
 * 
 * @param parent Objeto JSON que contiene el par llave-valor
 * @param key Llave que identifica el par 
 * @param type Tipo de dato almacenado en el valor del par 
 * @param required Indica si obtener el registro es obligatorio. de ser ese el caso, imprime un error en
 * la salida estándar de error
 * @return struct json_object* Objeto json que contiene solo el par llave-valor buscado 
 */
static struct json_object *expect_key
(
	struct json_object *parent, const char *key, enum json_type type, bool required
)
{
	struct json_object *value = json_object_object_get(parent, key); 
	if(value)
	{
		if(json_object_get_type(value) != type)
		{
			fprintf(stderr, "Error: mismatched JSON value type for key '%s'\n", key);
			quit(1);
		}
	} else if(required)
	{
		fprintf(stderr, "Error: expected JSON key '%s'\n", key);
		quit(1);
	}

	return value;
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
static void start_or_watch_game(struct json_object *message)
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

/**
 * @brief  Iniciliza los sprites del juego
 * 
 * Carga las imágenes a utilizar en el juego como sprites para que se puedan asociar posteriormente
 * a una entidad del juego
 */
static void init_sprites(void)
{
#define GLOB_FLAGS (GLOB_ERR | GLOB_NOSORT | GLOB_NOESCAPE)
	glob_t paths = { 0 };
	if(glob("assets/sprites/*/\?\?-*.png", GLOB_FLAGS, NULL, &paths) != 0)
	{
		fprintf(stderr, "Error: sprite glob failed (bad cwd?)\n");
		quit(1);
	}
#undef GLOB_FLAGS

	for(char **path = paths.gl_pathv; *path; ++path)
	{
		int sprite_id;
		if(sscanf(strrchr(*path, '/'), "/%d-", &sprite_id) != 1)
		{
			fprintf(stderr, "Error: bad sprite path (expected ../NN-*.png): %s\n", *path);
			quit(1);
		}

		SDL_Surface *surface = IMG_Load(*path);
		if(!surface)
		{
			sdl_image_fatal();
		}

		SDL_Texture *texture = SDL_CreateTextureFromSurface(game.renderer, surface);
		if(!texture)
		{
			sdl_fatal();
		}

		struct sprite sprite =
		{
			.surface = surface,
			.texture = texture
		};

		hash_map_put(&game.sprites, sprite_id, &sprite);
	}

	globfree(&paths);
}

/**
 * @brief Inicializa la pantalla de juego 
 *
 * En base a un mensaje del servidor que indica los parámetros gráficos del juego, 
 * crea una ventana bajo X11 y realiza su configuración inicial
 * @param message Mensaje del servidor que contiene parámetros para la ventana a crear
 */
static void init_graphics(struct json_object *message)
{
	int width = json_object_get_int(expect_key(message, "width", json_type_int, true));
	int height = json_object_get_int(expect_key(message, "height", json_type_int, true));

	assert(!game.window && !game.renderer);

	SDL_SysWMinfo wm_info;
	SDL_VERSION(&wm_info.version);

	if(SDL_CreateWindowAndRenderer(width, height, 0, &game.window, &game.renderer) != 0
	|| !SDL_GetWindowWMInfo(game.window, &wm_info))
	{
		sdl_fatal();
	} else if(wm_info.subsystem != SDL_SYSWM_X11)
	{
		fputs("Error: requires X11\n", stderr);
		quit(1);
	}

	game.x11_fd = XConnectionNumber(wm_info.info.x11.display);

	SDL_SetWindowTitle(game.window, "DonCEy Kong Jr.");
	if(!game.fullscreen)
	{
		SDL_SetWindowPosition(game.window, SDL_WINDOWPOS_CENTERED, SDL_WINDOWPOS_CENTERED);
	} else
	{
		SDL_DisplayMode display_mode;

		if(SDL_SetWindowFullscreen(game.window, SDL_WINDOW_FULLSCREEN) != 0
		|| SDL_GetCurrentDisplayMode(0, &display_mode) != 0
		|| SDL_RenderSetScale(game.renderer, (float)display_mode.w / width, (float)display_mode.h / height) != 0)
		{
			sdl_fatal();
		}
	}
}

static void init_clock(void)
{
	struct timespec timer_period =
	{
		.tv_sec  = 0,
		.tv_nsec = NANOS_PER_TICK
	};

	struct itimerspec timer_expiration =
	{
		.it_interval = timer_period,
		.it_value    = timer_period
	};

	if((game.timer_fd = timerfd_create(CLOCK_MONOTONIC, TFD_NONBLOCK | TFD_CLOEXEC)) == -1
	|| timerfd_settime(game.timer_fd, 0, &timer_expiration, NULL) != 0)
	{
		sys_fatal();
	}
}

/**
 * @brief Obtiene el valor del campo id del objeto JSON dado
 * 
 * Dado un objeto JSON, extrae el valor asociado a la llave "id". Utilizado para
 * procesar mensajes del servidor 
 * @param message Objeto JSON del que se extraerá el valor de id. Es un mensaje del servidor
 * @return int Valor de id extraído del mensaje JSON
 */
static int expect_id(struct json_object *message)
{
	return json_object_get_int(expect_key(message, "id", json_type_int, true));
}

/**
 * @brief Obtiene la entidad referenciada por un id contenido en un mensaje en formato JSON
 *
 * Dado un mensaje en formato JSON que se refiere a una entidad, extrae el valor de id del mensaje
 * y utiliza dicho valor para obtener un puntero a la entidad identificada por dicho id
 * @param message Mensaje del servidor con referencia a la entidad que se quiere obtener
 * @return struct entity* Puntero a la entidad identificada por el identificador dado
 */
static struct entity *expect_entity(struct json_object *message)
{
	int id = expect_id(message);

	struct entity *entity = hash_map_get(&game.entities, id);
	if(!entity)
	{
		fprintf(stderr, "Error: no entity has ID %d\n", id);
		quit(1);
	}

	return entity;
}

static struct ratio expect_ratio(struct json_object *message, const char *num_key, const char *denom_key)
{
	int num = json_object_get_int(expect_key(message, num_key, json_type_int, true));
	int denom = json_object_get_int(expect_key(message, denom_key, json_type_int, true));

	if(denom < 0 || (num != 0 && denom == 0))
	{
		fprintf(stderr, "Error: bad speed ratio: %d:%d\n", num, denom);
		quit(1);
	}

	struct ratio ratio =
	{
		.numerator   = num,
		.denominator = denom
	};

	return ratio;
}

static void expect_sequence(struct json_object *message, struct vec *sequence)
{
	struct json_object *sequence_ids = expect_key(message, "seq", json_type_array, true);
	if(json_object_array_length(sequence_ids) == 0)
	{
		fputs("Error: empty sequence array\n", stderr);
	}

	for(size_t i = 0; i < json_object_array_length(sequence_ids); ++i)
	{
		struct json_object *id_object = json_object_array_get_idx(sequence_ids, i);
		if(json_object_get_type(id_object) != json_type_int)
		{
			fputs("Error: expected int in sequence array\n", stderr);
			quit(1);
		}

		int id = json_object_get_int(id_object);
		if(!hash_map_get(&game.sprites, id))
		{
			fprintf(stderr, "Error: no sprite has ID %d\n", id);
			quit(1);
		}

		*(int*)vec_emplace(sequence) = id;
	}
}

/**
 * @brief Extrae valores de posicion vertical y horizontal de un objeto JSON
 *
 * Dado un mensaje en formato JSON proveniente del servidor, obtiene los valores
 * asociados a las llaves "x" y "y", los cuales se refieren a posiciones de pantalla
 * @param message Mensaje en formato JSON proveniente del servidor
 * @param x Parámetro de retorno en el que se almacena el valor de posición horizontal extraído
 * @param y Parámetro de retorno en el que se almacena el valor de posición vertical extraído
 */
static void expect_position(struct json_object *message, int *x, int *y)
{
	*x = json_object_get_int(expect_key(message, "x", json_type_int, true));
	*y = json_object_get_int(expect_key(message, "y", json_type_int, true));
}

/**
 * @brief Maneja los comandos provenientes del servidor
 * 
 * Dado un mensaje en formato JSON proveniente del servidor, analiza el mismo y determina
 * las acciones a tomar para llevar a cabo lo especificado por el comando. Los comandos son 
 * identificados por el valor asoaciado a la llave "op" en el mensaje. Si dicho para llave-valor
 /expect* no se encuentra en el mensaje o contiene un comando no válido, detiene la ejecución del
 * programa
 * @param message Mensaje en formato JSON proveniente del servidor 
 */
static void handle_command(struct json_object *message)
{
	const char *operation = json_object_get_string(expect_key(message, "op", json_type_string, true));

	if(strcmp(operation, "put") == 0)
	{
		int id = expect_id(message);

		struct entity new = { 0 };
		struct entity *existing = hash_map_get(&game.entities, id);
		struct entity *entity = existing ? existing : &new;

		entity->id = id;
		entity->next_sprite = 0;
		expect_position(message, &entity->x, &entity->y);

		if(existing)
		{
			vec_resize(&existing->sequence, 0);
		} else
		{
			new.sequence = vec_new(sizeof(int));
			entity = hash_map_put(&game.entities, id, &new);
		}

		expect_sequence(message, &entity->sequence);
		entity->speed_x = expect_ratio(message, "num_x", "denom_x");
		entity->speed_y = expect_ratio(message, "num_y", "denom_y");
	} else if(strcmp(operation, "move") == 0)
	{
		struct entity *entity = expect_entity(message);
		expect_position(message, &entity->x, &entity->y);
	} else if(strcmp(operation, "delete") == 0)
	{
		hash_map_delete(&game.entities, expect_id(message));
	} else if(strcmp(operation, "bye") == 0)
	{
		puts("Connection terminated by server");
		quit(0);
	} else
	{
		fprintf(stderr, "Error: unknown command '%s'\n", operation);
		quit(1);
	}
}

/**
 * @brief Procesa un mensaje enviado por el servidor
 *
 * Procesa un mensaje enviado del servidor, espera texto plano que pueda describir un
 * objeto JSON. Trata de parsear el objeto JSON, y si el parseo es exitoso, procesa
 * el mensaje ya sea como hanshake inicial, el handshake de inicio de juego o como
 * un comando
 * @param line Mensaje enviado del servidor como una cadena de caracteres
 */
static void receive(const char *line)
{
	struct json_object *root = json_tokener_parse(line);
	if(!root || json_object_get_type(root) != json_type_object)
	{
		fprintf(stderr, "Error: bad JSON: %s\n", line);
		quit(1);
	}

	struct json_object *error = expect_key(root, "error", json_type_string, false);
	if(error)
	{
		fprintf(stderr, "Error: server protocol error: %s\n", json_object_get_string(error));
		quit(1);
	}

	switch(game.state)
	{
		case HANDSHAKE_WHOAMI:
			start_or_watch_game(root);
			game.state = HANDSHAKE_INIT;
			break;

		case HANDSHAKE_INIT:
			init_graphics(root);
			init_sprites();
			init_clock();

			game.state = READY;
			break;

		default:
			handle_command(root);
			break;
	}

	json_object_put(root);
}

static void push_sdl_event(int type)
{
	SDL_Event event = { .type = type };
	if(SDL_PushEvent(&event) != 1)
	{
		sdl_fatal();
	}
}

#define X11_EVENT   (SDL_USEREVENT + 0)
#define TIMER_EVENT (SDL_USEREVENT + 1)

/**
 * @brief Controla el loop general del juego una vez iniciado
 * 
 */
static void event_loop(void)
{
	struct pollfd pollfds[] =
	{
		{
			.fd = game.net_fd,
			.events = POLLIN
		},
		{
			.fd = -1,
			.events = POLLIN
		},
		{
			.fd = -1,
			.events = POLLIN
		}
	};

	struct pollfd *net_pollfd = &pollfds[0];
	struct pollfd *x11_pollfd = &pollfds[1];
	struct pollfd *timer_pollfd = &pollfds[2];

	char *input_line = NULL;
	size_t input_length = 0;

	while(true)
	{
		SDL_Event event;
		while(SDL_PollEvent(&event) > 0)
		{
			switch(event.type)
			{
				case SDL_QUIT:
					bye();
					return;

				case SDL_KEYUP:
				case SDL_KEYDOWN:
					handle_key(&event.key);
					break;

				case X11_EVENT:
					errno = 0;
					while(getline(&input_line, &input_length, game.net_file) >= 0)
					{
						receive(input_line);
					}

					if(feof(game.net_file))
					{
						return;
					} else if(errno == EAGAIN)
					{
						clearerr(game.net_file);
						continue;
					} else
					{
						sys_fatal();
					}

					break;

				case TIMER_EVENT:
				{
					uint64_t expirations = 0;
					read(game.timer_fd, &expirations, sizeof expirations);

					if(expirations > 1)
					{
						fprintf(stderr, "Warning: %lu clock tick(s) missed\n", expirations - 1);
					}

					++game.ticks;
					redraw();

					break;
				}
			}
		}

		x11_pollfd->fd = game.x11_fd;
		timer_pollfd->fd = game.timer_fd;

		if(poll(pollfds, sizeof pollfds / sizeof(struct pollfd), -1) < 0 && errno != EINTR)
		{
			sys_fatal();
		}

		if(net_pollfd->revents)
		{
			push_sdl_event(X11_EVENT);
		}

		if(timer_pollfd->revents)
		{
			push_sdl_event(TIMER_EVENT);
		}
	}
}

/**
 * @brief Inicializa la conexión con el servidor  
 * 
 * @param node Dirección IP del servidor
 * @param service Puerto en el que escucha el servidor
 */
static void init_net(const char *node, const char *service)
{
	struct addrinfo *server_addrinfo = NULL;

	int addrinfo_result = getaddrinfo(node, service, NULL, &server_addrinfo);
	if(addrinfo_result != 0)
	{
		fprintf(stderr, "Network lookup error: %s\n", gai_strerror(addrinfo_result));
		quit(1);
	}

	game.net_fd = socket(server_addrinfo->ai_family, SOCK_STREAM, 0);

	if(game.net_fd < 0
	|| connect(game.net_fd, server_addrinfo->ai_addr, server_addrinfo->ai_addrlen) < 0
	|| fcntl(game.net_fd, F_SETFL, O_NONBLOCK) < 0)
	{
		sys_fatal();
	}

	freeaddrinfo(server_addrinfo);

	game.net_file = fdopen(game.net_fd, "a+");
	assert(game.net_file);
}

/**
 * @brief Inicializa los componentes de SDL2
 * 
 */
static void init_sdl(void)
{
	if(SDL_Init(SDL_INIT_VIDEO | SDL_INIT_AUDIO | SDL_INIT_TIMER) != 0)
	{
		sdl_fatal();
	} else if(IMG_Init(IMG_INIT_PNG) != IMG_INIT_PNG)
	{
		sdl_image_fatal();
	}
}

/**
 * @brief Escribe un mensaje de error como respuesta a un mal uso del ejecutable
 *
 * Si los parámetros de línea de comandos son erróneos, comunica que se puede correr
 * el comando dado con una bandera "--help" para solicitar mayor información
 * @param argv0 Comando que se ejecutó de manera inadecuada
 */
static void usage(const char *argv0)
{
	fprintf(stderr, "Run '%s --help' for more information\n", argv0);
}

/**
 * @brief Punto de entrada de la aplicación
 * 
 * Maneja la rutina de inicio del juego e inicializa el juego una vez terminada la misma
 * @param argc Cantidad de argumentos en la linea de comandos
 * @param argv Array que contiene los argumentos en la línea de comandos
 * @return int Código de salida que indica resultado de ejecución
 */
int main(int argc, char *argv[])
{
	const struct option CMDLINE_OPTIONS[] =
	{
		{"help",       no_argument, NULL, 'h'},
		{"version",    no_argument, NULL, 'v'},
		{"fullscreen", no_argument, NULL, 'f'},
		{NULL,         0,           NULL, 0}
	};

	game.sprites = hash_map_new(8, sizeof(struct sprite));
	game.entities = hash_map_new(8, sizeof(struct entity));

	while(true)
	{
		int option = getopt_long(argc, argv, "hvf", CMDLINE_OPTIONS, NULL);
		if(option == -1)
		{
			break;
		}

		switch(option)
		{
			case 'h':
				fprintf(stderr, "Usage: %s [-f|--fullscreen] <host> <port>\n", argv[0]);
				return 0;

			case 'v':
				puts("DonCEy Kong Jr. v1.0.0");
				return 0;

			case 'f':
				game.fullscreen = true;
				break;

			case '?':
				usage(argv[0]);
				return 1;
		}
	}

	if(argc - optind != 2)
	{
		fprintf(stderr, "%s: missing host or port\n", argv[0]);
		usage(argv[0]);

		return 1;
	}

	init_sdl();
	init_net(argv[optind], argv[optind + 1]);

	event_loop();
	quit(0);
}
