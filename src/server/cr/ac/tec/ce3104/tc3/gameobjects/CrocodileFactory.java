package cr.ac.tec.ce3104.tc3.gameobjects;

public class CrocodileFactory {
    public static Crocodile createCrocodile(CrocodileType type, Platform platform) {
        switch (type) {
            case RED:
                return new RedCrocodile(platform);

            case BLUE:
                return new BlueCrocodile(platform);

            default:
                assert false;
                return null;
        }
    }
}
