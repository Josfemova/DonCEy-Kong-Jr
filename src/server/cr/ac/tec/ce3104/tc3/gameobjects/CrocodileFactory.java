package cr.ac.tec.ce3104.tc3.gameobjects;

import cr.ac.tec.ce3104.tc3.Game;

public class CrocodileFactory {
    public Crocodile createCrocodile(CrocodileType type, Game game, Platform platform, Integer difficulty) {
        switch (type) {
            case RED:
                return new RedCrocodile(platform, difficulty);

            case BLUE:
                return new BlueCrocodile(game, platform, difficulty);

            default:
                assert false;
                return null;
        }
    }
}
