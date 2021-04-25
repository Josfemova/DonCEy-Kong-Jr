package cr.ac.tec.ce3104.tc3.physics;

public class Bounds {
    public Bounds(Position origin, Size size) {
        this.origin = origin;
        this.size = size;
    }

    public Position getOrigin() {
        return this.origin;
    }

    public Size getSize() {
        return this.size;
    }

    public Integer getBaseline() {
        return this.origin.getY() + this.size.getHeight();
    }

    public Integer getHorizontalCenter() {
        return this.origin.getX() + this.size.getWidth() / 2;
    }

    public Boolean leftOf(Bounds reference) {
        return this.origin.getX() + this.size.getWidth() <= reference.origin.getX();
    }

    public Boolean rightOf(Bounds reference) {
        return this.origin.getX() >= reference.origin.getX() + reference.size.getWidth();
    }

    public Boolean aboveOf(Bounds reference) {
        return this.origin.getY() + this.size.getHeight() <= reference.origin.getY();
    }

    public Boolean belowOf(Bounds reference) {
        return this.origin.getY() >= reference.origin.getY() + reference.size.getHeight();
    }

    public Boolean collidesWith(Bounds other) {
        return !this.leftOf(other) && !this.rightOf(other) && !this.aboveOf(other) && !this.belowOf(other);
    }

    private final Position origin;
    private final Size size;
}
