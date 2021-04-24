package cr.ac.tec.ce3104.tc3.modes;

import cr.ac.tec.ce3104.tc3.physics.Speed;
import cr.ac.tec.ce3104.tc3.physics.HorizontalDirection;
import cr.ac.tec.ce3104.tc3.resources.Sprite;
import cr.ac.tec.ce3104.tc3.resources.Sequence;
import cr.ac.tec.ce3104.tc3.gameobjects.PlayerAvatar;

public class Standing implements ControllableMode {
    @Override
    public Speed getSpeed() {
        return Speed.stationary();
    }

    @Override
    public Sequence getSequence() {
        return Sprite.STANDING;
    }

    @Override
    public void onMoveLeft(PlayerAvatar player) {
        player.switchTo(new Running(HorizontalDirection.LEFT));
    }

    @Override
    public void onMoveRight(PlayerAvatar player) {
        player.switchTo(new Running(HorizontalDirection.RIGHT));
    }
}
