#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <assert.h>

#include "util.h"
#include "constants.h"

/**
 * @brief Crea e inicializa un nuevo vector
 * 
 * @param element_size Tamaño en memoria de cada elemento
 * @return struct vec Vector inicializado que almacena vectores de tamaño element_size
 */
struct vec vec_new(size_t element_size)
{
	struct vec empty =
	{
		.data         = NULL,
		.length       = 0,
		.capacity     = 0,
		.element_size = element_size,
	};

	return empty;
}

/**
 * @brief Elimina todos los elementos de un vector 
 * 
 * @param vec puntero al vector cuyos elementos se quieren eliminar 
 */
void vec_clear(struct vec *vec)
{
	free(vec->data);

	vec->data = NULL;
	vec->length = vec->capacity = 0;
}

/**
 * @brief Obtiene el elemento ubicado en un índice dado de un vector
 * 
 * @param vec Puntero al vector en el que se encuentra el elemento
 * @param index Índice del elemento
 * @return void* puntero al vector del cual se quiere obtener el elemento
 */
void *vec_get(struct vec *vec, size_t index)
{
	return (char*)vec->data + vec->element_size * index;
}

/**
 * @brief Expande la capacidad máxima de un vector
 * 
 * @param vec Vector cuya capacidad quiere aumentarse
 * @param required Capacidad a la que se requiere expandir el vector 
 */
static void vec_require_capacity(struct vec *vec, size_t required)
{
	if(required > vec->capacity)
	{
		do
		{
			// Duplica la capacidad
			vec->capacity = vec->capacity > 0 ? VEC_CAPACITY_FACTOR * vec->capacity : DEFAULT_VEC_CAPACITY;
		} while(required > vec->capacity);

		vec->data = realloc(vec->data, vec->element_size * vec->capacity);
		if(!vec->data)
		{
			perror("realloc");
			abort();
		}
	}
}

/**
 * @brief Agrega un espacio a un vector y retorna un puntero al nuevo elemento  
 * 
 * @param vec vector en el cual se quiere realizar un emplace 
 * @return void* puntero al nuevo espacio agregado al vector 
 */
void *vec_emplace(struct vec *vec)
{
	// Se extiende y luego se obtiene un puntero al último
	vec_require_capacity(vec, vec->length + 1);
	return (char*)vec->data + vec->length++ * vec->element_size;
}

/**
 * @brief Elimina un elemento en el índice dado de un vector
 * 
 * @param vec vector del cual se quiere eliminar el elemento
 * @param index índice del elemento
 */
void vec_delete(struct vec *vec, size_t index)
{
	assert(index < vec->length); //verifica que el índice sea válido

	void *target = (char*)vec->data + vec->element_size * index;
	void *source = (char*)target + vec->element_size;

	// Se corren los elementos que quedaron por encima del eliminado
	memmove(target, source, (vec->length-- - index - 1) * vec->element_size);
}

/**
 * @brief Redimensiona un vector a un nuevo tamaño dado
 * 
 * @param vec Vector a redimensionar
 * @param new_size Nuevo tamaño del vector 
 */
void vec_resize(struct vec *vec, size_t new_size)
{
	vec_require_capacity(vec, new_size);
	if(new_size > vec->length)
	{
		memset(vec_get(vec, vec->length), 0, vec->element_size * (new_size - vec->length));
	}

	vec->length = new_size;
}
