package cr.ac.tec.ce3104.tc3.gameobjects;

import cr.ac.tec.ce3104.tc3.physics.Dynamics;
import cr.ac.tec.ce3104.tc3.physics.Position;
import cr.ac.tec.ce3104.tc3.resources.Sprite;

public abstract class Fruit extends GameObject {
    public Fruit(Sprite sprite, Position position, Integer score) {
        super(sprite, position);
        this.score = score;
    }

    @Override
    public Dynamics getDynamics() {
        return Dynamics.FLOATING;
    }

    public Integer getScore() {
        return this.score;
    }

    private Integer score;
}
