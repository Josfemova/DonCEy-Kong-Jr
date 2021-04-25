package cr.ac.tec.ce3104.tc3.physics;

public enum HorizontalDirection {
    LEFT,
    RIGHT;

    public HorizontalDirection invert() {
        switch (this) {
            case LEFT:
                return HorizontalDirection.RIGHT;

            case RIGHT:
                return HorizontalDirection.LEFT;

            default:
                assert false;
                return null;
        }
    }
}
