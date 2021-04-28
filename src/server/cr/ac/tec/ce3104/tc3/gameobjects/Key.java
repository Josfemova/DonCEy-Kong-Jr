package cr.ac.tec.ce3104.tc3.gameobjects;

import cr.ac.tec.ce3104.tc3.physics.Dynamics;
import cr.ac.tec.ce3104.tc3.physics.Position;
import cr.ac.tec.ce3104.tc3.resources.Sprite;

// La llave
public class Key extends GameObject {
    /**
     * Crea la entidad que representa la llave necesaria para liberar a Donkey Kong y ganar el nivel actual
     * @param position posicion en la que se quiere crear dicha llave
     */
    public Key(Position position) {
        super(Sprite.KEY, position);
    }

    @Override
    public Dynamics getDynamics() {
        return Dynamics.FLOATING;
    }
}
