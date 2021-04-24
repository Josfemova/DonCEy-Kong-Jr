package cr.ac.tec.ce3104.tc3.gameobjects;

import cr.ac.tec.ce3104.tc3.modes.Standing;
import cr.ac.tec.ce3104.tc3.physics.Dynamics;
import cr.ac.tec.ce3104.tc3.physics.Position;
import cr.ac.tec.ce3104.tc3.resources.Sprite;

public class PlayerAvatar extends GameObject {
    public PlayerAvatar(Position position) {
        super(new Standing(), position);
    }

    @Override
    public Dynamics getInteractionMode() {
        return Dynamics.INTERACTIVE;
    }
}
