#include <stdio.h>
#include <stdlib.h>

#include <getopt.h>

#include "constants.h"
#include "donceykongjr.h"

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
	// Primero se parsea la línea de comandos
	const struct option CMDLINE_OPTIONS[] =
	{
		{CMDLINE_HELP,            no_argument, NULL, CMDLINE_OPT_HELP},
		{CMDLINE_VERSION,         no_argument, NULL, CMDLINE_OPT_VERSION},
		{CMDLINE_FULLSCREEN,      no_argument, NULL, CMDLINE_OPT_FULLSCREEN},
		{CMDLINE_FULLSCREEN_FAKE, no_argument, NULL, CMDLINE_OPT_FULLSCREEN_FAKE},
		{NULL,                    0,           NULL, 0}
	};

	game.sprites = hash_map_new(DEFAULT_MAP_ORDER, sizeof(struct sprite));
	game.entities = hash_map_new(DEFAULT_MAP_ORDER, sizeof(struct entity));

	int option;
	while((option = getopt_long(argc, argv, CMDLINE_ALL_SHORTS, CMDLINE_OPTIONS, NULL)) != -1)
	{
		switch(option)
		{
			case CMDLINE_OPT_HELP:
				fprintf
				(
					stderr,
					"Usage: %s [OPTION]... <host> <port>\n"
					"\n"
					"    -f|--fullscreen       Enters fullscreen through Kernel Mode Setting\n"
					"    -F|--fake-fullscreen  Displays a maximized and borderless X11 window\n",
					argv[0]
				);

				return 0;

			case CMDLINE_OPT_VERSION:
				puts("DonCEy Kong Jr. v1.0.0");
				return 0;

			case CMDLINE_OPT_FULLSCREEN:
				game.flags |= GAME_FLAG_FULLSCREEN_MODESET;
				break;

			case CMDLINE_OPT_FULLSCREEN_FAKE:
				game.flags |= GAME_FLAG_FULLSCREEN_FAKE;
				break;

			case '?':
				usage(argv[0]);
				return EXIT_FAILURE;
		}
	}

	if(argc - optind != 2)
	{
		fprintf(stderr, "%s: missing host or port\n", argv[0]);
		usage(argv[0]);

		return EXIT_FAILURE;
	}

	// Inicio del juego
	init_sdl();
	init_net(argv[optind], argv[optind + 1]);

	event_loop();
	quit(EXIT_SUCCESS);
}
