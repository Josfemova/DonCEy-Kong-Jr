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

struct vec
{
	void  *data;
	size_t length;
	size_t capacity;
	size_t element_size;
};

struct hash_map
{
	struct vec buckets;
	unsigned   order;
	size_t     value_size;
};

enum client_state
{
	HANDSHAKE_WHOAMI,
	HANDSHAKE_INIT,
	READY
};

struct sprite
{
	SDL_Surface *surface;
	SDL_Texture *texture;
};

struct entity
{
	int x;
	int y;
	int sprite;
};

static struct
{
	enum client_state state;
	int               net_fd;
	int               x11_fd;
	int               timer_fd;
	FILE             *net_file;
	SDL_Window       *window;
	SDL_Renderer     *renderer;
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
	.fullscreen = false
};

struct key_value
{
	const char         *key;
	struct json_object *value;
};

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

static void vec_clear(struct vec *vec)
{
	free(vec->data);

	vec->data = NULL;
	vec->length = vec->capacity = 0;
}

static void *vec_get(struct vec *vec, size_t index)
{
	return (char*)vec->data + vec->element_size * index;
}

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

static void *vec_emplace(struct vec *vec)
{
	vec_require_capacity(vec, vec->length + 1);
	return (char*)vec->data + vec->length++ * vec->element_size;
}

static void vec_delete(struct vec *vec, size_t index)
{
	assert(index < vec->length);

	void *target = (char*)vec->data + vec->element_size * index;
	void *source = (char*)target + vec->element_size;

	memmove(target, source, (vec->length-- - index - 1) * vec->element_size);
}

static void vec_resize(struct vec *vec, size_t new_size)
{
	vec_require_capacity(vec, new_size);
	if(new_size > vec->length)
	{
		memset(vec_get(vec, vec->length), 0, vec->element_size * (new_size - vec->length));
	}

	vec->length = new_size;
}

static void *bucket_get_pair(struct vec *bucket, int lookup)
{
	if(bucket)
	{
		for(size_t i = 0; i < bucket->length; ++i)
		{
			char *pair = vec_get(bucket, i);
			int *key = (int*)pair;

			if(lookup == *key)
			{
				return pair;
			}
		}
	}

	return NULL;
}

static struct hash_map hash_map_new(unsigned order, size_t value_size)
{
	assert(order > 0);

	struct hash_map empty =
	{
		.buckets    = vec_new(sizeof(struct vec)),
		.order      = order,
		.value_size = value_size
	};

	return empty;
}

static void hash_map_clear(struct hash_map *map)
{
	for(size_t i = 0; i < map->buckets.length; ++i)
	{
		vec_clear(vec_get(&map->buckets, i));
	}

	vec_clear(&map->buckets);
}

static size_t hash_map_cell_size(struct hash_map *map)
{
	return map->value_size > sizeof(int) ? map->value_size : sizeof(int);
}

static struct vec *hash_map_bucket_for(struct hash_map *map, int key)
{
	if(!map->buckets.data)
	{
		return NULL;
	}

	return vec_get(&map->buckets, (unsigned)key & ((1u << map->order) - 1));
}

static void *hash_map_get(struct hash_map *map, int lookup)
{
	struct vec *bucket = hash_map_bucket_for(map, lookup);
	char *pair = bucket_get_pair(bucket, lookup);

	return pair ? pair + hash_map_cell_size(map) : NULL;
}

static void hash_map_put(struct hash_map *map, int key, const void *value)
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
	memcpy(stored_value, value, map->value_size);
}

static void hash_map_delete(struct hash_map *map, int key)
{
	struct vec *bucket = hash_map_bucket_for(map, key);
	char *pair = bucket_get_pair(bucket, key);

	if(pair)
	{
		vec_delete(bucket, (pair - (char*)bucket->data) / bucket->element_size);
	}
}

static void *bucket_get_value(struct hash_map *map, struct vec *bucket, size_t index)
{
	return (char*)vec_get(bucket, index) + hash_map_cell_size(map);
}

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

static void sdl_fatal(void)
{
	fprintf(stderr, "Fatal SDL error: %s\n", SDL_GetError());
	quit(1);
}

static void sdl_image_fatal(void)
{
	fprintf(stderr, "Fatal SDL_image error: %s\n", IMG_GetError());
	quit(1);
}

static void sys_fatal(void)
{
	perror("Fatal error");
	quit(1);
}

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
			struct sprite *sprite = hash_map_get(&game.sprites, entity->sprite);
			assert(sprite);

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
		}
	}

	SDL_RenderPresent(game.renderer);
	game.state = READY;
}

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

static void bye(void)
{
	struct key_value items[] =
	{
		{"op", json_object_new_string("bye")},
		{NULL, NULL}
	};

	transmit(items);
}

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
		{"op", json_object_new_string(operation)},
		{"key", json_object_new_string(key)},
		{NULL, NULL}
	};

	transmit(items);
}

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
			bye();
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
			for(size_t i = 0; i < json_object_array_length(games); ++i)
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
		{NULL, NULL}
	};

	transmit(items);
}

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

static int expect_id(struct json_object *message)
{
	return json_object_get_int(expect_key(message, "id", json_type_int, true));
}

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

static int expect_sprite(struct json_object *message)
{
	int id = json_object_get_int(expect_key(message, "sprite", json_type_int, true));
	if(!hash_map_get(&game.sprites, id))
	{
		fprintf(stderr, "Error: no sprite has ID %d\n", id);
		quit(1);
	}

	return id;
}

static void expect_position(struct json_object *message, int *x, int *y)
{
	*x = json_object_get_int(expect_key(message, "x", json_type_int, true));
	*y = json_object_get_int(expect_key(message, "y", json_type_int, true));
}

static void handle_command(struct json_object *message)
{
	const char *operation = json_object_get_string(expect_key(message, "op", json_type_string, true));

	if(strcmp(operation, "move") == 0)
	{
		struct entity *entity = expect_entity(message);
		expect_position(message, &entity->x, &entity->y);
	} else if(strcmp(operation, "transition") == 0)
	{
		expect_entity(message)->sprite = expect_sprite(message);
	} else if(strcmp(operation, "delete") == 0)
	{
		hash_map_delete(&game.entities, expect_id(message));
	} else if(strcmp(operation, "new") == 0)
	{
		struct entity entity = { .sprite = expect_sprite(message) };
		expect_position(message, &entity.x, &entity.y);

		hash_map_put(&game.entities, expect_id(message), &entity);
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

static void usage(const char *argv0)
{
	fprintf(stderr, "Run '%s --help' for more information\n", argv0);
}

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
				fprintf(stderr, "Usage: %s <host> <port>\n", argv[0]);
				return 0;

			case 'v':
				fputs("DonCEy Kong Jr. v1.0.0\n", stderr);
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
