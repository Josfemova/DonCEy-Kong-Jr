package cr.ac.tec.ce3104.tc3.modes;

import cr.ac.tec.ce3104.tc3.physics.Speed;
import cr.ac.tec.ce3104.tc3.physics.Bounds;
import cr.ac.tec.ce3104.tc3.physics.Position;
import cr.ac.tec.ce3104.tc3.physics.SpeedRatio;
import cr.ac.tec.ce3104.tc3.physics.Orientation;
import cr.ac.tec.ce3104.tc3.physics.VerticalDirection;
import cr.ac.tec.ce3104.tc3.resources.Sequence;
import cr.ac.tec.ce3104.tc3.resources.Animation;
import cr.ac.tec.ce3104.tc3.gameobjects.Vines;
import cr.ac.tec.ce3104.tc3.gameobjects.Platform;
import cr.ac.tec.ce3104.tc3.gameobjects.GameObject;

// Un cocodrilo rojo se mueve a lo vertical de una liana
public class RedCrawling implements Mode {
    /**
     * Crea un nuevo modo de objeto que indica que un cocodrilo rojo se encuentra movimiento en una liana
     * @param platform plataforma asociada a la liana en la cual se encuentra el cocodrilo
     * @param speedDenominator cantidad de ticks necesarios hasta el próximo movimiento
     */
    public RedCrawling(Platform platform, Integer speedDenominator) {
        this.platform = platform;
        this.speedDenominator = speedDenominator;
    }

    @Override
    public Speed getSpeed() {
        SpeedRatio ratio = new SpeedRatio(RedCrawling.SPEED_NUMERATOR, this.speedDenominator);
        if (this.direction == VerticalDirection.UP) {
            ratio = ratio.negate();
        }

        return Speed.vertical(ratio);
    }

    @Override
    public Sequence getSequence() {
        return this.direction == VerticalDirection.UP ? Animation.RED_CROCODILE_UP : Animation.RED_CROCODILE_DOWN;
    }

    @Override
    public void onRelocate(GameObject crocodile) {
        // Muere si se sale de la liana por cualquier razón
        Vines[] vines = this.platform.getAttached();
        if (!this.platform.exists() || vines == null) {
            crocodile.delete();
        } else if (this.direction == VerticalDirection.DOWN) {
            // Rebota algún tiempo antes de llegar al borde exacto
            Bounds crocodileBounds = crocodile.getBounds();
            Bounds lastBounds = vines[vines.length - 1].getBounds();
            Integer crocodileHeight = crocodileBounds.getSize().getHeight();
            Position adjacentPosition = new Position(crocodile.getPosition().getX(), crocodile.getPosition().getY() + crocodileHeight);
            Bounds adjacentBounds = new Bounds(adjacentPosition, crocodileBounds.getSize());

            if (lastBounds.collidesWith(crocodileBounds) && !lastBounds.collidesWith(adjacentBounds)) {
                    this.onHit(crocodile, null);
            }
        }
    }

    @Override
    public void onHit(GameObject crocodile, Orientation orientation) {
        // Si pega cambia de dirección
        this.direction = this.direction.invert();
        crocodile.switchTo(this);
    }

    private static final Integer SPEED_NUMERATOR = 2;

    // Dirección de movimiento y plataforma
    private VerticalDirection direction = VerticalDirection.DOWN;
    private Platform platform;
    private Integer speedDenominator;
}
