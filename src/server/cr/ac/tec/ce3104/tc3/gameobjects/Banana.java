package cr.ac.tec.ce3104.tc3.gameobjects;

import cr.ac.tec.ce3104.tc3.physics.Position;
import cr.ac.tec.ce3104.tc3.resources.Sprite;

// Un banano
public class Banana extends Fruit {
    /**
     * Crea una banana dada una posicion y un puntaje que otorga entrar en contacto con la misma
     * @param position posicion de la banana
     * @param score puntaje que otorga la banana al obtenerse
     */
    public Banana(Position position, Integer score) {
        super(Sprite.BANANA, position, score);
    }
}
