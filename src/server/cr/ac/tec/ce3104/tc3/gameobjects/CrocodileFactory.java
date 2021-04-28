package cr.ac.tec.ce3104.tc3.gameobjects;

import cr.ac.tec.ce3104.tc3.Game;

// Una fábrica de cocodrilos
public class CrocodileFactory {
    /**
     * Interfaz para la creación de cocodrilos de diversos tipos. 
     * @param type tipo de cocodrilo deseado
     * @param game juego en el que se quiere colocar el cocodrilo
     * @param platform plataforma sobre la cual se desea colocar el cocodrilo inicialmente
     * @param difficulty dificultad caracteristica de cocodrilo
     * @return un objeto cocodrilo, ya sea un cocodrilo rojo o azul
     */
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
