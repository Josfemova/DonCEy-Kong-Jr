package cr.ac.tec.ce3104.tc3.gameobjects;

import cr.ac.tec.ce3104.tc3.physics.Position;
import cr.ac.tec.ce3104.tc3.resources.Sprite;

public class Banana extends Fruit {
    public Banana(Position position, Integer score) {
        super(Sprite.BANANA, position, score);
    }
}
