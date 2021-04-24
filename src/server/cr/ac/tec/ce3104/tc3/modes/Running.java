package cr.ac.tec.ce3104.tc3.modes;

import cr.ac.tec.ce3104.tc3.physics.Speed;
import cr.ac.tec.ce3104.tc3.physics.SpeedRatio;
import cr.ac.tec.ce3104.tc3.physics.HorizontalDirection;
import cr.ac.tec.ce3104.tc3.resources.Animation;
import cr.ac.tec.ce3104.tc3.resources.Sequence;
import cr.ac.tec.ce3104.tc3.gameobjects.PlayerAvatar;

public class Running implements ControllableMode {
    public Running(HorizontalDirection direction) {
        this.direction = direction;
    }

    @Override
    public Speed getSpeed() {
        SpeedRatio ratio = Running.SPEED_RATIO;
        if (this.direction == HorizontalDirection.LEFT) {
            ratio = ratio.negate();
        }

        return Speed.horizontal(ratio);
    }

    @Override
    public Sequence getSequence() {
        return this.direction == HorizontalDirection.LEFT ? Animation.RUNNING_LEFT : Animation.RUNNING_RIGHT;
    }

    @Override
    public void onRelease(PlayerAvatar player) {
        player.switchTo(new Standing());
    }

    @Override
    public void onMoveLeft(PlayerAvatar player) {
        this.direction = HorizontalDirection.LEFT;
        player.switchTo(this);
    }

    @Override
    public void onMoveRight(PlayerAvatar player) {
        this.direction = HorizontalDirection.RIGHT;
        player.switchTo(this);
    }

    private static final SpeedRatio SPEED_RATIO = new SpeedRatio(3, 3);

    private HorizontalDirection direction;
}
