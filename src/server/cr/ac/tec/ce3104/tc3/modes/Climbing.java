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

// El jugador se está moviendo a lo vertical de una liana
public class Climbing implements ControllableMode {
    /**
     * Genera un nuevo modo para el jugador, el cual indica que el jugador se encuentra escalando por una liana u objeto similar
     * @param hangingMode modo anterior, el cual era un modo en el que el jugador está en la liana, pero no se mueve
     * @param direction direccion vertical en la que se mueve el jugador por la liana
     */
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
        // Se cae si por alguna razón se sale de la liana
        for (Vines vines : this.hangingMode.getPlatform().getAttached()) {
            if (vines.getBounds().collidesWith(player.getBounds())) {
                return;
            }
        }

        player.switchTo(new Falling(this, player.getPosition()));
    }

    @Override
    public void onHit(GameObject player, Orientation orientation) {
        // Los golpes al subir/bajar de una liana tienen una mecánica distinta
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
        Hanging mode = new Hanging(HorizontalDirection.RIGHT, this.hangingMode.getPlatform(), player);
        // Puede no permitirse
        if (mode.isValid()) {
            player.switchTo(mode);
        }
    }

    @Override
    public void onMoveRight(PlayerAvatar player) {
        Hanging mode = new Hanging(HorizontalDirection.LEFT, this.hangingMode.getPlatform(), player);
        // Puede no permitirse
        if (mode.isValid()) {
            player.switchTo(mode);
        }
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

    // Modo de colgado del que proviene
    private Hanging hangingMode;
    private VerticalDirection direction;
}
