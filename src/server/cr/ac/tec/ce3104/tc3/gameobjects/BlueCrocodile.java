package cr.ac.tec.ce3104.tc3.gameobjects;

import cr.ac.tec.ce3104.tc3.Game;
import cr.ac.tec.ce3104.tc3.physics.Size;
import cr.ac.tec.ce3104.tc3.physics.Bounds;
import cr.ac.tec.ce3104.tc3.physics.Position;
import cr.ac.tec.ce3104.tc3.modes.BlueSearching;
import cr.ac.tec.ce3104.tc3.resources.Animation;

public class BlueCrocodile extends Crocodile {
    /**
     * Crea un nuevo cocodrilo azul bajo un juego en una plataforma dada y con caracteristicas adaptadas segun el nivel de dificultad
     * @param game juego en el que se quiere crear la entidad
     * @param platform plataforma sobre la cual spawnea el cocodrilo
     * @param difficulty dificultad característica del cocodrilo
     */
    public BlueCrocodile(Game game, Platform platform, Integer difficulty) {
        super
        (
            new BlueSearching(game, Crocodile.getSpeedDenominator(difficulty)),
            BlueCrocodile.positionFromPlatform(platform)
        );
    }
    /**
     * Obtiene una posición a partir de una plataforma dad
     * @param platform plataforma a utilizar para extraer la posición
     * @return posición extraída de la plataforma para colocar un cocodrilo
     */
    private static Position positionFromPlatform(Platform platform) {
        Bounds platformBounds = platform.getBounds();
        Size spriteSize = Animation.BLUE_CROCODILE_RIGHT.getSize();

        return new Position
        (
            platformBounds.getHorizontalCenter() - spriteSize.getWidth() / 2,
            platformBounds.getOrigin().getY() - spriteSize.getHeight()
        );
    }
}
