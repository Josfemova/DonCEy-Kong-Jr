---
title: Instituto Tecnológico de Costa Rica\endgraf\bigskip \endgraf\bigskip\bigskip\
 Tarea Corta 3 - DonCEy Kong Jr \endgraf\bigskip Manual de Usuario \endgraf\bigskip\bigskip
author: 
- José Morales Vargas, carné 2019024269
- Alejandro Soto Chacón, carné 2019008163
date: \bigskip\bigskip\bigskip\bigskip Area Académica de\endgraf Ingeniería en Computadores \endgraf\bigskip\bigskip\ Lenguajes, Compiladores \endgraf e intérpretes (CE3103) \endgraf\bigskip\bigskip Profesor Marco Rivera Meneses \endgraf\vfill  Semestre I
header-includes:
- \setlength\parindent{23pt}
lang: es-ES
papersize: letter
classoption: fleqn
geometry: margin=0in
#fontfamily: sans
fontsize: 11pt
monofont: "Noto Mono"
linestretch: 0.15
...

\maketitle
\thispagestyle{empty}
\clearpage
\tableofcontents
\pagenumbering{roman}
\clearpage
\pagenumbering{arabic}
\setcounter{page}{0}

# Requisitos del sistema

- Cliente con sistema operativo GNU\Linux con X11 como servidor de pantalla
- Servidor con JDK 15 instalado, preferiblemente OpenJDK 
- Memoria RAM disponible: ~100KB para una experiencia agradable

# Uso de Servidor

El servidor consiste de un jar el cual se puede ejecutar tanto por medio de un doble click, como por la forma tradicional usando el comando de java:

```Shell
[usuario@equipo DirectorioEjecutable]$ java -jar donceykongjr-server.jar
```

Al ejecutar el servidor, se abrirá inmediatamente una ventana de administrador:

![](https://raw.githubusercontent.com/Josfemova/DonCEy-Kong-Jr/main/doc/manual/server1.png)

Para obtener información sobre los diferentes comandos disponibles para el administrador, puede ejecutar en la consola de administrador el comando `help`.
Al iniciar, la ventana solo mostrará la dirección actual en la que el servidor está escuchando, y en cuál puerto, información que se puede utilizar para conectar un cliente al servidor

![](https://raw.githubusercontent.com/Josfemova/DonCEy-Kong-Jr/main/doc/manual/server2.png)

Al iniciar un nuevo juego, el servidor comunicará que se ha unido un cliente, y desplegará en pantalla la información sobre los objetos creados al inicializar el juego:

![](https://raw.githubusercontent.com/Josfemova/DonCEy-Kong-Jr/main/doc/manual/server3.png)

A continuación, se desarrolla un poco más sobre cada comando disponible para el usuario administrador.

### Consideraciones previas:

El escenario de juego es de 256x240 px, es decir, la resolución original de la consola NES en la cual salió el videojuego. La posiciones de los diferentes objetos se dan tomando esto en consideración. Si bien el cliente puede renderizar el juego en otras resoluciones, todas son factor de 16:15, es decir, el mismo 256x240.
Como apoyo visual se sugiere abrir un cliente como espectador en el juego que quiere controlar, puesto que esto le permitirá observar el efecto de los comandos impartidos al programa. De ahora en adelante, se asumirá que usted controla el juego precisamente de esta manera, y las imágenes de ejemplos de ejecución de comandos irán acompañadas de una instancia espectador de un juego. 

Se agregó además una funcionalidad al cliente que hace que el mismo imprima la ubicación del mouse relativo a la pantalla de juego cuando se hace click, por lo cual puede utilizar esta funcionalidad para averiguar los valores de "x" y "y" correspondientes a una posición en especial.

### `list-games`

Este comando es relativamente simple. Imprime los juegos en curso y termina

![](https://raw.githubusercontent.com/Josfemova/DonCEy-Kong-Jr/main/doc/manual/list-games.png)

### `list-objects`

Parámetros:

- `game`: Identificador del juego cuyos objetos quiere conocerse

Este comando lista todos los objetos de una pantalla de juego activa. Puede utilizar este comando para encontrar el identificador de un objeto de interés, tal como una plataforma.

![](https://raw.githubusercontent.com/Josfemova/DonCEy-Kong-Jr/main/doc/manual/list-objects.png)

### `highlight`

Parámetros:

- `game`: Identificador del juego que contiene el objeto
- `object`: Identificador del objeto cuyo estado de resalte quiere modificarse
- `yes|no`: Indica si se quiere resaltar o no el objeto

Este es un comando de utilidad, perfecto para saber si por ejemplo, una plataforma conocida realmente posee el identificador que se cree.

![](https://raw.githubusercontent.com/Josfemova/DonCEy-Kong-Jr/main/doc/manual/highlight1.png)

![](https://raw.githubusercontent.com/Josfemova/DonCEy-Kong-Jr/main/doc/manual/highlight2.png)

### `delete`

Parámetros:

- `game`: Identificador del juego que contiene el objeto
- `object`: Identificador del objeto que se desea eliminar

En caso de querer eliminar un objeto creado como una fruta o un cocodrilo, puede utilizarse este comando:

![](https://raw.githubusercontent.com/Josfemova/DonCEy-Kong-Jr/main/doc/manual/delete.png)

### `attach-vines`

Parámetros:

- `game`: Identificador del juego que contiene el objeto
- `Platform`: Identificador de la plataforma en la cual se quiere crear la liana
- `lenght`: Longitud de la liana a ser creada

Dado un bloque plataforma sin lianas por debajo, este comando permite añadir lianas dónde sean necesarias, hasta un límite de 20 cuadros 8x8 (celdas unitarias).

![](https://raw.githubusercontent.com/Josfemova/DonCEy-Kong-Jr/main/doc/manual/attach.png)

### `detach-vines`

Parámetros:

- `game`: Identificador del juego que contiene el objeto
- `Platform`: Identificador de la plataforma en la cual se quieren remover lianas

Dada una plataforma con lianas asociadas, este comando eliminar las lianas atadas a dicha plataforma.

![](https://raw.githubusercontent.com/Josfemova/DonCEy-Kong-Jr/main/doc/manual/detach.png)

### `put-fruit`

Parámetros:

- `game`: Identificador del juego en el que se quiere crear la fruta
- `x`: Posición horizontal de la fruta en un rango de 0 a 256
- `y`: Posición vertical de la fruta en un rango de 0 a 240
- `apple|banana|nispero`: Tipo de fruta que quiere crearse
- `score`: Cantidad de puntos que da la fruta al ser obtenida por el jugador

Comando utilizado para colocar frutas en el campo de juego. Recordar que las posiciones horizontales aumenta hacia la derecha, y las posiciones verticales aumentan hacia abajo.

![](https://raw.githubusercontent.com/Josfemova/DonCEy-Kong-Jr/main/doc/manual/put-fruit.png)

### `put-crocodile`

Parámetros:

- `game`: Identificador del juego que contiene el objeto
- `Platform`: Identificador de la plataforma en la cual se quiere colocar el cocodrilo
- `red|blue`: Tipo de cocodrilo que quiere colocarse

Este comando es utilizado para crear cocodrilos según desee el administrador. Recordar que los cocodrilos rojos necesitan de una plataforma en la que se puedan encontrar lianas para descender. Los cocodrilos azules no poseen esta restricción

![](https://raw.githubusercontent.com/Josfemova/DonCEy-Kong-Jr/main/doc/manual/put-crocodile.png)

# Uso de cliente

El cliente es un ejecutable de línea de comandos. El programa se ejecuta de la siguiente manera:

```Shell
[usuario@equipo DirectorioEjecutable]$ ./donceykongjr [-h|--help] [-f|--fullscreen] <host> <port>
```

Donde los parámetros de la línea de comandos significan lo siguiente:

- `-h|--help`: banderas para solicitar indicaciones de como se usa el programa
- `-f|--fullscreen`: banderas para indicarle al programa que debe ejecutarse en modo pantalla completa 
- `host`: Dirección ip del servidor
- `port`: Puerto en el que se encuentra escuchando el servidor

Suponiendo que el servidor se encuentra en la máquina local, se puede establecer conexión de la siguiente forma:

```Shell
[usuario@equipo DirectorioEjecutable]$ ./donceykongjr localhost 8080
This is client 0
No games are currently running, starting game 0
```

Como se observa anteriormente, de no haber juego activos en el servidor, inmediatamente el cliente reconoce que debe comenzar un nuevo juego, por lo cual realiza la solicitud al servidor e inmediatamente comienza con la ejecución, sin embargo, qué tal si ya hay juegos disponibles?

```Shell
[usuario@equipo DirectorioEjecutable]$ ./donceykongjr localhost 8080
This is client 1
- Game 0 is running

Enter a game ID to watch, or 1 to start a new game
```

Como se puede observar, se le permite al usuario escoger entre dos posibilidades. Puede ser espectador de otra partida, o comenzar la propia. Si el lector ha sido lo suficientemente perspicaz para notarlo, ya se habrá dado cuenta de que para iniciar un juego, el usuario debe ingresar su propio número de cliente. En el servidor cada juego es registrado bajo el identificador del cliente que lo ha comenzado. Esto también significa que el juego 0 pertenece al jugador con ese mismo id. Esto facilita la conexión con el juego deseado, puesto que solo hay que saber a cual jugador se quiere observar para saber en qué juego se encuentra. 

Además, de escribirse un identificador inválido, el juego nos comunicará dicha situación y volverá a solicitar un id.

```Shell
This is client 1
- Game 0 is running

Enter a game ID to watch, or 1 to start a new game
> 3
Error: bad input
Enter a game ID to watch, or 1 to start a new game
> 
```

Una vez dado un identificador válido, comenzará el juego.

## Controles

Las teclas para controlar al avatar del jugador son las siguientes:

- **↑, W**: Si el jugador se encuentra en una liana, sube
- **→, D**: Jugador camina hacia la derecha, o puede soltarse de una liana al presionar dos veces
- **←, A**: Jugador camina hacia la izquierda, o puede soltarse de una liana al presionar dos veces
- **↓, S**: Si jugador se encuentra en una liana, desciende por la misma, o si se encuentra en el final de dicha liana, se suelta
- **ESPACIO**:" Si el jugador se encuentra sobre una superficie, salta. Si se encuentra en una liana, se suelta de la misma.

## Juego

El objetivo del juego es liberar a Donkey Kong la mayor cantidad de veces posible, acumulando la mayor cantidad de puntos posible. 

Para liberar a Donkey Kong, el jugador deberá tomar la llave azul que se ubica cerca de la jaula de Donkey Kong. Una vez tomada dicha llave, si el jugador entra en contacto con Donkey Kong, lo libera. 

Para obtener puntos durante el transcurso del nivel, el jugador debe entrar en contacto con las frutas esparcidas alrededor del juego. Cada fruta le otorgará al cliente una cierta cantidad de puntos. La cantidad de puntos depende de varios factores, entre ellos, si la fruta fue creada por un administrador de servidor. De ser tal el caso, la cantidad de puntos que se obtiene es determinada por el administrador que colocó dicha fruta. 

## Errores al iniciar cliente

### Sobrepaso del máximo de espectadores de una partida

El límite de clientes para cada juego es de 3. Un cliente jugador, y dos clientes espectadores. Si un tercer espectador trata de conectarse a la partida, el juego comunicará un mensaje de error e inmediatamente detendrá ejecución

```Shell
This is client 3
- Game 0 is running

Enter a game ID to watch, or 3 to start a new game
> 0
Error: server failure: no more expectators are allowed for this game
```

### Sobrepaso de máximo de partidas activas

Similar al caso de los espectadores, el servidor solo permitirá que hayan dos partidas de manera simultánea corriendo. Esto significa que al intentar conectarse un cliente como el tercer jugador activo, el cliente comunicará por medio de un mensaje de error que la cantidad de jugadores simultáneos máxima ha sido alcanzada, e inmediatamente detendrá la ejecución.

```Shell
This is client 5
- Game 0 is running
- Game 4 is running

Enter a game ID to watch, or 5 to start a new game
> 5
Error: server failure: the maximum number of active games has been reached
```

### Otros errores

En caso de un problema externo al juego, por ejemplo, un error del sistema del usuario, el cliente maneja estos errores y los comunica según su clasificación. Mientras que algunos errores pueden ser fatales (como errores de conexión física del computador), pueden encontrarse errores menores durante la ejecución que no afectan sustancialmente la jugabilidad, pero se proveen como advertencias de posibles defectos en el sistema del usuario.

Un ejemplo del tipo de errores mencionados es si el Servidor sufre un error y pierde comunicación, el cliente dará el siguiente mensaje

```Shell
The server has closed the connection
```

 y finalizará ejecución.

 Otro ejemplo es el caso en que no hay servidor en la dirección y puertos dados, entonces, el mensaje del cliente será:

```Shell
Fatal error: Connection refused
```

