#include <stdio.h>
#include <stdlib.h>
#include <stdbool.h>

#include <json-c/json_object.h>
#include <json-c/json_tokener.h>

#include "util.h"
#include "constants.h"
#include "donceykongjr.h"

/**
 * @brief  Envía un mensaje en forma de Objeto JSON al servidor
 * 
 * Escribe Un mensaje como objeto JSON al archivo que representa el stream de salida al socket Servidor  
 * @param items Pares llave-valor que conforman el objeto a enviar 
 */
void transmit(const struct key_value *items)
{
	// Se traduce las estructuras key-value a un objeto JSON
	struct json_object *root = json_object_new_object();
	for(; items->key; ++items)
	{
		json_object_object_add(root, items->key, items->value);
	}

	// Se envía este objeto JSON (recordar que net_file surgió de un fdopen())
	fprintf(game.net_file, "%s\n", json_object_to_json_string(root));
	fflush(game.net_file);

	// Se libera memoria
	json_object_put(root);
}

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
)
{
	struct json_object *value = json_object_object_get(parent, key); 
	if(value)
	{
		if(json_object_get_type(value) != type)
		{
			fprintf(stderr, "Error: mismatched JSON value type for key '%s'\n", key);
			quit(EXIT_FAILURE);
		}
	} else if(required)
	{
		fprintf(stderr, "Error: expected JSON key '%s'\n", key);
		quit(EXIT_FAILURE);
	}

	return value;
}

/**
 * @brief Obtiene el valor del campo id del objeto JSON dado
 * 
 * Dado un objeto JSON, extrae el valor asociado a la llave "id". Utilizado para
 * procesar mensajes del servidor 
 * @param message Objeto JSON del que se extraerá el valor de id. Es un mensaje del servidor
 * @return int Valor de id extraído del mensaje JSON
 */
static int expect_id(struct json_object *message)
{
	return json_object_get_int(expect_key(message, CMD_ID, json_type_int, true));
}

/**
 * @brief Obtiene la entidad referenciada por un id contenido en un mensaje en formato JSON
 *
 * Dado un mensaje en formato JSON que se refiere a una entidad, extrae el valor de id del mensaje
 * y utiliza dicho valor para obtener un puntero a la entidad identificada por dicho id
 * @param message Mensaje del servidor con referencia a la entidad que se quiere obtener
 * @return struct entity* Puntero a la entidad identificada por el identificador dado
 */
static struct entity *expect_entity(struct json_object *message)
{
	int id = expect_id(message);

	struct entity *entity = hash_map_get(&game.entities, id);
	if(!entity)
	{
		fprintf(stderr, "Error: no entity has ID %d\n", id);
		quit(EXIT_FAILURE);
	}

	return entity;
}

/**
 * @brief Extrae una razon matemática de un mensaje en formato JSON
 * 
 * Dado un mensaje en formato JSON que contiene información respecto a una razón matemática,
 * extrae la información del nominador y denominador de dicha razón matemática del objeto, y crea
 * un valor de struct ratio en base a dicha información 
 * @param message Objeto JSON que contiene los campos que se quiere extraer
 * @param num_key Cadena de caracteres utilizada como llave del valor que identifica el numerador
 * @param denom_key Cadena de caracteres utilizada como llave del valor que identifica el denominador
 * @return struct ratio Razón matemática extraída del objeto JSON
 */
static struct ratio expect_ratio(struct json_object *message, const char *num_key, const char *denom_key)
{
	int num = json_object_get_int(expect_key(message, num_key, json_type_int, true));
	int denom = json_object_get_int(expect_key(message, denom_key, json_type_int, true));

	// Se acepta 0/0 para el caso de una entidad estática
	if(denom < 0 || (num != 0 && denom == 0))
	{
		fprintf(stderr, "Error: bad speed ratio: %d:%d\n", num, denom);
		quit(EXIT_FAILURE);
	}

	struct ratio ratio =
	{
		.numerator   = num,
		.denominator = denom
	};

	return ratio;
}

/**
 * @brief Extrae una secuencia de id's de sprites de un mensaje en formato JSON
 * 
 * Dado un mensaje en formato JSON que contiene un campo con llave "seq" que tiene como
 * valor asociado un arreglo JSON de miembros de tipo entero, extrae dicho arreglo
 * y lo carga sobre un vector
 * @param message Objeto JSON que contiene el arreglo secuencia a extraerse
 * @param sequence Parámetro de salida que toma el valor de la secuencia de ID's de
 * 		  		   sprites
 */
static void expect_sequence(struct json_object *message, struct vec *sequence)
{
	struct json_object *sequence_ids = expect_key(message, CMD_SEQUENCE, json_type_array, true);
	if(json_object_array_length(sequence_ids) == 0)
	{
		fputs("Error: empty sequence array\n", stderr);
	}

	//Recorre el array json
	for(size_t i = 0; i < json_object_array_length(sequence_ids); ++i)
	{
		struct json_object *id_object = json_object_array_get_idx(sequence_ids, i);
		if(json_object_get_type(id_object) != json_type_int) 
		{
			fputs("Error: expected int in sequence array\n", stderr);
			quit(EXIT_FAILURE);//fallar si no se obtiene un valor de tipo entero
		}

		int id = json_object_get_int(id_object);
		if(!hash_map_get(&game.sprites, id))
		{
			fprintf(stderr, "Error: no sprite has ID %d\n", id);
			quit(EXIT_FAILURE);
		}
		//agrega elemento al vector de retorno
		*(int*)vec_emplace(sequence) = id;
	}
}

/**
 * @brief Extrae valores de posicion vertical y horizontal de un objeto JSON
 *
 * Dado un mensaje en formato JSON proveniente del servidor, obtiene los valores
 * asociados a las llaves "x" y "y", los cuales se refieren a posiciones de pantalla
 * @param message Mensaje en formato JSON proveniente del servidor
 * @param x Parámetro de retorno en el que se almacena el valor de posición horizontal extraído
 * @param y Parámetro de retorno en el que se almacena el valor de posición vertical extraído
 */
static void expect_position(struct json_object *message, int *x, int *y)
{
	*x = json_object_get_int(expect_key(message, CMD_X, json_type_int, true));
	*y = json_object_get_int(expect_key(message, CMD_Y, json_type_int, true));
}

/**
 * @brief Maneja los comandos provenientes del servidor
 * 
 * Dado un mensaje en formato JSON proveniente del servidor, analiza el mismo y determina
 * las acciones a tomar para llevar a cabo lo especificado por el comando. Los comandos son 
 * identificados por el valor asoaciado a la llave "op" en el mensaje. Si dicho para llave-valor
 /expect* no se encuentra en el mensaje o contiene un comando no válido, detiene la ejecución del
 * programa
 * @param message Mensaje en formato JSON proveniente del servidor 
 */
static void handle_command(struct json_object *message)
{
	// Dispatch principal de comandos provenientes del servidor
	const char *operation = json_object_get_string(expect_key(message, CMD_OP, json_type_string, true));
	if(strcmp(operation, CMD_PUT) == 0)//comando de crear entidades
	{
		int id = expect_id(message);

		// Se crea una nueva entidad (asumiendo que no existe)
		struct entity new = { 0 };
		struct entity *existing = hash_map_get(&game.entities, id);
		struct entity *entity = existing ? existing : &new;

		// En caso de que sí existiera, simplemente se sobreescribe
		if(existing)
		{
			vec_resize(&existing->sequence, 0);
		} else
		{
			new.sequence = vec_new(sizeof(int));
			entity = hash_map_put(&game.entities, id, &new);
		}

		// Se insertan otros valores que incluye el mensaje
		entity->next_sprite = 0;
		expect_position(message, &entity->x, &entity->y);
		expect_sequence(message, &entity->sequence);

		// Profundidad
		entity->z = json_object_get_int(expect_key(message, CMD_Z, json_type_int, true));
		if(entity->z > game.max_depth)
		{
			game.max_depth = entity->z;
		}

		entity->speed_x = expect_ratio(message, "num_x", "denom_x");
		entity->speed_y = expect_ratio(message, "num_y", "denom_y");
	} else if(strcmp(operation, CMD_MOVE) == 0)//comando de mover un entidad
	{
		struct entity *entity = expect_entity(message);
		expect_position(message, &entity->x, &entity->y);
	} else if(strcmp(operation, CMD_DELETE) == 0)//comando para eliminar una entidad
	{
		int id = expect_id(message);

		struct entity *entity = hash_map_get(&game.entities, id);
		if(entity)
		{
			vec_clear(&entity->sequence);
			hash_map_delete(&game.entities, id);
		}
	} else if(strcmp(operation, CMD_STATS) == 0) // Actualización de estadísticas
	{
		int lives = json_object_get_int(expect_key(message, CMD_LIVES, json_type_int, true));
		int score = json_object_get_int(expect_key(message, CMD_SCORE, json_type_int, true));

		update_stats(lives, score);
	} else if(strcmp(operation, CMD_HIGHLIGHT) == 0) // Resaltado de guía
	{
		expect_entity(message)->highlight = true;
	} else if(strcmp(operation, CMD_UNHIGHLIGHT) == 0) // Quitar resaltado
	{
		expect_entity(message)->highlight = false;
	} else if(strcmp(operation, CMD_BYE) == 0)//Servidor se despide del cliente
	{
		puts("Connection terminated by server");
		quit(EXIT_SUCCESS);
	} else
	{
		fprintf(stderr, "Error: unknown command '%s'\n", operation);
		quit(EXIT_FAILURE);
	}
}

/**
 * @brief Procesa un mensaje enviado por el servidor
 *
 * Procesa un mensaje enviado del servidor, espera texto plano que pueda describir un
 * objeto JSON. Trata de parsear el objeto JSON, y si el parseo es exitoso, procesa
 * el mensaje ya sea como hanshake inicial, el handshake de inicio de juego o como
 * un comando
 * @param line Mensaje enviado del servidor como una cadena de caracteres
 */
void receive(const char *line)
{
	struct json_object *root = json_tokener_parse(line);
	if(!root || json_object_get_type(root) != json_type_object)
	{
		// El servidor envió un mensaje con formato incorrecto
		fprintf(stderr, "Error: bad JSON: %s\n", line);
		quit(EXIT_FAILURE);
	}

	// Siempre se considera primero la posibilidad de un mensaje de error
	struct json_object *error = expect_key(root, CMD_ERROR, json_type_string, false);
	if(error)
	{
		fprintf(stderr, "Error: server failure: %s\n", json_object_get_string(error));
		quit(EXIT_FAILURE);
	}

	// Se esperan distintos comandos según el estado de inicialización y handshake
	switch(game.state)
	{
		case GAME_STATE_HANDSHAKE_WHOAMI:
			start_or_watch_game(root);
			game.state = GAME_STATE_HANDSHAKE_INIT;
			break;

		case GAME_STATE_HANDSHAKE_INIT:
			init_graphics(root);
			init_sprites();
			init_clock();

			game.state = GAME_STATE_READY;
			break;

		default:
			handle_command(root);
			break;
	}

	json_object_put(root);
}
