package cr.ac.tec.ce3104.tc3.gameobjects;

import cr.ac.tec.ce3104.tc3.physics.Dynamics;
import cr.ac.tec.ce3104.tc3.physics.Position;
import cr.ac.tec.ce3104.tc3.resources.Sprite;

public class DonkeyKong extends GameObject {
    public DonkeyKong(Position position) {
        super(Sprite.DONKEY_KONG, position);
    }

    @Override
    public Dynamics getDynamics() {
        return Dynamics.RIGID;
    }
}
