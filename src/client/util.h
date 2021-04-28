#ifndef UTIL_H
#define UTIL_H

#include <stddef.h>

/**
 * @brief Implementación de un array dinámico
 * 
 */
struct vec
{
	void  *data;
	size_t length;
	size_t capacity;
	size_t element_size;
};

/**
 * @brief  Implementación de hash map
 * 
 */
struct hash_map
{
	struct vec buckets;
	unsigned   order;
	size_t     value_size;
};

/**
 * @brief Iterador sobre un hash map
 */
struct hash_map_iter
{
	struct hash_map *map;
	void            *cell;
	size_t           current_bucket;
	size_t           next_index;
};


/**
 * @brief Crea e inicializa un nuevo vector
 * 
 * @param element_size Tamaño en memoria de cada elemento
 * @return struct vec Vector inicializado que almacena vectores de tamaño element_size
 */
struct vec vec_new(size_t element_size);

/**
 * @brief Elimina todos los elementos de un vector 
 * 
 * @param vec puntero al vector cuyos elementos se quieren eliminar 
 */
void vec_clear(struct vec *vec);

/**
 * @brief Obtiene el elemento ubicado en un índice dado de un vector
 * 
 * @param vec Puntero al vector en el que se encuentra el elemento
 * @param index Índice del elemento
 * @return void* puntero al vector del cual se quiere obtener el elemento
 */
void *vec_get(struct vec *vec, size_t index);

/**
 * @brief Agrega un espacio a un vector y retorna un puntero al nuevo elemento  
 * 
 * @param vec vector en el cual se quiere realizar un emplace 
 * @return void* puntero al nuevo espacio agregado al vector 
 */
void *vec_emplace(struct vec *vec);

/**
 * @brief Elimina un elemento en el índice dado de un vector
 * 
 * @param vec vector del cual se quiere eliminar el elemento
 * @param index índice del elemento
 */
void vec_delete(struct vec *vec, size_t index);

/**
 * @brief Redimensiona un vector a un nuevo tamaño dado
 * 
 * @param vec Vector a redimensionar
 * @param new_size Nuevo tamaño del vector 
 */
void vec_resize(struct vec *vec, size_t new_size);

/**
 * @brief Crea un nuevo hashmap y retorna el mismo 
 * 
 * @param order Orden del hashmap
 * @param value_size Tamaño de una entrada en el hashmap 
 * @return struct hash_map Hash map creado 
 */
struct hash_map hash_map_new(unsigned order, size_t value_size);

/**
 * @brief Elimina todos los elementos de un hash map dado
 * 
 * @param map puntero al hash map cuyos elementos quieren eliminarse
 */
void hash_map_clear(struct hash_map *map);

/**
 * @brief Obtiene un par llave-valor de un hash map
 * 
 * @param map Hash map del cual se quiere obtener el par llave-valor
 * @param lookup Llave que identifica al para 
 * @return void* Puntero al par llave-valor
 */
void *hash_map_get(struct hash_map *map, int lookup);

/**
 * @brief Agrega un par llave-valor a un hash map
 * 
 * @param map Hash map al que se quiere agregar el registro
 * @param key Llave que identifica el registro
 * @param value Valor del registro
 * @return void* puntero al valor insertado
 */
void *hash_map_put(struct hash_map *map, int key, const void *value);

/**
 * @brief Remueve un registro identificado por una llave de un hash map
 * 
 * @param map Hash map del que se quiere remover el registro
 * @param key llave que identifica el registro
 */
void hash_map_delete(struct hash_map *map, int key);

/**
 * @brief Comienza una iteración sobre un mapa.
 *
 * @param map El mapa sobre el que se iterará.
 * @return struct hash_map_iter Iterador sobre el hash_map
 */
struct hash_map_iter hash_map_iter(struct hash_map *map);

/**
 * @brief Avanza el iterador. Solo es válido llamar a este
 * procedimiento si `iter->cell` no es un puntero nulo.
 *
 * @param iter Iterador a avanzar
 */
void hash_map_iter_next(struct hash_map_iter *iter);

/**
 * @brief Obtiene la llave del elemento actual en el iterador.
 *
 * @return int Llave del elemento actual.
 */
int hash_map_iter_key(struct hash_map_iter *iter);

/**
 * @brief Obtiene un puntero al valor actual del iterador.
 *
 * @return int Valor del elemento actual.
 */
void *hash_map_iter_value(struct hash_map_iter *iter);

#endif
