
package cr.ac.tec.ce3104.tc3.modes;

import cr.ac.tec.ce3104.tc3.physics.Speed;
import cr.ac.tec.ce3104.tc3.physics.SpeedRatio;
import cr.ac.tec.ce3104.tc3.physics.Orientation;
import cr.ac.tec.ce3104.tc3.physics.HorizontalDirection;
import cr.ac.tec.ce3104.tc3.resources.Sprite;
import cr.ac.tec.ce3104.tc3.resources.Sequence;
import cr.ac.tec.ce3104.tc3.gameobjects.GameObject;
import cr.ac.tec.ce3104.tc3.gameobjects.PlayerAvatar;

public class Jumping implements ControllableMode {
    public Jumping(ControllableMode lastMode, PlayerAvatar player) {
        this.lastMode = lastMode;
        this.initialY = player.getPosition().getY();
    }

    @Override
    public Speed getSpeed() {
        return new Speed(this.lastMode.getSpeed().getX(), Jumping.VERTICAL_SPEED_RATIO);
    }

    @Override
    public Sequence getSequence() {
        return this.getDirection() == HorizontalDirection.LEFT ? Sprite.JUMPING_LEFT : Sprite.JUMPING_RIGHT;
    }

    @Override
    public void onRelocate(GameObject player) {
        if (initialY - player.getPosition().getY() >= Jumping.FALL_THRESHOLD) {
            player.switchTo(new Falling(this, player.getPosition()));
        }
    }

    @Override
    public HorizontalDirection getDirection() {
        return this.lastMode.getDirection();
    }

    @Override
    public void onHit(GameObject player, Orientation orientation) {
        player.switchTo(new Falling(new Standing(this.getDirection()), player.getPosition()));
    }

    private static final Integer    FALL_THRESHOLD       = 17;
    private static final SpeedRatio VERTICAL_SPEED_RATIO = new SpeedRatio(-2, 1);

    private ControllableMode lastMode;
    private final Integer initialY;
}
