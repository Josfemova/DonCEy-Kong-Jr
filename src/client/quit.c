#include <stdio.h>
#include <stdlib.h>

#include <unistd.h>

#include <SDL2/SDL.h>
#include <SDL2/SDL_ttf.h>
#include <SDL2/SDL_image.h>

#include <json-c/json_object.h>

#include "util.h"
#include "constants.h"
#include "donceykongjr.h"

/**
 * @brief Maneja el cierre de la ventana de juego
 * 
 * @param exit_code Código que comunica la causa del cierre de la ventana de juego
 */
void quit(int exit_code)
{
	// Esta función destruye/libera todos los recursos en struct game

	// Reloj
	close(game.timer_fd);

	// Socket
	if(game.net_file)
	{
		fclose(game.net_file);
	}

	// Ventana
	if(game.window)
	{
		SDL_DestroyWindow(game.window);
	}

	// Secuencias de entidades
	for(struct hash_map_iter iter = hash_map_iter(&game.entities); iter.cell; hash_map_iter_next(&iter))
	{
		struct entity *entity = hash_map_iter_value(&iter);

		vec_clear(&entity->sequence);
	}

	// Texturas de sprites
	for(struct hash_map_iter iter = hash_map_iter(&game.sprites); iter.cell; hash_map_iter_next(&iter))
	{
		struct sprite *sprite = hash_map_iter_value(&iter);

		SDL_DestroyTexture(sprite->texture);
		SDL_FreeSurface(sprite->surface);
	}

	// Mapas
	hash_map_clear(&game.sprites);
	hash_map_clear(&game.entities);

	// Texto del label
	if(game.stats_label.texture)
	{
		SDL_DestroyTexture(game.stats_label.texture);
		SDL_FreeSurface(game.stats_label.surface);
	}

	// Fuente
	if(game.font)
	{
		TTF_CloseFont(game.font);
	}

	// Se desinicializan todas las bibliotecas
	TTF_Quit();
	IMG_Quit();
	SDL_Quit();

	exit(exit_code);
}

/**
 * @brief Escribe en la salida estándar de error un error encontrado por SDL 
 * 
 */
void sdl_fatal(void)
{
	fprintf(stderr, "Fatal SDL error: %s\n", SDL_GetError());
	quit(EXIT_FAILURE);
}

/**
 * @brief Escribe en la salida estándar de error un un error de SDL asociado a una imagen
 * 
 */
void sdl_image_fatal(void)
{
	fprintf(stderr, "Fatal SDL_image error: %s\n", IMG_GetError());
	quit(EXIT_FAILURE);
}

/**
 * @brief Escribe en la salida estándar de error sobre un error de SDL asociado a texto
 * 
 */
void sdl_ttf_fatal(void)
{
	fprintf(stderr, "Fatal SDL_ttf error: %s\n", TTF_GetError());
	quit(EXIT_FAILURE);
}

/**
 * @brief Comunica un estado de error fatal y cierra la aplicación inmediatamente 
 * 
 */
void sys_fatal(void)
{
	perror("Fatal error");
	quit(EXIT_FAILURE);
}

/**
 * @brief Comunica una despedida al Servidor
 * 
 * Escribe un objeto JSON que comunica una despedida al archivo que representa el stream de salida
 * al socket Servidor
 */
void bye(void)
{
	struct key_value items[] =
	{
		{CMD_OP, json_object_new_string(CMD_BYE)},
		{NULL,   NULL}
	};

	transmit(items);
}
