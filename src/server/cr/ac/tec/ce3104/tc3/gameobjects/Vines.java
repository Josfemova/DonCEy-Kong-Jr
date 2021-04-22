package cr.ac.tec.ce3104.tc3.gameobjects;

import cr.ac.tec.ce3104.tc3.physics.Position;
import cr.ac.tec.ce3104.tc3.resources.Sprite;

class Vines extends GameObject {
    public Vines(Position position, Integer length) {
        super(Sprite.VINES, position, 1, length);
    }
}
