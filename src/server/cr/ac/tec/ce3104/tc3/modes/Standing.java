package cr.ac.tec.ce3104.tc3.modes;

import cr.ac.tec.ce3104.tc3.physics.Speed;
import cr.ac.tec.ce3104.tc3.physics.SpeedRatio;
import cr.ac.tec.ce3104.tc3.physics.HorizontalDirection;
import cr.ac.tec.ce3104.tc3.resources.Sprite;
import cr.ac.tec.ce3104.tc3.resources.Sequence;
import cr.ac.tec.ce3104.tc3.gameobjects.GameObject;
import cr.ac.tec.ce3104.tc3.gameobjects.PlayerAvatar;

public class Standing implements ControllableMode {
    public Standing(HorizontalDirection direction) {
        this.direction = direction;
    }

    @Override
    public Speed getSpeed() {
        return Speed.stationary();
    }

    @Override
    public Sequence getSequence() {
        return this.direction == HorizontalDirection.LEFT ? Sprite.STANDING_LEFT : Sprite.STANDING_RIGHT;
    }

    @Override
    public HorizontalDirection getDirection() {
        return this.direction;
    }

    @Override
    public void onFreeFall(GameObject player) {
        player.switchTo(new Falling(this, player.getPosition()));
    }

    @Override
    public void onJump(PlayerAvatar player) {
        player.switchTo(new Jumping(this, player));
    }

    @Override
    public void onMoveLeft(PlayerAvatar player) {
        player.switchTo(new Running(HorizontalDirection.LEFT));
    }

    @Override
    public void onMoveRight(PlayerAvatar player) {
        player.switchTo(new Running(HorizontalDirection.RIGHT));
    }

    private HorizontalDirection direction;
}
