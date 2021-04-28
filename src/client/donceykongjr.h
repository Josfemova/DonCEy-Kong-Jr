#ifndef DONCEYKONGJR_H
#define DONCEYKONGJR_H

#include <stdio.h>
#include <stddef.h>
#include <stdbool.h>

#include <X11/Xlib.h>

#include <SDL2/SDL.h>
#include <SDL2/SDL_ttf.h>
#include <SDL2/SDL_image.h>

#include <json-c/json_object.h>

#include "util.h"

/**
 * @brief Representa los componentes de un sprite
 * 
 */
struct sprite
{
	SDL_Surface *surface;
	SDL_Texture *texture;
};

/**
 * @brief representa una fracción numérica
 * 
 */
struct ratio
{
	int      numerator;
	unsigned denominator;
};

/**
 * @brief Representa una entidad
 * 
 */
struct entity
{
	int          x;
	int          y;
	int          z;
	struct vec   sequence;
	size_t       next_sprite;
	struct ratio speed_x;
	struct ratio speed_y;
	bool         highlight;
};

/**
 * @brief Representa un par llave-valor
 * 
 */
struct key_value
{
	const char         *key;
	struct json_object *value;
};

/**
 * @brief Describe el estado del cliente
 *
 */
extern struct game
{
	/**
	 * @brief Estados posibles en los que se puede encontrar un jugador
	 * 
	 */
	enum
	{
		GAME_STATE_HANDSHAKE_WHOAMI,
		GAME_STATE_HANDSHAKE_INIT,
		GAME_STATE_READY
	} state;

	/**
	 * @brief Bitflags que indican distintas opciones bajo las que puede operar el cliente.
	 */
	enum
	{
		GAME_FLAG_FULLSCREEN_MODESET = 0x01,
		GAME_FLAG_FULLSCREEN_FAKE    = 0x02,
		GAME_FLAG_SPECTATOR          = 0x04
	} flags;

	int               net_fd;
	int               x11_fd;
	int               timer_fd;
	FILE             *net_file;
	SDL_Window       *window;
	SDL_Renderer     *renderer;
	TTF_Font         *font;
	size_t            ticks;
	struct hash_map   sprites;
	struct hash_map   entities;
	struct sprite     stats_label;
	int               max_depth;
} game;

/**
 * @brief  Iniciliza los sprites del juego
 * 
 * Carga las imágenes a utilizar en el juego como sprites para que se puedan asociar posteriormente
 * a una entidad del juego
 */
void init_sprites(void);

/**
 * @brief Inicializa la pantalla de juego 
 *
 * En base a un mensaje del servidor que indica los parámetros gráficos del juego, 
 * crea una ventana bajo X11 y realiza su configuración inicial
 * @param message Mensaje del servidor que contiene parámetros para la ventana a crear
 */
void init_graphics(struct json_object *message);

/**
 * @brief Inicia un reloj para llevar cuenta del tiempo transcurrido
 * 
 * Configura e inicializa el timer del juego que se utiliza para llevar
 * registro de los ticks transcurridos
 */
void init_clock(void);

/**
 * @brief Inicializa la conexión con el servidor  
 * 
 * @param node Dirección IP del servidor
 * @param service Puerto en el que escucha el servidor
 */
void init_net(const char *node, const char *service);

/**
 * @brief Inicializa los componentes de SDL2
 * 
 */
void init_sdl(void);

/**
 * @brief redibuja la pantalla de juego  
 * 
 * Redibuja todas las entidades de juego registradas en el hash map de entities pertenecientes al estado de juego game 
 */
void redraw(void);

/**
 * @brief Controla el loop general del juego una vez iniciado
 * 
 */
void event_loop(void);

/**
 * @brief Mueve una entidad, dada la cantidad correcta de ticks transcurridos 
 * 
 * Dada una coordenada y la velocidad(cantidad movimiento/ticks) en esa 
 * coordenada de una entidad, decide si dicha entidad debe moverse o no. 
 * De ser el número de ticks factor del denominador de la velocidad, mueve
 * la entidad la cantidad unidades de longitud específicadas por el 
 * numerador de la velocidad. Retorna un valor booleano que indica si 
 * hubo movimiento o no
 * 
 * @param coordinate Puntero a un campo de eje coordenado de una entidad (x o y)
 * @param speed Fracción de velocidad expresada como (cantidad movimiento/ticks)
 * @return true La entidad se movió en este tick
 * @return false La entidad no se movió en este tick
 */
bool move_on_tick(int *coordinate, const struct ratio *speed);

/**
 * @brief Actualiza la etiqueta de estadísticas en pantalla.
 *
 * @param lives cantidad de vidas restantes
 * @param score puntaje
 */
void update_stats(int lives, int score);

/**
 * @brief Reponsable de manejar los eventos de presión de teclas  
 * 
 * Dado un evento de presión de tecla, de ser dicha tecla parte de los controles de juego, envía al servidor
 * un mensaje que comunica el tipo de evento y la tecla que lo causó, de forma que el servidor pueda resolver
 * las instrucciones a emitir basado en dicho evento
 * @param event Puntero a la estructura que describe el evento de presión de teclado
 */
void handle_key(const SDL_KeyboardEvent *event);

/**
 * @brief Reacciona a un click (enumeración de IDs).
 *
 * Entradas: evento de mouse.
 */
void handle_click(const SDL_MouseButtonEvent *event);

/**
 * @brief Emite el mensaje que le indica al servidor el modo de operación del cliente
 * y a qué partida quiere unirse el mismo
 *
 * Controla la rutina de inicio del cliente una vez recibido el mensaje inicial del Servidor. 
 * Dado el estado del servidor, da ciertas opciones al usuario para inicializalizar un juego.
 * @param message Mensaje inicial enviado del Servidor al cliente
 */
void start_or_watch_game(struct json_object *message);

/**
 * @brief  Envía un mensaje en forma de Objeto JSON al servidor
 * 
 * Escribe Un mensaje como objeto JSON al archivo que representa el stream de salida al socket Servidor  
 * @param items Pares llave-valor que conforman el objeto a enviar 
 */
void transmit(const struct key_value *items);

/**
 * @brief Procesa un mensaje enviado por el servidor
 *
 * Procesa un mensaje enviado del servidor, espera texto plano que pueda describir un
 * objeto JSON. Trata de parsear el objeto JSON, y si el parseo es exitoso, procesa
 * el mensaje ya sea como hanshake inicial, el handshake de inicio de juego o como
 * un comando
 * @param line Mensaje enviado del servidor como una cadena de caracteres
 */
void receive(const char *line);

/**
 * @brief  Obtiene un registro llave-valor de un objeto JSON y lo retorna como un objeto JSON separado
 * 
 * @param parent Objeto JSON que contiene el par llave-valor
 * @param key Llave que identifica el par 
 * @param type Tipo de dato almacenado en el valor del par 
 * @param required Indica si obtener el registro es obligatorio. de ser ese el caso, imprime un error en
 * la salida estándar de error
 * @return struct json_object* Objeto json que contiene solo el par llave-valor buscado 
 */
struct json_object *expect_key
(
	struct json_object *parent, const char *key, enum json_type type, bool required
);

/**
 * @brief Maneja el cierre de la ventana de juego
 * 
 * @param exit_code Código que comunica la causa del cierre de la ventana de juego
 */
void quit(int exit_code);

/**
 * @brief Escribe en la salida estándar de error un error encontrado por SDL 
 * 
 */
void sdl_fatal(void);

/**
 * @brief Escribe en la salida estándar de error un un error de SDL asociado a una imagen
 * 
 */
void sdl_image_fatal(void);

/**
 * @brief Escribe en la salida estándar de error sobre un error de SDL asociado a texto
 * 
 */
void sdl_ttf_fatal(void);

/**
 * @brief Comunica un estado de error fatal y cierra la aplicación inmediatamente 
 * 
 */
void sys_fatal(void);

/**
 * @brief Comunica una despedida al Servidor
 * 
 * Escribe un objeto JSON que comunica una despedida al archivo que representa el stream de salida
 * al socket Servidor
 */
void bye(void);

#endif
