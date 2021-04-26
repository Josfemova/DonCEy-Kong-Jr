
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
