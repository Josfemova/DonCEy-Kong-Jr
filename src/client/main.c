#include <stdio.h>
#include <stdlib.h>

#include <getopt.h>

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
