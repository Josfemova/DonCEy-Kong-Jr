package cr.ac.tec.ce3104.tc3.modes;

import cr.ac.tec.ce3104.tc3.Game;
import cr.ac.tec.ce3104.tc3.physics.Speed;
import cr.ac.tec.ce3104.tc3.physics.SpeedRatio;
import cr.ac.tec.ce3104.tc3.physics.Orientation;
import cr.ac.tec.ce3104.tc3.physics.HorizontalDirection;
import cr.ac.tec.ce3104.tc3.resources.Sequence;
import cr.ac.tec.ce3104.tc3.resources.Animation;
import cr.ac.tec.ce3104.tc3.gameobjects.GameObject;

// El jugador ha perdido
public class Dying implements ControllableMode {
    public Dying(Game game) {
        this.game = game;
    }

    @Override
    public Speed getSpeed() {
        return Speed.vertical(Dying.FREE_FALL_SPEED_RATIO);
    }

    @Override
    public Sequence getSequence() {
        return Animation.DYING;
    }

    @Override
    public HorizontalDirection getDirection() {
        return HorizontalDirection.RIGHT;
    }

    @Override
    public void onHit(GameObject player, Orientation orientation) {
        this.game.onPlayerLost();
    }

    private static final SpeedRatio FREE_FALL_SPEED_RATIO = new SpeedRatio(8, 3);

    private Game game;
}
