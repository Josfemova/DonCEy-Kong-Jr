package cr.ac.tec.ce3104.tc3;

/**
 * Enumerador para caracterizar las presiones de teclas de parte del cliente
 */
public enum Key {
    UP,
    DOWN,
    LEFT,
    RIGHT,
    JUMP;

    public static Key parse(String source) {
        switch (source) {
            case "up":
                return Key.UP;

            case "down":
                return Key.DOWN;

            case "left":
                return Key.LEFT;

            case "right":
                return Key.RIGHT;

            case "jump":
                return Key.JUMP;

            default:
                return null;
        }
    }
}
