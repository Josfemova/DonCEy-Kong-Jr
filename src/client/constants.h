#ifndef CONSTANTS_H
#define CONSTANTS_H

#include <glob.h>

#include <SDL2/SDL.h>

// Frecuencia y período de reloj
#define CLOCK_HZ       30
#define NANOS_PER_TICK (1000000000 / CLOCK_HZ)

// Constantes de eventos de usuario de SDL
#define X11_EVENT   (SDL_USEREVENT + 0)
#define TIMER_EVENT (SDL_USEREVENT + 1)

// Tamaño máximo soportado para una línea enviada por el servidor
#define MAX_INPUT_LINE_SIZE 512

// Salto predeterminado en el caso de tasas de velocidad de la forma 0/n, n != 0
#define JUMP_DEFAULT 1

// Constantes RGBA
#define COLOR_BLACK     0
#define COLOR_WHITE     255
#define ALPHA_HIGHLIGHT 96

// Parámetros asociados a la etiqueta de estadísticas
#define FONT_FILE             "assets/arcade_n.ttf"
#define FONT_POINT_SIZE       8
#define STATS_LABEL_X         130
#define STATS_LABEL_Y         10
#define STATS_LABEL_FORMAT    "[J]%d [S]%04d"
#define STATS_LABEL_MAX_CHARS 32
#define STATS_LABEL_COLOR     { .r = COLOR_WHITE, .g = COLOR_WHITE, .b = COLOR_WHITE, .a = COLOR_WHITE }

// Llaves, comando y parámetros del protocolo con el servidor
#define CMD_OP          "op"
#define CMD_KEY         "key"
#define CMD_ERROR       "error"
#define CMD_BYE         "bye"
#define CMD_MOVE        "move"
#define CMD_INIT        "init"
#define CMD_PRESS       "press"
#define CMD_RELEASE     "release"
#define CMD_PUT         "put"
#define CMD_DELETE      "delete"
#define CMD_STATS       "stats"
#define CMD_HIGHLIGHT   "highlight"
#define CMD_UNHIGHLIGHT "unhighlight"
#define CMD_UNKNOWN     "unknown"
#define CMD_ID          "id"
#define CMD_SEQUENCE    "seq"
#define CMD_X           "x"
#define CMD_Y           "y"
#define CMD_Z           "z"
#define CMD_WIDTH       "width"
#define CMD_HEIGHT      "height"
#define CMD_WHOAMI      "whoami"
#define CMD_GAMES       "games"
#define CMD_LIVES       "lives"
#define CMD_SCORE       "score"

// Cadenas de teclas usadas en el protocolo con el servidor
#define KEY_UP    "up"
#define KEY_LEFT  "left"
#define KEY_RIGHT "right"
#define KEY_DOWN  "down"
#define KEY_JUMP  "jump"

// Constantes char
#define NEWLINE        '\n'
#define PATH_SEPARATOR '/'

// Constante entera que por convención indica un file descriptor no válido
#define FD_INVALID -1

// Relacionado a operaciones con rutas y globbing de sprites (autodetección)
#define GLOB_FLAGS           (GLOB_ERR | GLOB_NOSORT | GLOB_NOESCAPE)
#define SPRITE_PATH_GLOB    "assets/sprites/*/\?\?-*.png"
#define SPRITE_PATH_PATTERN "/%d-"
#define FOPEN_MODE_APPEND   "a+"

// Opciones de línea de comando
#define CMDLINE_HELP                "help"
#define CMDLINE_OPT_HELP            'h'
#define CMDLINE_VERSION             "version"
#define CMDLINE_OPT_VERSION         'v'
#define CMDLINE_FULLSCREEN          "fullscreen"
#define CMDLINE_OPT_FULLSCREEN      'f'
#define CMDLINE_FULLSCREEN_FAKE     "fullscreen-fake"
#define CMDLINE_OPT_FULLSCREEN_FAKE 'F'
#define CMDLINE_ALL_SHORTS          "hvfF"

// Parámetros  constantesempíricos para vectores
#define DEFAULT_VEC_CAPACITY 4
#define VEC_CAPACITY_FACTOR  2

// Parámetros constantes para hash maps
#define DEFAULT_MAP_ORDER       8
#define HASH_MAP_CELLS_PER_ITEM 2

#endif
