#include <stdio.h>
#include <stdlib.h>

#include <SDL2/SDL.h>

static void sdl_fail(void)
{
	printf("Fatal SDL error: %s\n", SDL_GetError());

	SDL_Quit();
	exit(1);
}

int main()
{
	if(SDL_Init(SDL_INIT_VIDEO | SDL_INIT_AUDIO | SDL_INIT_TIMER) != 0) {
		sdl_fail();
	}

	SDL_Quit();
	return 0;
}
