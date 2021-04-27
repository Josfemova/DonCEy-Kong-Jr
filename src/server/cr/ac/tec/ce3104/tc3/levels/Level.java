package cr.ac.tec.ce3104.tc3.levels;

import cr.ac.tec.ce3104.tc3.Game;
import cr.ac.tec.ce3104.tc3.physics.Size;
import cr.ac.tec.ce3104.tc3.gameobjects.PlayerAvatar;

public interface Level {
    /**
     * Obtiene el tamaño del area de juego de referencia (resolución virtual del juego)
     * @return resolución virtual del juego
     */
    Size getGameAreaSize();

    /**
     * Realiza las operaciones necesarias para dibujar el nivel sobre un juego dado
     * @param game juego sobre el cual se quiere dibujar el nivel
     * @param initialScore puntaje inicial con el cual se comienza el nivel
     * @return Referencia a la entidad que representa al jugador 
     */
    PlayerAvatar setup(Game game, Integer initialScore);
}
