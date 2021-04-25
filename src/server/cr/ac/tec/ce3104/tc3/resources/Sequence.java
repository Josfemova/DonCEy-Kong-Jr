package cr.ac.tec.ce3104.tc3.resources;

import cr.ac.tec.ce3104.tc3.physics.Size;

public abstract class Sequence {
    public abstract Sprite[] getSprites();

    public Size getSize() {
        return this.freeze().getSize();
    }

    public Sprite freeze() {
        return this.getSprites()[0];
    }
}
