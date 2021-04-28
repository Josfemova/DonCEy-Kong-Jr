package cr.ac.tec.ce3104.tc3.modes;

import java.util.Random;

import cr.ac.tec.ce3104.tc3.Game;
import cr.ac.tec.ce3104.tc3.physics.Speed;
import cr.ac.tec.ce3104.tc3.physics.Bounds;
import cr.ac.tec.ce3104.tc3.physics.Position;
import cr.ac.tec.ce3104.tc3.physics.SpeedRatio;
import cr.ac.tec.ce3104.tc3.physics.Orientation;
import cr.ac.tec.ce3104.tc3.physics.HorizontalDirection;
import cr.ac.tec.ce3104.tc3.resources.Animation;
import cr.ac.tec.ce3104.tc3.resources.Sequence;
import cr.ac.tec.ce3104.tc3.gameobjects.Vines;
import cr.ac.tec.ce3104.tc3.gameobjects.Platform;
import cr.ac.tec.ce3104.tc3.gameobjects.GameObject;

public class BlueSearching implements Mode {
    /**
     * Crea un modo de búsqueda de liana para un cocodrilo azul
     * @param game juego en el que se encuentra el cocodrilo azul
     * @param speedDenominator cada cuantos ticks de mueve el cocodrilo
     */
    public BlueSearching(Game game, Integer speedDenominator) {
        this.speedDenominator = speedDenominator;
        this.game = game;
    }

    @Override
    public Speed getSpeed() {
        SpeedRatio ratio = new SpeedRatio(BlueSearching.SPEED_NUMERATOR, this.speedDenominator);
        if (this.direction == HorizontalDirection.LEFT) {
            ratio = ratio.negate();
        }

        return Speed.horizontal(ratio);
    }

    @Override
    public Sequence getSequence() {
        return this.direction == HorizontalDirection.LEFT ? Animation.BLUE_CROCODILE_LEFT : Animation.BLUE_CROCODILE_RIGHT;
    }

    @Override
    public void onRelocate(GameObject crocodile) {
        Position oneBelow = new Position(crocodile.getPosition().getX(), crocodile.getPosition().getY() + 1);
        Bounds frictionBounds = new Bounds(oneBelow, crocodile.getSize());

        Platform platform = null;
        for (GameObject other : this.game.getGameObjects().values()) {
            if (other == crocodile || !(other instanceof Platform)) {
                continue;
            }

            Bounds otherBounds = other.getBounds();
            if (otherBounds.collidesWith(frictionBounds)) {
                platform = (Platform)other;
                break;
            }
        }

        if (this.currentPlatform != platform) {
            this.currentPlatform = platform;
            if (platform != null) {
                Vines[] attached = platform.getAttached();
                if (attached != null && BlueSearching.randomGenerator.nextInt(BlueSearching.UNLIKELIHOOD) == 0) {
                    Bounds first = attached[0].getBounds();
                    Position newOrigin = new Position
                    (
                        first.getHorizontalCenter() - Animation.BLUE_CROCODILE_DOWN.getSize().getWidth() / 2,
                        first.getOrigin().getY()
                    );

                    crocodile.relocate(newOrigin);
                    this.onFreeFall(crocodile);

                    return;
                }
            }
        }

        Integer offset = BlueSearching.SPEED_NUMERATOR + 1;
        if (this.direction == HorizontalDirection.LEFT) {
            offset = -offset;
        }

        Position nextBelow = new Position(oneBelow.getX() + offset, oneBelow.getY());
        if (game.testCollisions(crocodile, nextBelow).getHitOrientation() == null) {
            this.switchDirection(crocodile);
        }
    }

    @Override
    public void onFreeFall(GameObject crocodile) {
        crocodile.switchTo(new BlueCrawling(this.game, this.speedDenominator));
    }

    @Override
    public void onHit(GameObject crocodile, Orientation orientation) {
        this.switchDirection(crocodile);
    }

    private static final Integer SPEED_NUMERATOR = 5;
    private static final Integer UNLIKELIHOOD = 5;
    private static final Random randomGenerator = new Random();

    private HorizontalDirection direction = HorizontalDirection.RIGHT;
    private Integer speedDenominator;
    private Platform currentPlatform = null;
    private Game game;

    /**
     * Cambia la dirección a la cual se dirige el cocodrilo actual 
     * @param crocodile entidad que referencia al cocodrilo cuya orientación se quiere cambiar
     */
    private void switchDirection(GameObject crocodile) {
        this.direction = this.direction.invert();
        crocodile.switchTo(this);
    }
}
