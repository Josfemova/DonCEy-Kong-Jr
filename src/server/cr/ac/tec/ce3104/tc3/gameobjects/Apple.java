package cr.ac.tec.ce3104.tc3.gameobjects;

import cr.ac.tec.ce3104.tc3.physics.Position;
import cr.ac.tec.ce3104.tc3.resources.Sprite;

class Apple extends Fruit {
    public Apple(Position position, Integer score) {
        super(Sprite.APPLE, position, score);
    }
}
