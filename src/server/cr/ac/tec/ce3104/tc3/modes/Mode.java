package cr.ac.tec.ce3104.tc3.modes;

import cr.ac.tec.ce3104.tc3.physics.Speed;
import cr.ac.tec.ce3104.tc3.physics.Orientation;
import cr.ac.tec.ce3104.tc3.resources.Sequence;
import cr.ac.tec.ce3104.tc3.gameobjects.GameObject;

public interface Mode {
    Speed getSpeed();

    Sequence getSequence();

    default void onRelocate(GameObject object) {}

    default void onFreeFall(GameObject object) {}

    default void onHit(GameObject object, Orientation orientation) {}
}
