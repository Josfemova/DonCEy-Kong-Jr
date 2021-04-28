package cr.ac.tec.ce3104.tc3.gameobjects;

import cr.ac.tec.ce3104.tc3.physics.Dynamics;
import cr.ac.tec.ce3104.tc3.physics.Position;
import cr.ac.tec.ce3104.tc3.resources.Sprite;

// Una fruta
public abstract class Fruit extends GameObject {
    /**
     * Constructor general para objetos tipo fruta en el juego
     * @param sprite Sprite que contiene referencia a la imagen correspondiente a la fruta
     * @param position posición en la que se quiere crear la fruta
     * @param score puntaje que otorga la fruta al ser obtenida
     */
    public Fruit(Sprite sprite, Position position, Integer score) {
        super(sprite, position);
        this.score = score;
    }

    @Override
    public Dynamics getDynamics() {
        return Dynamics.FLOATING;
    }
    /**
     * Obtiene el puntaje que se le debe agregar al jugador si el mismo entra en contacto con la fruta
     * @return puntaje que otorga la fruta al ser obtenida
     */
    public Integer getScore() {
        return this.score;
    }

    @Override
    protected Integer getZ() {
        return 1;
    }

    private Integer score;
}
