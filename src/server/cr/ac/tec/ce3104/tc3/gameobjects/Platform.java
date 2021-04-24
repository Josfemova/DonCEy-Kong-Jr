package cr.ac.tec.ce3104.tc3.gameobjects;

import cr.ac.tec.ce3104.tc3.physics.Dynamics;
import cr.ac.tec.ce3104.tc3.physics.Position;
import cr.ac.tec.ce3104.tc3.resources.Sprite;

public class Platform extends GameObject {
    public Platform(Position position) {
        //TODO
        super((Sprite)null, position);
    }

    @Override
    public Dynamics getInteractionMode() {
        return Dynamics.RIGID;
    }
}
