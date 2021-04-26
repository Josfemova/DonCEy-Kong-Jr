package cr.ac.tec.ce3104.tc3.physics;

public enum VerticalDirection {
    UP,
    DOWN;

    public VerticalDirection invert() {
        switch (this) {
            case UP:
                return VerticalDirection.DOWN;

            case DOWN:
                return VerticalDirection.UP;

            default:
                assert false;
                return null;
        }
    }
}
