package cr.ac.tec.ce3104.tc3.modes;

import cr.ac.tec.ce3104.tc3.physics.Speed;
import cr.ac.tec.ce3104.tc3.physics.Position;
import cr.ac.tec.ce3104.tc3.physics.SpeedRatio;
import cr.ac.tec.ce3104.tc3.physics.VerticalDirection;
import cr.ac.tec.ce3104.tc3.physics.HorizontalDirection;
import cr.ac.tec.ce3104.tc3.resources.Sprite;
import cr.ac.tec.ce3104.tc3.resources.Sequence;
import cr.ac.tec.ce3104.tc3.gameobjects.Platform;
import cr.ac.tec.ce3104.tc3.gameobjects.GameObject;
import cr.ac.tec.ce3104.tc3.gameobjects.PlayerAvatar;

// El jugador se encuentra colgando de una liana
public class Hanging implements ControllableMode {
    /**
     * Crea un nuevo modo para el jugador, el cual indica que el avatar del jugador se encuentra estático en una liana
     * @param direction direccion horizontal a la que mira el jugador
     * @param platform plataforma asociada a la liana en la que se encuentra el jugador
     * @param player entidad de juego que representa al avatar del jugador
     */
    public Hanging(HorizontalDirection direction, Platform platform, PlayerAvatar player) {
        this.platform = platform;

        Integer y = player.getPosition().getY();
        Position position = new Position(Hanging.calculateHorizontalBase(platform, direction), y);

        // Determina la validez de este movimiento
        if (player.getGame().testCollisions(player, position).getHitOrientation() != null) {
            direction = direction.invert();
            position = new Position(Hanging.calculateHorizontalBase(platform, direction), y);

            if (player.getGame().testCollisions(player, position).getHitOrientation() != null) {
                this.valid = false;
            }
        }

        if (this.valid) {
            this.direction = direction;
               player.relocate(position);
        }
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
    public void onJump(PlayerAvatar player) {
        player.switchTo(new Falling(new Standing(this.direction), player.getPosition()));
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
    /**
     * Obtiene la plataforma asociada a la liana en la que se encuentra el jugador
     * @return plataforma asociada a la liana en la que se encuentra el jugador
     */
    public Platform getPlatform() {
        return this.platform;
    }

    /**
     * @brief Determina si el modo es válido antes de cambiar al mismo,
     *
     * @return Estado de validez.
     */
    public Boolean isValid() {
        return this.valid;
    }

    /**
     * Calcula la posicion horizontal en la que se debe encontrar el jugador al sujetarse de la liana
     * @param platform plataforma asociada a una liana
     * @param direction direccion en la cual mira el jugador
     * @return resultado del calculo
     */
    private static Integer calculateHorizontalBase(Platform platform, HorizontalDirection direction) {
        // Para que "agarre" la liana
        Integer offset = -5;
        if (direction == HorizontalDirection.RIGHT) {
            offset = -offset - Sprite.HANGING_RIGHT.getSize().getWidth();
        }

        return platform.getBounds().getHorizontalCenter() + offset;
    }

    // Dirección y plataforma asociadas
    private HorizontalDirection direction;
    private Platform platform;
    private Boolean valid = true;

    /**
     * Indica la rutina a ejecutar dado un cambio de orientación del jugador producto de una entrada de usuario
     * @param player entidad que identifica al avatar del jugador
     * @param newDirection nueva direccion a la que "mira" el jugador
     */
    private void onFaceDirection(PlayerAvatar player, HorizontalDirection newDirection) {
        this.direction = this.direction.invert();
        if (this.direction == newDirection) {
            player.switchTo(new Falling(new Running(this.direction), player.getPosition()));
        } else {
            player.switchTo(new Hanging(this.direction, this.platform, player));
        }
    }
}
