package cr.ac.tec.ce3104.tc3.modes;

import cr.ac.tec.ce3104.tc3.physics.Speed;
import cr.ac.tec.ce3104.tc3.physics.SpeedRatio;
import cr.ac.tec.ce3104.tc3.physics.Orientation;
import cr.ac.tec.ce3104.tc3.physics.HorizontalDirection;
import cr.ac.tec.ce3104.tc3.resources.Animation;
import cr.ac.tec.ce3104.tc3.resources.Sequence;
import cr.ac.tec.ce3104.tc3.gameobjects.GameObject;
import cr.ac.tec.ce3104.tc3.gameobjects.PlayerAvatar;

// El jugador está caminando/corriendo
public class Running implements ControllableMode {
    /**
     * Crea un nuevo estado para indicar que el jugador se encuentra corriendo en el escenario de juego
     * @param direction direccion horizontal en la que corre el jugador
     */
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
    public HorizontalDirection getDirection() {
        return this.direction;
    }

    @Override
    public void onFreeFall(GameObject player) {
        player.switchTo(new Falling(this, player.getPosition()));
    }

    @Override
    public void onHit(GameObject player, Orientation orientation) {
        this.onRelease((PlayerAvatar)player);
    }

    @Override
    public void onRelease(PlayerAvatar player) {
        player.switchTo(new Standing(this.direction));
    }

    @Override
    public void onJump(PlayerAvatar player) {
        player.switchTo(new Jumping(this, player));
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

    private static final SpeedRatio SPEED_RATIO = new SpeedRatio(6, 3);

    private HorizontalDirection direction;
}
