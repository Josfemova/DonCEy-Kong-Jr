#ifndef CONSTANTS_H
#define CONSTANTS_H

#include <glob.h>

#include <SDL2/SDL.h>

#define CLOCK_HZ       30
#define NANOS_PER_TICK (1000000000 / CLOCK_HZ)

#define GLOB_FLAGS (GLOB_ERR | GLOB_NOSORT | GLOB_NOESCAPE)

#define X11_EVENT   (SDL_USEREVENT + 0)
#define TIMER_EVENT (SDL_USEREVENT + 1)

#endif
