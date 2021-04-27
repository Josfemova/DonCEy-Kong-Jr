package cr.ac.tec.ce3104.tc3.modes;

import cr.ac.tec.ce3104.tc3.Game;
import cr.ac.tec.ce3104.tc3.physics.Speed;
import cr.ac.tec.ce3104.tc3.physics.Bounds;
import cr.ac.tec.ce3104.tc3.physics.Position;
import cr.ac.tec.ce3104.tc3.physics.SpeedRatio;
import cr.ac.tec.ce3104.tc3.physics.Orientation;
import cr.ac.tec.ce3104.tc3.resources.Sprite;
import cr.ac.tec.ce3104.tc3.resources.Sequence;
import cr.ac.tec.ce3104.tc3.resources.Animation;
import cr.ac.tec.ce3104.tc3.gameobjects.GameObject;

public class BlueCrawling implements Mode {
    /**
     * Crea un nuevo modo para cocodrilo azul que se encuentra en movimiento en una liana
     * @param game juego en el que se encuentra el cocodrilo
     * @param speedDenominator cada cuantos ticks debe moverser el cocodrilo
     */
    public BlueCrawling(Game game, Integer speedDenominator) {
        this.speedDenominator = speedDenominator;
        this.game = game;
    }

    @Override
    public Speed getSpeed() {
        return Speed.vertical(new SpeedRatio(BlueCrawling.SPEED_NUMERATOR, this.speedDenominator));
    }

    @Override
    public Sequence getSequence() {
        return this.sequence;
    }

    @Override
    public void onHit(GameObject crocodile, Orientation orientation) {
        if (this.sequence == Animation.BLUE_CROCODILE_DOWN) {
            this.sequence = Sprite.FALLING_BLUE;
            crocodile.switchTo(this);
        } else {
            crocodile.switchTo(new BlueSearching(this.game, this.speedDenominator));
        }
    }

    private static final Integer SPEED_NUMERATOR = 2;

    private Integer speedDenominator;
    private Game game;
    private Sequence sequence = Animation.BLUE_CROCODILE_DOWN;
}
