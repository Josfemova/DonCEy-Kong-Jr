package cr.ac.tec.ce3104.tc3.physics;

/**
 * Enumerador para indicar si un objeto mira hacia la derecha o la izquierda
 */
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
