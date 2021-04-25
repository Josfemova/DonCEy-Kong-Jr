package cr.ac.tec.ce3104.tc3.modes;

import cr.ac.tec.ce3104.tc3.physics.Speed;
import cr.ac.tec.ce3104.tc3.physics.SpeedRatio;
import cr.ac.tec.ce3104.tc3.physics.VerticalDirection;
import cr.ac.tec.ce3104.tc3.physics.HorizontalDirection;
import cr.ac.tec.ce3104.tc3.resources.Sprite;
import cr.ac.tec.ce3104.tc3.resources.Sequence;
import cr.ac.tec.ce3104.tc3.gameobjects.Platform;
import cr.ac.tec.ce3104.tc3.gameobjects.GameObject;
import cr.ac.tec.ce3104.tc3.gameobjects.PlayerAvatar;

public class Hanging implements ControllableMode {
    public Hanging(HorizontalDirection direction, Platform platform) {
        this.direction = direction;
        this.platform = platform;
    }

    @Override
    public Speed getSpeed() {
        return Speed.stationary();
    }

    @Override
    public Sequence getSequence() {
        return this.direction == HorizontalDirection.LEFT ? Sprite.HANGING_LEFT : Sprite.HANGING_RIGHT;
    }

    @Override
    public HorizontalDirection getDirection() {
        return this.direction;
    }

    @Override
    public void onMoveLeft(PlayerAvatar player) {
        this.onFaceDirection(player, HorizontalDirection.LEFT);
    }

    @Override
    public void onMoveRight(PlayerAvatar player) {
        this.onFaceDirection(player, HorizontalDirection.RIGHT);
    }

    @Override
    public void onMoveUp(PlayerAvatar player) {
        player.switchTo(new Climbing(this, VerticalDirection.UP));
    }

    @Override
    public void onMoveDown(PlayerAvatar player) {
        player.switchTo(new Climbing(this, VerticalDirection.DOWN));
    }

    public Platform getPlatform() {
        return this.platform;
    }

    private HorizontalDirection direction;
    private Platform platform;

    private void onFaceDirection(PlayerAvatar player, HorizontalDirection newDirection) {
        this.direction = this.direction.invert();
        if (this.direction == newDirection) {
            player.switchTo(new Jumping(this, player));
        } else {
            player.switchTo(this);
        }
    }
}
