package cr.ac.tec.ce3104.tc3.gameobjects;

import cr.ac.tec.ce3104.tc3.modes.Falling;
import cr.ac.tec.ce3104.tc3.modes.Hanging;
import cr.ac.tec.ce3104.tc3.modes.Climbing;
import cr.ac.tec.ce3104.tc3.modes.Standing;
import cr.ac.tec.ce3104.tc3.modes.ControllableMode;
import cr.ac.tec.ce3104.tc3.resources.Sprite;
import cr.ac.tec.ce3104.tc3.physics.Dynamics;
import cr.ac.tec.ce3104.tc3.physics.Position;
import cr.ac.tec.ce3104.tc3.physics.HorizontalDirection;

public class PlayerAvatar extends GameObject {
    public PlayerAvatar(Position position, Integer initialScore) {
        super(new Falling(new Standing(HorizontalDirection.RIGHT), position), position);
        this.score = initialScore;
    }

    @Override
    public Dynamics getDynamics() {
        return this.lost ? Dynamics.FLOATING : Dynamics.INTERACTIVE;
    }

    @Override
    public void onInteraction(GameObject other) {
        if (other.isDangerous()) {
            // Se ha tocado un enemigo o agua
            this.die();
        } else {
            ControllableMode mode = (ControllableMode)this.getMode();
            if (!(mode instanceof Climbing) && !(mode instanceof Hanging)) {
                Vines vines = (Vines)other;
                this.switchTo(new Hanging(mode.getDirection(), vines.getPlatform(), this));
            }
        }
    }

    @Override
    public void onFloatingContact(GameObject floating) {
        if (floating instanceof Fruit) {
            this.updateScore(+((Fruit)floating).getScore());
            floating.delete();
        } else if (floating instanceof Key) {
            this.hasKey = true;
            floating.delete();
        }
    }

    public Integer getScore() {
        return this.score;
    }

    public Boolean hasLost() {
        return this.lost;
    }

    public Boolean hasKey() {
        return this.hasKey;
    }

    public void die() {
        if (!this.lost) {
            this.lost = true;
            this.refreshMode();
        }
    }

    private void updateScore(Integer difference) {
        this.score += difference;
        this.refreshMode();
    }

    private Integer score;
    private Boolean lost = false;
    private Boolean hasKey = false;
}
