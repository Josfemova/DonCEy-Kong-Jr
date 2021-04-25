package cr.ac.tec.ce3104.tc3.gameobjects;

import cr.ac.tec.ce3104.tc3.modes.Mode;
import cr.ac.tec.ce3104.tc3.physics.Dynamics;
import cr.ac.tec.ce3104.tc3.physics.Position;

public abstract class Crocodile extends GameObject {
    @Override
    public Dynamics getDynamics() {
        return Dynamics.INTERACTIVE;
    }

    protected Crocodile(Mode mode, Position position) {
        super(mode, position);
    }
}
