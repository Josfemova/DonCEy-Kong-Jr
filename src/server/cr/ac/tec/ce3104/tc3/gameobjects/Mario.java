package cr.ac.tec.ce3104.tc3.gameobjects;

import cr.ac.tec.ce3104.tc3.physics.Dynamics;
import cr.ac.tec.ce3104.tc3.physics.Position;
import cr.ac.tec.ce3104.tc3.resources.Sprite;

// Mario
public class Mario extends GameObject {
    /**
     * Constructor para crear una entidad con el aspecto de Mario, el responsable de la captura de Donkey Kong
     * @param position posicion en la que se quiere colocar a Mario en la pantalla de juego
     */
    public Mario(Position position) {
        super(Sprite.MARIO, position);
    }

    @Override
    public Dynamics getDynamics() {
        return Dynamics.RIGID;
    }
}
