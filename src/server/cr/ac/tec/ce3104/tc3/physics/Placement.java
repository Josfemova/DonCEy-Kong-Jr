package cr.ac.tec.ce3104.tc3.physics;

import java.util.ArrayList;
import java.util.Collection;

import cr.ac.tec.ce3104.tc3.levels.Level;
import cr.ac.tec.ce3104.tc3.gameobjects.GameObject;

public class Placement {
    public Placement(GameObject placed, Position placedAt, Level level, Collection<GameObject> scene) {
        this(placed, new Bounds(placedAt, placed.getSize()), level, scene);

        if (this.hitOrientation == null) {
            Integer nextVerticalDisplacement = Math.max(1, Math.abs(placed.getMode().getSpeed().getY().getNumerator()));
            Position nextPosition = new Position(placedAt.getX(), placedAt.getY() + nextVerticalDisplacement);
            Bounds nextBounds = new Bounds(nextPosition, this.bounds.getSize());

            if (this.hitOrientation == null) {
                this.freeFall = new Placement(placed, nextBounds, level, scene).hitOrientation == null;
            }
        }
    }

    public Orientation getHitOrientation() {
        return this.hitOrientation;
    }

    public GameObject getInteractionTarget() {
        return this.interactionTarget;
    }

    public Collection<GameObject> getTouchedFloatings() {
        return this.touchedFloatings;
    }

    public Boolean inFreeFall() {
        return this.freeFall;
    }

    private GameObject placed;
    private Bounds bounds;
    private Orientation hitOrientation = null;
    private Integer bestDistanceSquare = null;
    private GameObject interactionTarget = null;
    private Collection<GameObject> touchedFloatings = new ArrayList<>();
    private Boolean freeFall = false;

    private Placement(GameObject placed, Bounds bounds, Level level, Collection<GameObject> scene) {
        this.placed = placed;
        this.bounds = bounds;

        this.testWalls(level.getGameAreaSize());
        if (placed.getDynamics() != Dynamics.FLOATING) {
            this.testCollisions(scene);
        }
    }

    private void testWalls(Size gameAreaSize) {
        Integer areaWidth = gameAreaSize.getWidth();
        Integer areaHeight = gameAreaSize.getHeight();

        Bounds leftWall = new Bounds(new Position(-1, 0), new Size(1, areaHeight));
        Bounds rightWall = new Bounds(new Position(areaWidth, 0), new Size(1, areaHeight));
        Bounds bottomWall = new Bounds(new Position(0, areaHeight), new Size(areaWidth, 1));

        if (!this.bounds.rightOf(leftWall) || !this.bounds.leftOf(rightWall)) {
            this.tryHitOrientation(Orientation.HORIZONTAL);
        } else if (!this.bounds.aboveOf(bottomWall)) {
            Position current = this.placed.getPosition();
            Integer lowestValidY = areaHeight - this.bounds.getSize().getHeight();
            if (current.getY() > lowestValidY) {
                this.placed.relocate(new Position(current.getX(), lowestValidY));
            }

            this.tryHitOrientation(Orientation.VERTICAL);
        }
    }

    private void testCollisions(Collection<GameObject> scene) {
        Bounds beforeCollision = this.placed.getBounds();

        for (GameObject other : scene) {
            Bounds otherBounds = other.getBounds();
            if (other == this.placed || !this.bounds.collidesWith(otherBounds)) {
                continue;
            }

            switch (other.getDynamics()) {
                case RIGID:
                    Orientation orientation = Orientation.HORIZONTAL;
                    Boolean above = beforeCollision.aboveOf(otherBounds);
                    Boolean below = beforeCollision.belowOf(otherBounds);

                    if (above) {
                        Integer lowestValidY = otherBounds.getOrigin().getY() - this.bounds.getSize().getHeight();
                        if (beforeCollision.getBaseline() - 1 > lowestValidY) {
                            this.placed.relocate(new Position(beforeCollision.getOrigin().getX(), lowestValidY));
                        }
                    }

                    if (above || below) {
                        orientation = Orientation.VERTICAL;
                    }

                    this.tryHitOrientation(orientation);
                    break;

                case FLOATING:
                    this.touchedFloatings.add(other);
                    break;

                case INTERACTIVE:
                    this.tryInteractionTarget(other);
                    break;
            }
        }
    }

    private void tryHitOrientation(Orientation hitOrientation) {
        // Las colisiones rígidas verticales tienen precedencia por sobre las horizontales
        if (this.hitOrientation == null || this.hitOrientation != Orientation.VERTICAL) {
            this.hitOrientation = hitOrientation;
        }
    }

    private void tryInteractionTarget(GameObject other) {
        Integer deltaX = other.getPosition().getX() - this.bounds.getOrigin().getX();
        Integer deltaY = other.getPosition().getY() - this.bounds.getOrigin().getY();
        Integer distanceSquare = deltaX * deltaX + deltaY * deltaY;

        if (other.isDangerous() || this.bestDistanceSquare == null || distanceSquare <= this.bestDistanceSquare) {
            this.bestDistanceSquare = distanceSquare;
            this.interactionTarget = other;
        }
    }
}
