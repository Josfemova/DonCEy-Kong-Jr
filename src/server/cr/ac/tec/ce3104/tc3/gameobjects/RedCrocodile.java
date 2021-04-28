package cr.ac.tec.ce3104.tc3.gameobjects;

import cr.ac.tec.ce3104.tc3.physics.Bounds;
import cr.ac.tec.ce3104.tc3.physics.Position;
import cr.ac.tec.ce3104.tc3.modes.RedCrawling;
import cr.ac.tec.ce3104.tc3.resources.Animation;

// Cocodrilo rojo
public class RedCrocodile extends Crocodile {
    /**
     * Crea un nuevo cocodrilo rojo una plataforma dada y con caracteristicas adaptadas segun el nivel de dificultad
     * @param platform plataforma sobre la cual spawnea el cocodrilo
     * @param difficulty dificultad característica del cocodrilo
     */
    public RedCrocodile(Platform platform, Integer difficulty) {
        super(new RedCrawling(platform, Crocodile.getSpeedDenominator(difficulty)), RedCrocodile.positionFromPlatform(platform));
    }
    /**
     * Obtiene una posición a partir de una plataforma dad
     * @param platform plataforma a utilizar para extraer la posición
     * @return posición extraída de la plataforma para colocar un cocodrilo
     */
    private static Position positionFromPlatform(Platform platform) {
        Vines[] attached = platform.getAttached();
        assert attached != null && attached.length > 0;

        Bounds firstBounds = attached[0].getBounds();
        Integer x = firstBounds.getHorizontalCenter() - Animation.RED_CROCODILE_DOWN.getSize().getWidth() / 2;

        return new Position(x, firstBounds.getOrigin().getY());
    }
}
