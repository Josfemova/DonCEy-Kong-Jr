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

public class RedCrawling implements Mode {
    public RedCrawling(Platform platform) {
        this.platform = platform;
    }

    @Override
    public Speed getSpeed() {
        SpeedRatio ratio = RedCrawling.SPEED_RATIO;
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
        Vines[] vines = this.platform.getAttached();
        if (!this.platform.exists() || vines == null) {
            crocodile.delete();
        } else if (this.direction == VerticalDirection.DOWN) {
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
            this.direction = this.direction.invert();
            crocodile.switchTo(this);
    }

    private static final SpeedRatio SPEED_RATIO = new SpeedRatio(2, 3);

    private VerticalDirection direction = VerticalDirection.DOWN;
    private Platform platform;
}
