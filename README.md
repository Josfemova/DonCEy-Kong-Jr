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
- `texture`: Imagen que se dibuja sobre la superficie del sprite.

### Razones

Para expresar velocidades y otros valores relativos, se recurre a razones numéricas. Para esto se requiere de una forma de expresar fracciones matemáticas sin perder información. Dado que un tipo de dato de punto flotante era una opción inadecuada, se creo una estructura distinta que registra una razón entre dos números enteros, llamada `ratio`. Cada `ratio` tiene dos elementos:

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

- Si se registró un evento de tecla, se envía un mensaje al servidor con la información de dicho evento.
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

El servidor es un programa en java compuesto por algunas clases base y 6 paquetes que controlan distintos aspectos de la lógica de juego. 

Primeramente, véase el diagrama para el paquete `resources`:

![](https://raw.githubusercontent.com/Josfemova/DonCEy-Kong-Jr/main/doc/resources.png)

EL paquete cumple una funcionalidad relativamente simple. Es un conjunto de utilidades que proveen la noción al servidor del apartado gráfico del juego, o en resumidas cuentas, es el encargado de administrar lo que concierne a sprites y conjuntos de sprite que conforman una animación, pero no la representación de las entidades a las cuales les corresponden dichos sprites. 

Las entidades son en cambio, representadas utilizando las diferentes clases del paquete `gameobjects`, visto en el siguiente diagrama

![](https://raw.githubusercontent.com/Josfemova/DonCEy-Kong-Jr/main/doc/gameobjects.png)

El paquete de `gameobjects` contiene lo necesario para representar todas las entidades que componen el juego, además de ofrecer algunas facilidades para construcción de escenarios. Como puede observarse, desde el jugador hasta las plataformas mismas son representados como instancias de `GameObject`.

Cada subclase de `GameObject` contiene funcionalidad específica a sí misma, por ejemplo, puede observarse en Platform métodos que facilitan la creación tanto de plataformas de ladrillos como plataformas de pasto, facilitando considerablemente la creación de una escena de juego. 

Para los objetos que puede ser colocados por el usuario administrador, se implementó un patrón de diseño factory, de manera que la creación de frutas y cocodrilos es relativamente transparente al usuario. 

Se implementa además un patrón observer. Este patrón es una forma de permitirle a una clase ser notificada en caso de cambios en el estado interno de una entidad. Esto es principalmente utilizado para el objeto de juego en sí, el cual debe realizar ciertas operaciones si se da un cambio en el estado interno en alguna de todas las entidades que administra.

Según el tipo de `GameObject` se puede tener una observación, ¿Qué sucede con aquellos objetos que tienen estados transitivos, es decir, un objeto que se puede encontrar en varios "modos de operación"?

La respuesta a la pregunta anterior es dada por el paquete `modes`. Este paquete contiene diferentes clases que permiten definir en qué estado se encuentra un `GameObject`. Claramente no todas las entidades tienen estados transitivos, pero las entidades que sí, como el jugador, dependen fundamentalmente de poder diferencias entre estados como caminar, saltar, caer, etc.  

![](https://raw.githubusercontent.com/Josfemova/DonCEy-Kong-Jr/main/doc/modes.png)

Como puede observarse en el diagrama anterior, el paquete `modes` ofrece funcionalidad que le permite a una entidad distinguir entre sus estados, y las implicaciones de las diferencias entre los mismos. Por ejemplo, no todo estado tiene respuestas a un evento en específico. Lo anterior puede inferirse con solo pensar en si debería ser posible saltar mientras ya un jugador se encuentra en el aire. La respuesta a la cuestión anterior es un rotundo no, y por eso mismo puede observarse que a modos como `Falling` y `Jumping` no les concierne controlar que sucede una vez que se llame `onJump()`; simplemente si un jugador se encuentra en alguno de los dos estados anteriores, no puede suceder nada cuando el jugador presiona el botón de saltar.

Es relevante discutir también la forma en la que el servidor es capaz de siquiera procesar un estado como "caer". Para esto, el servidor no solo debe tener una noción de qué hay(`gameobjects`), o cómo luce(`resources`); el servidor requiere de algo que le permita tener una noción de las reglas que rigen el comportamiento y las interacciones de las entidades del juego. Para esto, está el paquete `physics`, el cual se muestra en el siguiente diagrama:

![](https://raw.githubusercontent.com/Josfemova/DonCEy-Kong-Jr/main/doc/physics.png)

Como puede observarse, el paquete anterior funciona como el administrador de función elemental del juego. Las dos tareas más importantes que administra este paquete son velocidades y colisiones.

La primera tarea es esencial, puesto que no puede haber movimiento de una entidad si no se conoce qué distancia se mueve, en qué dirección, y en cuanto tiempo. El movimiento del jugador y cocodrilos depende fundamentalmente de este paquete.

La segunda tarea no debe considerarse menos importante. Las reglas que rigen una interacción de colisión, y los eventos que desencadena una no serían posibles si no fuese por este paquete. Se sabe que el jugador no debe caer al vacío porque una colisión es detectada con el piso, se sabe que puede subir y bajar por una liana porque colisiona con la misma, sabemos que obtiene puntos al entrar en contacto con una fruta porque colisiona con ella. En síntesis, `physics` provee la lógica base que es el fundamento para establecer las reglas que rigen las interacciones entre entidades del juego.

Otra capa fundacional del juego, aunque no relacionada directamente a las entidades del escenario de juego, es el paquete de `networking`.

![](https://raw.githubusercontent.com/Josfemova/DonCEy-Kong-Jr/main/doc/networking.png)

El paquete provee una capa de abstracción sobre el funcionamiento de la conexión con el cliente y lo que respecta a la misma, por ejemplo, como diferenciar entre clientes jugadores y clientes espectadores. Este paquete también provee la funcionalidad que permite notificarle a un cliente sobre los cambios que deben reproducirse en el escenario de juego, puesto que es aquí donde se forman y envían los mensajes en formato JSON que debe interpretar el cliente al momento de correr el videojuego. 

Una vez comprendidos los fundamentos que conforman el juego, se puede proceder a analizar las partes del programa que funcionan sobre estas capas fundacionales.

El primero de los paquetes de mayor nivel es precisamente el paquete `levels`. 

![](https://raw.githubusercontent.com/Josfemova/DonCEy-Kong-Jr/main/doc/levels.png)

Efectivamente el paquete solo es compuesto por una superclase Level, y una implementación de dicha clase para representar el nivel 1. El funcionamiento es relativamente simple, una instancia de `Level` permite dibujar un escenario de juego. Ya que el juego actual consta de un nivel único, solo hay una implementación de dicha superclase, sin embargo, planeando de forma anticipada, se provee una forma simple y fácil de agregar e interactuar con niveles adicionales si esto fuese necesario.

Finalmente, las clases en el directorio base del código del servidor son la última capa de lógica en la funcionalidad del mismo:

![](https://raw.githubusercontent.com/Josfemova/DonCEy-Kong-Jr/main/doc/core.png)

La noción final del servidor es en sí un compuesto entre dos elementos, una aplicación de administración, y un conjunto de juegos activos. Es gracias a las capas inferiores que es posible expresar la lógica general en una forma tan simple, y relativamente transparente al usuario; El sistema visto solo desde esta capa pareciese administrarse por sí solo.

Para evitar posibles conflictos en uso de puertos, entre otros posibles problemas, se recurre a utilizar el patrón de diseño Singleton en la implementación del Servidor. Solo se puede encontrar una instancia de Servidor activa, y no hay forma de crear una instancia adicional que pueda crear problemas, puesto que el constructor de la clase `Server` es privada.

Esta capa de lógica es efectivamente, la capa administrativa. Es por eso que se ubica alojada aquí la clase `Admin`. Una instancia de esta clase habilita una interfaz gráfica para que un administrador de sistema pueda realizar tareas como crear frutas y cocodrilos en el juego, por medio de los comandos indicados en el manual de usuario.

En el caso de game, se aprecia el flujo de inicio de una partida. El mismo constructor nos indica que es iniciado a través de una instancia de un cliente, dicho cliente es registrado como el dueño de la partida, y demás clientes de dicha partida son registrados como espectadores. Ya la lógica base ha sido administrada en capaz anteriores, por lo que a `Game` solo le conciernen cambios de estados mayores, por ejemplo, lo que sucede cuando un jugador pierde o gana, las vidas y puntaje del jugador, la integración de elementos de juego a la escena y las formas en las que un cliente puede interactuar con un juego. Como se mencionó anteriormente, `Game` funcionalmente es solo un administrador general, y solo le concierne controlar que sucede una vez que un elemento ya se ha auto-administrado. Es por eso que esta clase implementa la interface `GameObjectObserver`, puesto que si bien no le conciernen los detalles sobre los cambios de estado de un objeto, sí le conciernen las consecuencias macro que implican dichos cambios de estado. 

## 1.3. Problemas sin solución

No se reportan problemas sin solución.

## 1.4. Actividades realizadas por estudiante

![](https://raw.githubusercontent.com/Josfemova/DonCEy-Kong-Jr/main/doc/plan-actividades.png)

## 1.5. Problemas solucionados

1. Errores de movimiento luego de regresar de un estado de salto
	
	* _Descripción_: En algunos casos, al saltar, el cliente dejaría de reconocer presiones de teclado consecutivas, lo que resultaba en el usuario teniendo que presionar una tecla de movimiento por cada porción de movimiento que quisiera, en vez de ser capaz de tan solo dejar presionado un botón direccional.

	* _Intentos de solución_: 
  	* Originalmente se creyó que el problema estaba asociado a un corte inapropiado de los sprites que componen el avatar del jugador, por lo cual se realizaron ajustes a estos.
  	* A pesar de que la medida anterior resultó en una mejor experiencia de juego, no resolvió el problema en cuestión, solo reducía los casos en los que el problema podía ocurrir. Se hicieron algunos ajustes de velocidades ocasionalmente para notar el efecto que los cambios tenían en el comportamiento del avatar del jugador

	* _Solución encontrada_: EL problema ocurría debido a la forma en la que se procesaban velocidades de movimiento para un objeto cuando se movía verticalmente, lo que ocasionaba ciertos problemas con el movimiento horizontal al mismo tiempo. El ajuste necesario fue agregar lógica adicional para asegurar que las colisiones en las situaciones que ocasionaban problemas se dieran de forma en que no existiesen inconsistencias en las posiciones de los objetos que entraban en colisión. En específico, se da un corrimiento forzoso en el eje vertical de manera que no hayan colisiones verticales inconsistentes. Estas colisiones verticales inconsistentes podían darse en situaciones en las cuales el movimiento de un objeto por cada unidad de tiempo superase la razón 1 pixel por tick.

	* _Conclusiones_:
      - Cuando se tienen entidades interactuando en un ambiente bajo el efecto de ciertas reglas de interacción física, puede surgir inconsistencias inesperadas debido a cantidades de movimiento establecidas en el código.
    * _Recomendaciones_:
      - Considerar por adelantado inconsistencias que pueden surgir al mover elementos gráficos en un ambiente en el cual se toman decisiones según eventos de colisión, puesto que esto puede permitir desarrollar una contramedida antes de tiempo, e incluso diseñar la solución al problema sin necesidad de colocar una contramedida adicional.
	* _Bibliografía_:
      - No hay bibliorafía para la resolución de este problema, el mismo fue resuleto por el equipo sin necesidad de consulta externa.

## 1.6. Conclusiones y Recomendaciones del Proyecto

### Conclusiones

- Si bien el paradigma de orientación a objetos tiene sus ventajas respecto a reutilización de código, también tiene una desventaja consirable que debe tomarse en cuenta a la hora de trabajar bajo el mismo, y es que es propenso a granulizar la lógica de un programa hasta puntos que pueden resultar relativamente imprácticos. Esta característica puede tener como consecuencia una reducción de complejidad aparente, pero aumento de la complejidad real.

- SDL2 se mostró como una biblioteca de gráficos lo suficientemente capaz como para el desarrollo completo del cliente sin necesidad de recurrir a otra biblioteca de apoyo para tareas gráficas. Esta herramienta es simple, pero increíblemente útil.

- El formato JSON sustancialmente útil y preferible para la comunicación entre diferentes agentes en una interacción por medio de internet. El hecho de que este formato sea relativamente simple y estandarizado lo hace fácil de parsear y utilizar, razón principal por la cual se escogió este formato para el desarrollo de la tarea.
  
- Las bibliotecas json-c y json-simple son utilidades simples pero efectivas para el procesamiento y uso de cadenas de caracteres formateadas según un formato JSON.

- La implementación de un patrón Singleton permite evitar problemas que pueden surgir al tener varias instancias de una clase cuyo funcionamiento está diseñado alrededor de ser una clase de instancia única. Es decir, aplicar el patrón Singleton es una buena práctica para declarar de manera explícita que una clase solo debería tener una instancia en todo un programa. 

- EL patrón de diseño observer es de gran utilidad para lograr un efecto de autoadministración del código, ya que en vez de tener que diseñar rutinas que se vean obligadas a buscar cambios de estado de cada objeto que es campo de una clase, el patrón observer es capaz de notificar al observador el origen único del cambio, lo cual resulta en economización de recursos. 

- El patrón de diseño Factory simplifica la creación de objetos y hace el proceso detrás de la misma transparente al usuario, lo que se podría considerar según el contexto como una ventaja deseable en un subsistema.


### Recomendaciones

- Dedicar un tiempo adecuado al planeamiento de un proyecto a desarrollarse bajo el paradigma de orientación a objetos, de manera que se puedan minimizar las desventajas y maximizar las ventajas de trabajar con el mismo.

- Se recomienda utilizar la biblioteca de SDL2 para desarrollo de videojuegos o aplicaciones gráficas similares en el lenguaje de programación C. Si bien la curva de aprendizaje puede ser algo pronunciada, la utilidad de la biblioteca hace que valga la pena dedicarle el tiempo necesario para aprender a utilizarla de una forma apropiada. 

- Si se quiere establecer un protocolo de comunicación entre un cliente y un servidor simple, sencillo y fácil de utilizar, JSON es un formato recomendado para esta tarea. Una comunicación relativamente compleja es fácil de expresar por medio de JSON. 

- Para procesamiento de mensajes en formato JSON: si se trabaja en el lenguaje de programación Java, se recomienda utilizar la biblioteca json-simple. En caso de estar trabajando en el lenguaje de programación C, se recomienda utilizar la biblioteca de json-c.

- Aplicar el patrón de diseño Singleton cuando se considere que una clase debería tener una única instancia durante la ejecución de un programa.

- Aplicar el patrón de diseño Observer cuando se vea que un objeto necesita reaccionar al cambio de estado de algún otro objeto, y buscar formas alternativas de informar al primero sobre el cambio del segundo sea impráctico o desperdicie recursos.

- Utilizar el patrón Factory cuando se requiera esconder detalles de implementación sobre la creación de una colección de objetos, pero igual se le quiere habilitar al usuario una forma simple de crear dichos objetos. 

## 1.8. Bibliografía

::: {#refs}
:::

# 2. Bitácoras

## *José Morales*

### 9 de abril

- Se realizó una reunión de coordinación inicial para tomar decisiones sobre la organización inicial de la tarea y las fechas en que ambos integrantes estaríamos más disponibles para trabajar en la asignación.
- Se creo el repositorio para almacenar el proyecto y se agregaron los archivos base necesarios para orientarse en el principio.

### 12 de abril

- Se investigó sobre las posibles bibliotecas a utilizar para desarrollar el lado del cliente del proyecto. SDL2 parece ser la mejor opción por enfoque y tiempo de desarollo de la tarea.
- Se realizó una segunda reunión para tomar decisiones más específicas respecto a la ejecución de las tareas asociadas con el proyecto. También se tomaron decisiones respecto a las herramientas a utilizar para el desarollo, sin embargo, es necesario confirmar con el profesor si se puede proceder con el plan que teníamos pensando, especialmente por la posibilidad de incompatibilidad entre plataformas, lo que puede causar varios problemas.
- Se agregaron algunos archivos base para la documentación de la tarea.
- Se comenzó el planeamiento de la estructura para el la sección del servidor en java. 

### 13 de abril

- Se confeccionó parte del diagrama UML primitivo del proyecto.

### 14 de abril

- Se completo el diagrama UML primitivo del proyecto.
- Se discutió con el compañero Alejandro las consideraciones a tomar en cuenta para la interacción entre cliente y servidor, incluído el protocolo de comunicación.
- Se resolvieron las dudas con el profesor respecto a viabilidad de uso de la biblioteca SDL2, compatibilidad multiplataforma y sistemas de compilación de proyecto. 
- Se planeó una reunión, aunque la misma fue cancelada dado que el motivo de la misma era para adptar los planes a los cambios que pudieran surgir a partir de la resolución de las dudas. Dado que las soluciones a las dudas eran favorables y permitían continuar sin perturbaciones, dicha reunión se canceló.

### 16 de abril 

- Se iniciaron implementaciones de algunas clases relacionadas a la representación del estado de juego del lado del servidor.

### 17 de abril

- Se obtuvieron y cortaron los diferentes sprites necesarios para poder programar el juego
- Se completó la implementación de las clases relacionadas a representación de un estado de juego en el Servidor y se agregaron algunas clases que aprovechan mejor las caractaerísticas de la programación orientada a objetos, entre ellas el uso de patrones como singleton y Factory.
- Se realizaron algunas tareas relacionadas a documentación y registro de los planeamientos realizados puesto que si bien ya habían sido discutidos en el grupo, faltaba una formalización de algunas cosas para integración en la documentación externa. 

### 18 de abril

- Se agregó una implementación de lista enlazada al lado del Servidor java.
- Se agregó código para detección de colisiones entre dos objetos de juego. 
- Se modificaron algunas implementaciones de elementos para representar objetos del juego de manera que si se diera un cambio de texturas o algo similar, se más fácil cambiar los valores utilizados para la detección de colisiones en el servidor.
- Se agregó lógica primitiva de conexión al servidor.

### 19 de abril

- Se agregaron encabezados a documentos de documentación técnica ejecutiva y manual de usuario. 
- Se agregaron las secciones iniciales al documento README.md en donde se almacena la documentación externa del proyecto.

### 20 de abril

- Se realizaron cambios menores a algunas clases para registrar información de sprites y manejar cambios de estado con onTick()

### 21 de abril

- Se extrajeron texturas adicionales para el dibujo de la pantalla de juego.
- Se agregó rutina prototipo para dibujado para la primera pantalla de juego. Sujeta a cambios, solo es una prueba de concepto inicial.

### 22 de abril

- Se realizó una reunión con el compañero de trabajo para determinar términos operativos de la fase final de la realización del proyecto. Dado el avance acumulado hasta el momento y las estructura del proyecto, se determinó que la mejor estrategia para continuar en la fase final es tener un miembro dedicado a completar la lógica de juego y que otro miembro se dedique a completar la documentación requerida, esto porque el entrelazado entre cliente y servidor en la lógica de juego puede resultar un causante de conflictos a la hora de trabajar en el código de manera paralela. Mi persona toma entonces la tarea de asegurarse que todo lo relacionado a documentación se encuentre en condición y completitud adecuada.
- Se agregó la documentación interna correspondiente a la lógica del cliente

### 23 de abril

- Se comenzó con la modificación del diagrama UML prototipo
- Se agregó mayor documentación interna del cliente
- Se agregaron archivos de bibliografía de recursos consultados hasta ahora

### 24 de abril

- Se agregan archivos de bibliografía al proyecto.
- Se agregó documentación de algunas funciones del cliente.

### 25 de abril

- Se realizan pruebas de funcionalidad.
- Se agregan sprites nuevos a los assets del proyecto.
- Se finaliza la rutina para renderizar el primer nivel del juego.
- Se finaliza el diagrama de funcionamiento del cliente y se agrega a la documentación externa.

### 26 de abril

- Se finaliza diagrama UML del Servidor.
- Se agrega guía para uso de cliente al manual de usuario.
- Se agregan secciones referentes a algoritmos y estructuras utilizadas a documentación externa.
- Se agregan imágenes de diagramas y plan de actividades a la documentación externa.
- Se agrega documentación interna final del servidor.
- Se realizan otras pruebas de calidad para terminar de pulir el cliente.
- Se finaliza el manual de usuario.
- Se agregan recomendaciones y conclusiones al proyecto
- Se confecciona presentación para defensa de proyecto

## *Alejandro Soto*

### 9 de abril

- Se publica la especificación del trabajo.
- Se forman grupos de trabajo.
- Se realiza una sesión de discusión inicial en la que se cubren aspectos operativos principales para la realización del trabajo. Se llega a la conclusión de que posterior a esta primera reunión, debe realizarse otra para definir con mayor detalle los aspectos operativos para realización de la tarea.

### 12 de abril

- Se realiza la segunda reunión estipulada anteriormente y se define un plan de actividades a seguir para la realización del trabajo. Se toma en cuenta la disponibilidad de cada integrante para definir tiempos de trabajo que se ajusten a la disponibilidad de cada integrantes.

### 14 de abril 

- Compañero de equipo propone un modelo primitivo de la estructura del servidor.
- Se discuten los detalles de implementación de la conexión cliente-servidor con el compañero, incluído el formato para los mensajes a ser enviados.
- Se decide utilizar un formato JSON para los mensajes entre cliente y servidor. Se definen comandos principales a ser enviados y los campos esperados para cada mensaje.
- Se resolvieron consultas sobre la construcción del proyecto y la defensa para poder tomar una decisión informada respecto a bibliotecas y ddemás herramientas a utilizar para el desarrollo del proyecto. Dadas las respuestas obtenidas, se decide trabajar en sistema operativo GNU/Linux, usar cmake para construír cliente y servidor, y utilizar la biblioteca SDL2 para el renderizado del juego en pantalla del cliente. 
- Inicialmente se planeó un encuentro con el compañero de trabajo para resolver posibles conflictos de planeamiento que pudiesen surgir en la resolución de las dudas planteadas al profesor, sin embargo dado que la resolución de dichas dudas no causaba ningún conflicto con el plan original, se canceló dicha reunión.

### 17 de abril

- Se agregó estructura de proyecto nueva.
- Se configuró adecuadamente el .gitignore.
- Se configuró el sistema de construcción del proyecto.
- Se agregaron puntos de entrada para el cliente y el servidor.
- Se agregó la dependencia a json-c.

### 18 de abril

- Se agregaron los sprites cortados al proyecto.
- Se realiza una implementación de vector en C para almacenamiento de colecciones varias.
- Se realiza una implementación de hash map en C para almacenamiento de datos relacionados al juego(como las entidades).
- Se implementa lógica de conexión con servidor por medio de sockets.
- Se implementan funciones para extraer la información de los mensaje provenientes del servidor.
- Se implementa lógica de dibujo de pantalla en el cliente.
- Se agrega lógica de rutina inicial del cliente.

### 19 de abril

- Se agregó capacidad para interpretar parámetros de línea de comandos del lado del cliente.
- Se agregó lógica al cliente para permitir un modo de pantalla completa. 

### 20 de abril

- Se corrigieron detalles de implementación del modo de pantalla completa. 

### 22 de abril

- Se reestructuró el servidor de forma que se ajuste mejor a las necesidades del proyecto.
- Se modificó la forma en que las clases que representan entidades en el cliente guardan información sobre posición y otros datos asociados. 
- Se modificó la lógica de conexión del servidor con el cliente para tener una mayor concordancia entre ambos.
- Se agregaron algunas funciones de manejo de envíos de comandos  para favorecer legibilidad del código.
- Se favorece la implementación por defecto de un ArrayList para guardar colecciones varias.
- Se agregan lógica al cliente para la lógica de animación de sprites y control de tiempo.

### 23 de abril

- Se termina implementación de movimiento del lado del cliente.
- Se corrige un error menor del divisor de reloj.
- Se agrega detección de WASD y tecla espacio como entradas válidas de usuario.
- Se agrega lógica de movimiento al servidor.

### 24 de abril

- Se actualizaron algunos archivos de documentación.
- Se agregó lógica adicional a la conexión con el cliente.

### 25 de abril

- Se agregaron las modificaciones de conexión al repositorio.
- Para una mejor experiencia de usuario, se actualizan los sprites correspondientes a lianas y se modifica la forma en la que se centra la imagen.
- Se habilitan los saltos del persona del jugador en dirección opuesta a una liana en la que se encuentra el jugador.
- Se corrigen algunos errores relacionados al salto desde una liana.
- Tomando los recursos disponibles en línea sobre el juego original, se nota que los cuadros de tierra no son relevantes para la detección de colisiones, por lo que se modifica la lógica para clasificar a los mismos solo como adornos flotantes.
- Se mejoró la lógica de procesamiento de algunas colisiones.
- Una vez considerado que el cliente no recibirá modificaciones sustanciales mayores en lógica, se decide seccionar el mismo en distintos archivos para facilitar la legibilidad del código.
- Se implementan cocodrilos rojos y sus animaciones.
- Se implementan agregan indicadores gráficos sobre estado de juego.
- Después de discutirlo con el compañero de trabajo, se resuelve una nueva manera de dibujar las lianas de forma que sea más consistente e igualmente visualmente agradable para el usuario. Dada una instrucción de crear una liana, se decide de forma aleatoria cuales cuadros intermedios de la liana deberán ser una liana con hojas o sin hojas.
- Se agregan limitadores de partidas y espectadores máximos.

### 26 de abril

- Se implementan comandos de control para administrador de servidor.
- Se finaliza aplicación de administración de juegos.
- Se agrega la lógica asociada a la llave para abrir la jaula de DonkeyKong.
- Se implementan condiciones de gane.
- Se implementa lógica de aumento de dificultad cuanto se finaliza un nivel.
- Se modifica el manejo de colisiones con los extremos de plataformas de pasto para mejorar calidad de experiencia de usuario.
- Se agrega salida de consola del servidor que informa sobre los diferentes eventos y cambios en los escenario de juego y conexiones.
- Se agrega al entity de Mario al escenario del nivel 1.
- Se resuelven errores de movimiento debidos a inconsistencia de colisiones.
- Se retocan algunos valores para mejorar la experiencia de usuario.

