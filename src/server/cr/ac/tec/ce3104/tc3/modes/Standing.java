package cr.ac.tec.ce3104.tc3.modes;

import cr.ac.tec.ce3104.tc3.physics.Speed;
import cr.ac.tec.ce3104.tc3.physics.SpeedRatio;
import cr.ac.tec.ce3104.tc3.physics.HorizontalDirection;
import cr.ac.tec.ce3104.tc3.resources.Sprite;
import cr.ac.tec.ce3104.tc3.resources.Sequence;
import cr.ac.tec.ce3104.tc3.gameobjects.GameObject;
import cr.ac.tec.ce3104.tc3.gameobjects.PlayerAvatar;

// El jugador se encuentra quieto sobre una plataforma
public class Standing implements ControllableMode {
    /**
     * Crea un nuevo modo para el jugador en la posición de inicio de juego
     * @param direction dirección horizontal a la cual mira el avatar del jugador
     * @return Modo de pie que solo se usa en la posición inicial.
     */
    public static Standing initial() {
         Standing mode = new Standing(HorizontalDirection.RIGHT);
        mode.initial = true;
        return mode;
    }

    /**
     * Crea un nuevo modo para el jugador el cual indica que el avatar del jugador se encuentra inmóvil sobre alguna superficie
     * @param direction dirección horizontal a la cual mira el avatar del jugador
     */
    public Standing(HorizontalDirection direction) {
        this.direction = direction;
    }

    @Override
    public Speed getSpeed() {
        return Speed.stationary();
    }

    @Override
    public Sequence getSequence() {
        if (this.initial) {
            return Sprite.STANDING_FIRST;
        }

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
    private Boolean initial = false;
}
