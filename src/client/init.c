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
#include <SDL2/SDL_ttf.h>
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
	if(glob(SPRITE_PATH_GLOB, GLOB_FLAGS, NULL, &paths) != 0)
	{
		fprintf(stderr, "Error: sprite glob failed (bad cwd?)\n");
		quit(EXIT_FAILURE);
	}

	// Se busca por cada uno de los posibles sprites
	for(char **path = paths.gl_pathv; *path; ++path)
	{
		// Se trata de encajar el patrón de filename con el id de sprite
		int sprite_id;
		if(sscanf(strrchr(*path, PATH_SEPARATOR), SPRITE_PATH_PATTERN, &sprite_id) != 1)
		{
			fprintf(stderr, "Error: bad sprite path (expected ../NN-*.png): %s\n", *path);
			quit(EXIT_FAILURE);
		}

		// Ahora se traduce este archivo a una textura utilizable

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

		// Finalmente, se agrega a las texturas conocidas

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
	// El servidor indica las dimensiones del área de juego
	int width = json_object_get_int(expect_key(message, CMD_WIDTH, json_type_int, true));
	int height = json_object_get_int(expect_key(message, CMD_HEIGHT, json_type_int, true));

	// Una precondición es que no se haya hecho esto ya anteriormente
	assert(!game.window && !game.renderer);

	// Información de tamaño de pantalla
	SDL_SysWMinfo wm_info;
	SDL_VERSION(&wm_info.version);

	// Se crea la ventana y se ajustan opcioens de gráficos
	if(SDL_CreateWindowAndRenderer(width, height, 0, &game.window, &game.renderer) != 0
	|| SDL_SetRenderDrawBlendMode(game.renderer, SDL_BLENDMODE_BLEND) < 0
	|| !SDL_GetWindowWMInfo(game.window, &wm_info))
	{
		sdl_fatal();
	} else if(wm_info.subsystem != SDL_SYSWM_X11)
	{
		fputs("Error: requires X11\n", stderr);
		quit(EXIT_FAILURE);
	}

	// El file descriptor de X11 se utiliza para tener nuestro propio loop en vez del de SDL
	game.x11_fd = XConnectionNumber(wm_info.info.x11.display);
	SDL_SetWindowTitle(game.window, "DonCEy Kong Jr.");

	// Casos de ventana regular vs fullscreen
	if(!(game.flags & GAME_FLAG_FULLSCREEN_MODESET) && !(game.flags & GAME_FLAG_FULLSCREEN_FAKE))
	{
		SDL_SetWindowPosition(game.window, SDL_WINDOWPOS_CENTERED, SDL_WINDOWPOS_CENTERED); //centra la pantalla
	} else
	{
		SDL_DisplayMode display_mode;
		int mode = (game.flags & GAME_FLAG_FULLSCREEN_FAKE) ? SDL_WINDOW_FULLSCREEN_DESKTOP : SDL_WINDOW_FULLSCREEN;

		if(SDL_SetWindowFullscreen(game.window, mode) != 0
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

	// Aspectos tanto inicial como de reiteración de expiración
	struct itimerspec timer_expiration =
	{
		.it_interval = timer_period,
		.it_value    = timer_period
	};

	// Se crea e inicializa el reloj
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

	// Se resuelve el par host/puerto a una estructura utilizable
	int addrinfo_result = getaddrinfo(node, service, NULL, &server_addrinfo);
	if(addrinfo_result != 0)
	{
		fprintf(stderr, "Network lookup error: %s\n", gai_strerror(addrinfo_result));
		quit(EXIT_FAILURE);
	}

	// Socket TCP
	game.net_fd = socket(server_addrinfo->ai_family, SOCK_STREAM, 0);

	// Se trata de conectar al servidor
	if(game.net_fd < 0
	|| connect(game.net_fd, server_addrinfo->ai_addr, server_addrinfo->ai_addrlen) < 0
	// Como no hay orden exacto de eventos entre servidor y usuario, debe ser non-blocking
	|| fcntl(game.net_fd, F_SETFL, O_NONBLOCK) < 0)
	{
		sys_fatal();
	}

	freeaddrinfo(server_addrinfo);

	// Se crea un FILE* a partir de este fd, para uso sencillo de stdio.h en vez de unistd.h
	game.net_file = fdopen(game.net_fd, FOPEN_MODE_APPEND);
	assert(game.net_file);
}

/**
 * @brief Inicializa los componentes de SDL2
 * 
 */
void init_sdl(void)
{
	if(SDL_Init(SDL_INIT_VIDEO) != 0)
	{
		sdl_fatal();
	} else if(IMG_Init(IMG_INIT_PNG) != IMG_INIT_PNG)
	{
		sdl_image_fatal();
	} else if(TTF_Init() != 0 || !(game.font = TTF_OpenFont(FONT_FILE, FONT_POINT_SIZE)))
	{
		sdl_ttf_fatal();
	}
}
