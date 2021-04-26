#include <stdio.h>
#include <stdlib.h>
#include <assert.h>

#include <glob.h>
#include <fcntl.h>
#include <netdb.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <sys/timerfd.h>

#include <X11/Xlib.h>

#include <SDL2/SDL.h>
#include <SDL2/SDL_image.h>
#include <SDL2/SDL_syswm.h>

#include <json-c/json_object.h>

#include "util.h"
#include "constants.h"
#include "donceykongjr.h"

/**
 * @brief  Iniciliza los sprites del juego
 * 
 * Carga las imágenes a utilizar en el juego como sprites para que se puedan asociar posteriormente
 * a una entidad del juego
 */
void init_sprites(void)
{
	glob_t paths = { 0 };
	if(glob("assets/sprites/*/\?\?-*.png", GLOB_FLAGS, NULL, &paths) != 0)
	{
		fprintf(stderr, "Error: sprite glob failed (bad cwd?)\n");
		quit(1);
	}

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
void init_graphics(struct json_object *message)
{
	int width = json_object_get_int(expect_key(message, "width", json_type_int, true));
	int height = json_object_get_int(expect_key(message, "height", json_type_int, true));

	assert(!game.window && !game.renderer);

	SDL_SysWMinfo wm_info; //Información de pantalla
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
		SDL_SetWindowPosition(game.window, SDL_WINDOWPOS_CENTERED, SDL_WINDOWPOS_CENTERED); //centra la pantalla
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

/**
 * @brief Inicia un reloj para llevar cuenta del tiempo transcurrido
 * 
 * Configura e inicializa el timer del juego que se utiliza para llevar
 * registro de los ticks transcurridos
 */
void init_clock(void)
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
 * @brief Inicializa la conexión con el servidor  
 * 
 * @param node Dirección IP del servidor
 * @param service Puerto en el que escucha el servidor
 */
void init_net(const char *node, const char *service)
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
void init_sdl(void)
{
	if(SDL_Init(SDL_INIT_VIDEO | SDL_INIT_AUDIO | SDL_INIT_TIMER) != 0)
	{
		sdl_fatal();
	} else if(IMG_Init(IMG_INIT_PNG) != IMG_INIT_PNG)
	{
		sdl_image_fatal();
	}
}
