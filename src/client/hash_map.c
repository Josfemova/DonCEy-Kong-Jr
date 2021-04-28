#include <stdlib.h>
#include <assert.h>
#include <string.h>

#include "util.h"
#include "constants.h"

/**
 * @brief Busca por un par llave-valor en un bucket dado
 * 
 * @param bucket Puntero al bucket del hash map en que se quiere buscar el par
 * @param lookup Llave que identifica el par buscado
 * @return void* Puntero al par si el mismo es encontrado en el bucket, NULL de lo contrario
 */
static void *bucket_get_pair(struct vec *bucket, int lookup)
{
	if(bucket)
	{
		// Se busca linealmente
		for(size_t i = 0; i < bucket->length; ++i)
		{
			char *pair = vec_get(bucket, i); //Obtiene elemento en índice i del vector bucket
			int *key = (int*)pair;

			if(lookup == *key)
			{
				return pair;
			}
		}
	}

	return NULL;
}

/**
 * @brief Crea un nuevo hashmap y retorna el mismo 
 * 
 * @param order Orden del hashmap
 * @param value_size Tamaño de una entrada en el hashmap 
 * @return struct hash_map Hash map creado 
 */
struct hash_map hash_map_new(unsigned order, size_t value_size)
{
	assert(order > 0); //orden no puede ser nulo

	struct hash_map empty =
	{
		.buckets    = vec_new(sizeof(struct vec)),
		.order      = order,
		.value_size = value_size
	};

	return empty;
}

/**
 * @brief Elimina todos los elementos de un hash map dado
 * 
 * @param map puntero al hash map cuyos elementos quieren eliminarse
 */
void hash_map_clear(struct hash_map *map)
{
	for(size_t i = 0; i < map->buckets.length; ++i)
	{
		vec_clear(vec_get(&map->buckets, i));
	}

	vec_clear(&map->buckets);
}

/**
 * @brief Obtiene el tamaño de celda de un elemento de hash map
 *
 * @param map Mapa cuyo tamaño de celda quiere obtenerse
 */
static size_t hash_map_cell_size(struct hash_map *map)
{
	// Máximo entre tamaño de llave (sizeof(int)) y tamaño de valor, para garantizar alineamiento
	return map->value_size > sizeof(int) ? map->value_size : sizeof(int);
}

/**
 * @brief Obtiene el bucket que contiene el registro con la llave dada
 * 
 * @param map Hash map en el que se encuentra el bucket
 * @param key Llave del registro a buscar
 * @return struct vec* bucket que contiene el registro identificado por la llave, o NULL si el registro no existe
 */
static struct vec *hash_map_bucket_for(struct hash_map *map, int key)
{
	// No hay memoria reservada
	if(!map->buckets.data)
	{
		return NULL;
	}

	/* `((1 << order) - 1)` es una potencia de dos menos uso: `2 ** order - 1`
	 * Entonces, esto toma los bits indicados por el orden.
	 */
	return vec_get(&map->buckets, (unsigned)key & ((1u << map->order) - 1));
}

/**
 * @brief Obtiene un par llave-valor de un hash map
 * 
 * @param map Hash map del cual se quiere obtener el par llave-valor
 * @param lookup Llave que identifica al para 
 * @return void* Puntero al par llave-valor
 */
void *hash_map_get(struct hash_map *map, int lookup)
{
	struct vec *bucket = hash_map_bucket_for(map, lookup);
	char *pair = bucket_get_pair(bucket, lookup);

	return pair ? pair + hash_map_cell_size(map) : NULL;
}

/**
 * @brief Agrega un par llave-valor a un hash map
 * 
 * @param map Hash map al que se quiere agregar el registro
 * @param key Llave que identifica el registro
 * @param value Valor del registro
 * @return void* puntero al valor insertado
 */
void *hash_map_put(struct hash_map *map, int key, const void *value)
{
	// Se inicializan los buckets de no haber sido ya
	if(!map->buckets.data)
	{
		vec_resize(&map->buckets, 1lu << map->order);

		struct vec empty_bucket = vec_new(HASH_MAP_CELLS_PER_ITEM * hash_map_cell_size(map));
		for(size_t i = 0; i < 1lu << map->order; ++i)
		{
			*((struct vec*)vec_get(&map->buckets, i)) = empty_bucket;
		}
	}

	struct vec *bucket = hash_map_bucket_for(map, key);
	char *pair = bucket_get_pair(bucket, key);

	// Si el elemento no estaba ya, se inserta
	if(!pair)
	{
		pair = vec_emplace(bucket);
	}

	int *stored_key = (int*)pair;
	void *stored_value = pair + hash_map_cell_size(map);

	// Se sobreescribe, haya estado antes o no
	*stored_key = key;
	return memcpy(stored_value, value, map->value_size);
}

/**
 * @brief Remueve un registro identificado por una llave de un hash map
 * 
 * @param map Hash map del que se quiere remover el registro
 * @param key llave que identifica el registro
 */
void hash_map_delete(struct hash_map *map, int key)
{
	struct vec *bucket = hash_map_bucket_for(map, key);
	char *pair = bucket_get_pair(bucket, key);

	if(pair)
	{
		// El segundo argumento es un índice
		vec_delete(bucket, (pair - (char*)bucket->data) / bucket->element_size);
	}
}

/**
 * @brief Comienza una iteración sobre un mapa.
 *
 * @param map El mapa sobre el que se iterará.
 * @return struct hash_map_iter Iterador sobre el hash_map
 */
struct hash_map_iter hash_map_iter(struct hash_map *map)
{
	struct hash_map_iter iter =
	{
		.map            = map,
		.cell           = NULL,
		.current_bucket = 0,
		.next_index     = 0
	};

	hash_map_iter_next(&iter);
	return iter;
}

/**
 * @brief Avanza el iterador. Solo es válido llamar a este
 * procedimiento si `iter->cell` no es un puntero nulo.
 *
 * @param iter Iterador a avanzar
 */
void hash_map_iter_next(struct hash_map_iter *iter)
{
	for(; iter->current_bucket < iter->map->buckets.length; ++iter->current_bucket)
	{
		struct vec *bucket = vec_get(&iter->map->buckets, iter->current_bucket);
		if(iter->next_index < bucket->length)
		{
			// Se encontró una siguiente celda en la iteración
			iter->cell = vec_get(bucket, iter->next_index++);
			return;
		}

		// El bucket actual ya no tiene más elementos para iterar
		iter->next_index = 0;
	}

	// Si no se encontró, la iteración ha terminado
	iter->cell = NULL;
}

/**
 * @brief Obtiene la llave del elemento actual en el iterador.
 *
 * @return int Llave del elemento actual.
 */
int hash_map_iter_key(struct hash_map_iter *iter)
{
	return *(int*)iter->cell;
}

/**
 * @brief Obtiene un puntero al valor actual del iterador.
 *
 * @return int Valor del elemento actual.
 */
void *hash_map_iter_value(struct hash_map_iter *iter)
{
	return (char*)iter->cell + hash_map_cell_size(iter->map);
}
