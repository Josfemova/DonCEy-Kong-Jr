package cr.ac.tec.ce3104.tc3.gameobjects;

import cr.ac.tec.ce3104.tc3.physics.Dynamics;
import cr.ac.tec.ce3104.tc3.physics.Position;
import cr.ac.tec.ce3104.tc3.resources.Sprite;

public class Vines extends GameObject {
    public Vines(Position position) {
        super(Sprite.VINES, position);
    }

    @Override
    public Dynamics getInteractionMode() {
        return Dynamics.FLOATING;
    }
}
