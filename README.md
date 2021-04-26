---
title: Instituto Tecnológico de Costa Rica\endgraf\bigskip \endgraf\bigskip\bigskip\
 Tarea Corta 3 - DonCEy Kong Jr \endgraf\bigskip\bigskip\bigskip\bigskip
author: 
- José Morales Vargas, carné 2019024270
- Alejandro Soto Chacón, carné 2019008164
date: \bigskip\bigskip\bigskip\bigskip Area Académica de\endgraf Ingeniería en Computadores \endgraf\bigskip\bigskip\ Lenguajes, Compiladores \endgraf e intérpretes (CE3104) \endgraf\bigskip\bigskip Profesor Marco Rivera Meneses \endgraf\vfill  Semestre I
header-includes:
- \setlength\parindent{24pt}
- \usepackage{url}
lang: es-ES
papersize: letter
classoption: fleqn
geometry: margin=1in
#fontfamily: sans
fontsize: 12pt
monofont: "Noto Sans Mono"
linestretch: 1.15
bibliography: bibliografia.bib
csl: /home/josfemova/UsefulRepos/styles/apa.csl
nocite: |
    @perror, @getline, @poll, @read, @getaddrinfo, @ip, @socket, @connect, @getopt, @timerfd, @x11, @sdlwiki, @booklet, @swing
...

\maketitle
\thispagestyle{empty}
\clearpage
\tableofcontents
\pagenumbering{roman}
\clearpage
\pagenumbering{arabic}
\setcounter{page}{1}

# DonCEy Kong Jr

## 1.1. Descripción de las estructuras de datos desarrolladas

Debe considerarse que entre las estructuras a listar solo se especificaran las del cliente en esta sección. Esto debido a que las estructuras en el servidor son mejor descritas por la relaciones entre clases, puesto que cada clase es en sí una estructura autocontenida. Las relaciones entre clases y qué representan es mejor cubierto en la sección **1.2** de este documento. La razón detrás de esta decisión es que sería redundante colocar los diagramas de clase en dos secciones distintas y repetir la misma explicación de las relaciones entre clases.

### Vector

Un vector es un arreglo dinámico en el cual cada uno de sus elementos se encuentran contiguos en memoria. En el cliente se implementa un vector capaz de almacenar elementos genéricos. Este vector es utilizado como pieza fundamental en la construcción de otras estructuras de datos a ser descritas en las secciones posteriores. En la implementación, un vector es dado como un struct llamado `vec`, el cual contiene los siguientes campos:

- `data`: Es un puntero que indica el bloque de memoria en el que comienzan los datos del vector
- `lenght`: Indica cuantos elementos contiene el vector
- `capacity`: Indica la capacidad actual del vector
- `element_size`: Indica el tamaño en memoria que se requiere reservar para cada elemento del vector

### hash map

Un hash map es una estructura de datos en la cual se mapean llaves a cierto valores. Un hashmap está compuesto por un vector de buckets, los cuales al mismo tiempo contienen un vector en el cual se almacenan las entradas mapeadas a cada bucket. La ventaja de esta estructura de datos es que el tiempo de acceso a cada elemento es relativamente constante, por lo cual se puede asegurar cierta consistencia de velocidad a la hora de buscar un elemento en la estructura.

En el servidor, se utiliza un hash map para el control de diferentes datos, por ejemplo, las entidades y los sprites. Estos mapas rara vez se encuentran solos, más bien suelen ser uno de los campos de alguna otra estructura superior, entre ellas la estructura con la cual se representa el juego en el cliente.

El hash map es representado pro un struct `hash_map` el cual contiene los campos:

- `buckets`: Es un vector que contiene los distintos buckets que componen el mapa
- `order`: Indica el orden del mapa. Se utiliza para el mapeo de valores
- `value_size` :Indica el tamaño en memoria que se debe reservar para cada valor a almacenar en el mapa

### Juego

Un juego en el cliente se representa como un struct el cual contiene los campos:

- `state`: Valor de estado
- `net_fd`: Identificador de archivo descriptor de la conexión
- `x11_fd`: Identificador de archivo descriptor de pantalla
- `timer_fd`: Identificador de archivo descriptor de timer
- `net_file`: Stream del socket
- `window`: Ventana de SDL
- `renderer`: Renderizador de SDL
- `ticks`: unidades de tiempo transcurridas
- `sprites`: hash map de sprites del juego
- `entities`: hash map de entidades del juego
- `fullscreen`: valor que indica si la pantalla se debe dibujar como pantalla completa  

### Sprites

Un sprite del está conformado por dos elementos:

- `surface`: Conjunto de pixeles(superficie) en los que se dibuja el sprite
- `texture`: Imagen que se dibuja sobre la superficie del sprite

### Razones

Para expresar velocidades y otros valores relavos, se recurre a razones numéricas. Para esto se requiere de una forma de expresar fracciones matemáticas sin perder información. Dado que un tipo de dato de punto flotante era una opción inadecuada, se creo una estructura distinta que registra una razón entre dos números enteros, llamada `ratio`. Cada `ratio` tiene dos elementos:

* `numerator`: Numerador de la fracción
* `denominator`: Denominador de la fracción

### Entidades

Para representar a los distintos tipos de elementos gráficos que se dibujan era necesario una estructura que contuviese todo lo necesario para el manejo y manipulación de las distintas entidades. Para esto se recurrió a crear un struct `entity`, el cual contiene información de posición, velocidad, y algunos otros datos de estado. Los campos de dicho struct son

* `id`: Identificador de la entidad
* `x`: Posición horizontal de la entidad
* `y`: Posición vertical de la entidad
* `sequence`: Vector que contiene secuencia de sprites de animación del estado actual
* `next_sprite`: Siguiente sprite para animar a la entidad
* `speed_x`: velocidad horizontal expresada como una razón entre movimiento/ticks
* `speed_y`: velocidad vertical expresada como una razón entre movimiento/ticks

### Par llave-valor

Dentro del cliente se hace uso del formato JSON para comunicación con el servidor. Esto hacia necesario el desarrollo de una estructura relativamente simple que sirviese para la descomposición de objetos JSON. Para esto se agregó un struct `key_value` el cual contiene los campos:

- `key`: String que identifica la llave del objeto json
- `value`: Valor que puede ser un valor en sí, o un objeto JSON anidado


## 1.2. Descripción detallada de algoritmos desarrollados

### Cliente 

![](https://raw.githubusercontent.com/Josfemova/DonCEy-Kong-Jr/main/doc/DonCEy-Kong-Jr-C.png)

Como se puede observar, el cliente tiene una funcionalidad relativamente simple en cuanto a relaciones entre componentes. Esto se debe a que, tal como se específico, el cliente solo es un intérprete de los comandos enviados por el servidor, ya que el servidor es quien maneja la lógica de juego en sí. El cliente no tiene noción ni siquiera de las colisiones, sino que su trabajo se centra en dibujar y captar acciones del usuario.

La rutina de inicio consiste en inicializar la biblioteca de gráficos y la conexión con el servidor, y terminadas estas dos tareas, comenzar con el ciclo de juego. 

Cada ciclo de juego se dan los mismos pasos:

- Si se registró un evento de tecla, se envía un mensaje al servidor con la infromación de dicho evento.
- Se lee el stream del socket y según la entrada se sigue uno de tres flujos:
  - Iniciar como jugador o espectador: Envía un mensaje de handshake al servidor para comunicarle como el jugador quiere comenzar su instancia cliente.
  - Post handshake: Crea la pantalla, inicializa el timer de juego y carga los recursos gráficos.
  - Manejar comandos: Cuando ya el juego está activo, lee del socket los comandos que debe ejecutar localmente, tal crear entidades, dibujar objetos, entre otras operaciones posibles. 
- Si el juego se encuentra en un estado en que debe refrescar la pantalla, la redibuja con `redraw()`

En el manejo de comandos, descifrado de instrucciones y control del juego, el cliente hace uso de tres categorías distintas de utilidades. 

1. Utilidades manejo de JSON: Utilizadas para obtener información de los mensaje enviados por el servidor.
2. Utilidades de manejo de vectores: En varias secciones del programa se hace uso de vectores para diferentes propósitos. Esta funcionalidad es de utilidad general.
3. Utilidades de manejo de hash maps: Los elementos que componen la pantalla de juego y algunos otros son registrados por medio del uso de hash maps tal como se desarrollo en la sección 1.1. Estas funciones son utilizadas para la consulta y manipulación de estos hash maps. 

### Servidor

![](https://raw.githubusercontent.com/Josfemova/DonCEy-Kong-Jr/main/doc/plan-actividades.png)

## 1.3. Problemas sin solución

## 1.4. Actividades realizadas por estudiante

![]()

## 1.5. Problemas solucionados

1. Pathfinding omite el ubicación inicial
	
	* _Descripción_: 

	* _Intentos de solución_: 

	* _Solución encontrada_: 

	* _Conclusiones_:
      - x
      - y
    * _Recomendaciones_:
      - x
      - y
	* _Bibliografía_:
      - x
      - y
      - z

## 1.6. Conclusiones y Recomendaciones del Proyecto

### Conclusiones

### Recomendaciones

## 1.8. Bibliografía

::: {#refs}
:::

# 2. Bitácoras

## *José Morales*

## *Alejandro Soto*