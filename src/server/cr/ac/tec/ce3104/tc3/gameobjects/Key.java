package cr.ac.tec.ce3104.tc3.gameobjects;

import cr.ac.tec.ce3104.tc3.physics.Dynamics;
import cr.ac.tec.ce3104.tc3.physics.Position;
import cr.ac.tec.ce3104.tc3.resources.Sprite;

public class Key extends GameObject {
    public Key(Position position) {
        super(Sprite.KEY, position);
    }

    @Override
    public Dynamics getDynamics() {
        return Dynamics.FLOATING;
    }
}
