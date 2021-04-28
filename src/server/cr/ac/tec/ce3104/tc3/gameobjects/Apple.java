package cr.ac.tec.ce3104.tc3.gameobjects;

import cr.ac.tec.ce3104.tc3.physics.Position;
import cr.ac.tec.ce3104.tc3.resources.Sprite;

// Una manzana
public class Apple extends Fruit {
    /**
     * Crea una manzana dada una posicion y un puntaje que otorga entrar en contacto con la misma
     * @param position posicion de la manzana
     * @param score puntaje que otorga la manzana al obtenerse
     */
    public Apple(Position position, Integer score) {
        super(Sprite.APPLE, position, score);
    }
}
