package cr.ac.tec.ce3104.tc3.gameobjects;

import cr.ac.tec.ce3104.tc3.physics.Position;
import cr.ac.tec.ce3104.tc3.resources.Sequence;

public abstract class Crocodile extends GameObject {
    protected Crocodile(Sequence sequence, Position position) {
        super(sequence, position);
    }
}
