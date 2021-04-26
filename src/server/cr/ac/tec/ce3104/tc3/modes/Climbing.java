package cr.ac.tec.ce3104.tc3.modes;

import cr.ac.tec.ce3104.tc3.physics.Speed;
import cr.ac.tec.ce3104.tc3.physics.SpeedRatio;
import cr.ac.tec.ce3104.tc3.physics.Orientation;
import cr.ac.tec.ce3104.tc3.physics.VerticalDirection;
import cr.ac.tec.ce3104.tc3.physics.HorizontalDirection;
import cr.ac.tec.ce3104.tc3.resources.Animation;
import cr.ac.tec.ce3104.tc3.resources.Sequence;
import cr.ac.tec.ce3104.tc3.gameobjects.Vines;
import cr.ac.tec.ce3104.tc3.gameobjects.GameObject;
import cr.ac.tec.ce3104.tc3.gameobjects.PlayerAvatar;

public class Climbing implements ControllableMode {
    public Climbing(Hanging hangingMode, VerticalDirection direction) {
        this.hangingMode = hangingMode;
        this.direction = direction;
    }

    @Override
    public Speed getSpeed() {
        SpeedRatio ratio = Climbing.SPEED_RATIO;
        if (this.direction == VerticalDirection.UP) {
            ratio = ratio.negate();
        }

        return Speed.vertical(ratio);
    }

    @Override
    public Sequence getSequence() {
        return this.getDirection() == HorizontalDirection.LEFT ? Animation.CLIMBING_LEFT : Animation.CLIMBING_RIGHT;
    }

    @Override
    public HorizontalDirection getDirection() {
        return this.hangingMode.getDirection();
    }

    @Override
    public void onRelocate(GameObject player) {
        for (Vines vines : this.hangingMode.getPlatform().getAttached()) {
            if (vines.getBounds().collidesWith(player.getBounds())) {
                return;
            }
        }

        player.switchTo(new Falling(this, player.getPosition(), this.hangingMode.getPlatform()));
    }

    @Override
    public void onHit(GameObject player, Orientation orientation) {
        switch (orientation) {
            case HORIZONTAL:
                PlayerAvatar avatar = (PlayerAvatar)player;
                if(this.getDirection() == HorizontalDirection.LEFT) {
                    this.hangingMode.onMoveLeft(avatar);
                } else {
                    this.hangingMode.onMoveRight(avatar);
                }

                break;

            case VERTICAL:
                player.switchTo(this.hangingMode);
                break;
        }
    }

    @Override
    public void onRelease(PlayerAvatar player) {
        player.switchTo(this.hangingMode);
    }

    @Override
    public void onMoveLeft(PlayerAvatar player) {
        player.switchTo(new Hanging(HorizontalDirection.RIGHT, this.hangingMode.getPlatform(), player));
    }

    @Override
    public void onMoveRight(PlayerAvatar player) {
        player.switchTo(new Hanging(HorizontalDirection.LEFT, this.hangingMode.getPlatform(), player));
    }

    @Override
    public void onMoveUp(PlayerAvatar player) {
        this.direction = VerticalDirection.UP;
        player.switchTo(this);
    }

    @Override
    public void onMoveDown(PlayerAvatar player) {
        this.direction = VerticalDirection.DOWN;
        player.switchTo(this);
    }

    private static final SpeedRatio SPEED_RATIO = new SpeedRatio(4, 6);

    private Hanging hangingMode;
    private VerticalDirection direction;
}
