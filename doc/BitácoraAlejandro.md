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
- Se implementa lógica de aumento de dificultad cuanto se finalia un nivel.
- Se modifica el manejo de colisiones con los extremos de plataformas de pasto para mejorar calidad de experiencia de usuario.
- Se agrega salida de consola del servidor que informa sobre los diferentes eventos y cambios en los escenario de juego y conexiones.
- Se agrega al entity de Mario al escenario del nivel 1.
- Se resuelven errores de movimiento debidos a inconsistencia de colisiones.
- Se retocan algunos valores para mejorar la experiencia de usuario.
