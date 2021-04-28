#include <errno.h>
#include <stdio.h>
#include <stdlib.h>
#include <stdint.h>
#include <assert.h>
#include <stdbool.h>

#include <poll.h>
#include <unistd.h>

#include <SDL2/SDL.h>
#include <SDL2/SDL_image.h>

#include <json-c/json_object.h>

#include "util.h"
#include "constants.h"
#include "donceykongjr.h"

/**
 * @brief Renderiza una textura en una posición.
 * 
 * Entradas: textura a renderizar y sus coordenadas.
 */
static void render(const struct sprite *sprite, int x, int y)
{
	struct SDL_Rect destination =
	{
		.x = x,
		.y = y,
		.w = sprite->surface->w,
		.h = sprite->surface->h
	};

	if(SDL_RenderCopy(game.renderer, sprite->texture, NULL, &destination) < 0)
	{
		sdl_fatal();
	}
}

/**
 * @brief Renderiza una entidad en su posición.
 * 
 * Entradas: entidad a renderizar y su textura
 */
static void render_entity(const struct entity *entity, const struct sprite *sprite)
{
	render(sprite, entity->x, entity->y);

	// Resaltado en caso de ser necesario
	if(entity->highlight)
	{
		struct SDL_Rect area =
		{
			.x = entity->x,
			.y = entity->y,
			.w = sprite->surface->w,
			.h = sprite->surface->h
		};

		if(SDL_SetRenderDrawColor(game.renderer, 255, 255, 255, 128) < 0
		|| SDL_RenderFillRect(game.renderer, &area) < 0
		|| SDL_SetRenderDrawColor(game.renderer, 0, 0, 0, SDL_ALPHA_OPAQUE) < 0)
		{
			sdl_fatal();
		}
	}
}

/**
 * @brief redibuja la pantalla de juego  
 * 
 * Redibuja todas las entidades de juego registradas en el hash map de entities pertenecientes al estado de juego game 
 */
void redraw(void)
{
	if(SDL_RenderClear(game.renderer) < 0)
	{
		sdl_fatal();
	}

	for(int depth = 0; depth <= game.max_depth; ++depth)
	{
		for(struct hash_map_iter iter = hash_map_iter(&game.entities); iter.cell; hash_map_iter_next(&iter))
		{
			int id = hash_map_iter_key(&iter);
			struct entity *entity = hash_map_iter_value(&iter);

			if(entity->z != depth)
			{
				continue;
			}

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

			render_entity(entity, sprite);

			// Esto es una optimización, el servidor ignoraría un mensaje de espectador en todo caso
			if(!(game.flags & GAME_FLAG_SPECTATOR) && moved)
			{
				struct key_value items[] =
				{
					{"op", json_object_new_string("move")},
					{"id", json_object_new_int(id)},
					{"x",  json_object_new_int(entity->x)},
					{"y",  json_object_new_int(entity->y)},
					{NULL, NULL}
				};

				transmit(items);
			}
		}
	}

	if(game.stats_label.texture)
	{
		render(&game.stats_label, STATS_LABEL_X, STATS_LABEL_Y);
	}

	SDL_RenderPresent(game.renderer);
}

/**
 * @brief Agrega un evento al queue de eventos de SDL
 * 
 * @param type Entero que identifica el tipo de evento a agregar
 */
static void push_sdl_event(int type)
{
	SDL_Event event = { .type = type };
	if(SDL_PushEvent(&event) != 1)
	{
		sdl_fatal();
	}
}

/**
 * @brief Controla el loop general del juego una vez iniciado
 * 
 */
void event_loop(void)
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

				case SDL_MOUSEBUTTONDOWN:
					handle_click(&event.button);
					break;

				case X11_EVENT:
					errno = 0;
					while(getline(&input_line, &input_length, game.net_file) >= 0)
					{
						receive(input_line);
					}

					if(feof(game.net_file))
					{
						puts("The server has closed the connection");
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
