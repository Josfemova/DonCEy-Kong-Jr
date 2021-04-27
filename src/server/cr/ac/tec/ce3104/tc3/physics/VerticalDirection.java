package cr.ac.tec.ce3104.tc3.physics;

/**
 * Enumerador que indica las direcciones posibles en un eje vertical
 */
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
