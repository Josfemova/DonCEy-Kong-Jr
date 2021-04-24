package cr.ac.tec.ce3104.tc3.gameobjects;

import cr.ac.tec.ce3104.tc3.physics.Position;

public class CrocodileFactory {
    public static Crocodile createCrocodile(CrocodileType type, Position position) {
        switch (type) {
            case RED:
                return new RedCrocodile(position);

            case BLUE:
                return new BlueCrocodile(position);

            default:
                assert false;
                return null;
        }
    }
}
