package cr.ac.tec.ce3104.tc3.gameobjects;

public class CrocodileFactory {
    public Crocodile createCrocodile(CrocodileType type, Platform platform, Integer difficulty) {
        switch (type) {
            case RED:
                return new RedCrocodile(platform, difficulty);

            case BLUE:
                return new BlueCrocodile(platform, difficulty);

            default:
                assert false;
                return null;
        }
    }
}
