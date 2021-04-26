package cr.ac.tec.ce3104.tc3.modes;

import cr.ac.tec.ce3104.tc3.physics.Speed;
import cr.ac.tec.ce3104.tc3.physics.Position;
import cr.ac.tec.ce3104.tc3.physics.SpeedRatio;
import cr.ac.tec.ce3104.tc3.physics.Orientation;
import cr.ac.tec.ce3104.tc3.physics.HorizontalDirection;
import cr.ac.tec.ce3104.tc3.resources.Sprite;
import cr.ac.tec.ce3104.tc3.resources.Sequence;
import cr.ac.tec.ce3104.tc3.gameobjects.Platform;
import cr.ac.tec.ce3104.tc3.gameobjects.GameObject;
import cr.ac.tec.ce3104.tc3.gameobjects.PlayerAvatar;

public class Falling implements ControllableMode {
    public Falling(ControllableMode lastMode, Position initialPosition) {
        this(lastMode, initialPosition, null);
    }

    public Falling(ControllableMode lastMode, Position initialPosition, Platform sourcePlatform) {
        this.lastMode = lastMode;
        this.initialY = initialPosition.getY();
        this.sourcePlatform = sourcePlatform;
    }

    @Override
    public Speed getSpeed() {
        return new Speed(this.lastMode.getSpeed().getX(), Falling.FREE_FALL_SPEED_RATIO);
    }

    @Override
    public Sequence getSequence() {
        return this.getDirection() == HorizontalDirection.LEFT ? Sprite.FALLING_LEFT : Sprite.FALLING_RIGHT;
    }

    @Override
    public HorizontalDirection getDirection() {
        return this.lastMode.getDirection();
    }

    @Override
    public void onRelocate(GameObject player) {
        this.lastWasHorizontalHit = false;
    }

    @Override
    public void onHit(GameObject player, Orientation orientation) {
        Standing standingMode = new Standing(this.getDirection());
        switch (orientation) {
            case HORIZONTAL:
                if (!lastWasHorizontalHit) {
                    this.lastMode = standingMode;
                    player.switchTo(this);
                    lastWasHorizontalHit = true;
                }

                break;

            case VERTICAL:
                if (player.getPosition().getY() - this.initialY < Falling.DEATH_THRESHOLD) {
                    player.switchTo(standingMode);
                } else {
                    ((PlayerAvatar)player).die();
                }

                lastWasHorizontalHit = false;
                break;
        }
    }

    public Platform getSourcePlatform() {
        return this.sourcePlatform;
    }

    private static final SpeedRatio FREE_FALL_SPEED_RATIO = new SpeedRatio(3, 1);
    private static final Integer    DEATH_THRESHOLD       = 50;

    private ControllableMode lastMode;
    private Integer          initialY;
    private Platform         sourcePlatform;
    private Boolean          lastWasHorizontalHit = false;
}
