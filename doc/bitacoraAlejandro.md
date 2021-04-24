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
- Se resolvieron consultas sobre la construcción del proyecto y la defensa para poder tomar una decisión informada respecto a bibliotecas y ddemás herramientas a utilizar para el desarrollo del proyecto. Dadas las respuestas obtenidas, se decide trabajar en sistema operativo GNU\Linux, usar cmake para construír cliente y servidor, y utilizar la biblioteca SDL2 para el renderizado del juego en pantalla del cliente. 
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
