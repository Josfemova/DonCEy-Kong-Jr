package cr.ac.tec.ce3104.tc3.gameobjects;

import cr.ac.tec.ce3104.tc3.physics.Position;
import cr.ac.tec.ce3104.tc3.resources.Sprite;

// Un níspero
public class Nispero extends Fruit {
    /**
     * Crea un nispero dada una posicion y un puntaje que otorga entrar en contacto con la misma
     * @param position posicion del nispero
     * @param score puntaje que otorga el nispero al obtenerse
     */
    public Nispero(Position position, Integer score) {
        super(Sprite.NISPERO, position, score);
    }
}
