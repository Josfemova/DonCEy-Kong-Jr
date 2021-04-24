package cr.ac.tec.ce3104.tc3.gameobjects;

import cr.ac.tec.ce3104.tc3.physics.Position;

public class FruitFactory {
    public static Fruit createFruit(FruitType type, Position position, Integer score) {
        switch (type) {
            case NISPERO:
                return new Nispero(position, score);

            case APPLE:
                return new Apple(position, score);

            case BANANA:
                return new Banana(position, score);

            default:
                assert false;
                return null;
        }
    }
}
