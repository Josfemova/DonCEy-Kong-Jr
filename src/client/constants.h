#ifndef CONSTANTS_H
#define CONSTANTS_H

#include <glob.h>

#include <SDL2/SDL.h>

#define CLOCK_HZ       30
#define NANOS_PER_TICK (1000000000 / CLOCK_HZ)

#define GLOB_FLAGS (GLOB_ERR | GLOB_NOSORT | GLOB_NOESCAPE)

#define X11_EVENT   (SDL_USEREVENT + 0)
#define TIMER_EVENT (SDL_USEREVENT + 1)

#define FONT_FILE             "assets/arcade_n.ttf"
#define FONT_POINT_SIZE       8
#define STATS_LABEL_X         130
#define STATS_LABEL_Y         10
#define STATS_LABEL_FORMAT    "[J]%d [S]%04d"
#define STATS_LABEL_MAX_CHARS 32
#define STATS_LABEL_COLOR     { .r = 255, .g = 255, .b = 255, .a = 255 }

#define MAX_INPUT_LINE_SIZE 512

#endif
