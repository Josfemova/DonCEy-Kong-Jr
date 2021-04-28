package cr.ac.tec.ce3104.tc3.gameobjects;

import cr.ac.tec.ce3104.tc3.physics.Position;

// Fábrica de frutas
public class FruitFactory {
    /**
     * Interfaz para creación de frutas de diversos tipos
     * @param type tipo de fruta a crear
     * @param position posicion en la que se quiere colocar la fruta
     * @param score puntaje que otorga la fruta al ser obtenida
     * @return referencia a un objeto fruta colocable en un juego
     */
    public Fruit createFruit(FruitType type, Position position, Integer score) {
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
