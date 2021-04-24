package cr.ac.tec.ce3104.tc3.modes;

import cr.ac.tec.ce3104.tc3.physics.Speed;
import cr.ac.tec.ce3104.tc3.resources.Sequence;

public interface Mode {
    Speed getSpeed();

    Sequence getSequence();
}
