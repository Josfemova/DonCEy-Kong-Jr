#include <stdlib.h>
#include <assert.h>
#include <string.h>

#include "util.h"

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
	if(!map->buckets.data)
	{
		return NULL;
	}

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
	if(!map->buckets.data)
	{
		vec_resize(&map->buckets, 1lu << map->order);

		struct vec empty_bucket = vec_new(2 * hash_map_cell_size(map));
		for(size_t i = 0; i < 1lu << map->order; ++i)
		{
			*((struct vec*)vec_get(&map->buckets, i)) = empty_bucket;
		}
	}

	struct vec *bucket = hash_map_bucket_for(map, key);
	char *pair = bucket_get_pair(bucket, key);

	if(!pair)
	{
		pair = vec_emplace(bucket);
	}

	int *stored_key = (int*)pair;
	void *stored_value = pair + hash_map_cell_size(map);

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
		vec_delete(bucket, (pair - (char*)bucket->data) / bucket->element_size);
	}
}

/**
 * @brief Obtiene un valor en el índice dado de un bucket de un hash map
 * 
 * @param map Hash map en el que se encuentra el valor
 * @param bucket Bucket del hash map en el que se encuentra el valor
 * @param index Índice en el bucket del valor que se quiere obtener
 * @return void* puntero al valor obtenido
 */
void *bucket_get_value(struct hash_map *map, struct vec *bucket, size_t index)
{
	return (char*)vec_get(bucket, index) + hash_map_cell_size(map);
}
