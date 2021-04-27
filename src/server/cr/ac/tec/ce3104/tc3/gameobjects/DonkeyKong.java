package cr.ac.tec.ce3104.tc3.gameobjects;

import cr.ac.tec.ce3104.tc3.Game;
import cr.ac.tec.ce3104.tc3.physics.Dynamics;
import cr.ac.tec.ce3104.tc3.physics.Position;
import cr.ac.tec.ce3104.tc3.resources.Sprite;

public class DonkeyKong extends GameObject {

    /**
     * Constructor que genera una nueva entidad que representa a Donkey Kong
     * @param position posicion en la que se quiere colocar a Donkey Kong
     * @param game juego en el que se quiere agrega la entidad
     */
    public DonkeyKong(Position position, Game game) {
        super(Sprite.DONKEY_KONG, position);
        this.game = game;
    }

    @Override
    public Dynamics getDynamics() {
        return this.game.getPlayer().hasKey() ? Dynamics.INTERACTIVE : Dynamics.RIGID;
    }

    @Override
    public void onInteraction(GameObject other) {
        if (other == this.game.getPlayer()) {
            this.game.onPlayerWon();
        }
    }

    private Game game;
}
