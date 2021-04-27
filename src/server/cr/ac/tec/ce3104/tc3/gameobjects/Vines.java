package cr.ac.tec.ce3104.tc3.gameobjects;

import java.util.Random;

import cr.ac.tec.ce3104.tc3.modes.Hanging;
import cr.ac.tec.ce3104.tc3.modes.Falling;
import cr.ac.tec.ce3104.tc3.modes.Climbing;
import cr.ac.tec.ce3104.tc3.modes.ControllableMode;
import cr.ac.tec.ce3104.tc3.physics.Bounds;
import cr.ac.tec.ce3104.tc3.physics.Dynamics;
import cr.ac.tec.ce3104.tc3.physics.Position;
import cr.ac.tec.ce3104.tc3.resources.Sprite;

public class Vines extends GameObject {
    /**
     * Crea una liana conformada por distintas celdas unitarias hasta llegar a una cantidad de celdas dada. Coloca dicha liana bajo la plataforma especificada
     * @param platform plataforma en la cual se desea crear la liana 
     * @param length cantidad de celdas unitarias que conforman la liana 
     * @return Arreglo que contiene las celdas unitarias que conforman la liana creada
     */
    public static Vines[] makeChain(Platform platform, Integer length) {
        assert length > 0;

        Integer unitHeight = Sprite.VINES.getSize().getHeight();
        Bounds platformBounds = platform.getBounds();
        Integer baseX = platformBounds.getHorizontalCenter() - Sprite.VINES.getSize().getWidth() / 2;
        Integer baseY = platformBounds.getBaseline();

        Vines[] vines = new Vines[length];
        Boolean random;
        for (Integer i = 0; i < length; ++i) {
            random = (Vines.randomGenerator.nextInt() % 5 ==0) ? true : false;
            Boolean withLeaf = i < length - 1 ? random : true;
            Sprite sprite = withLeaf ? Sprite.VINES_WITH_LEAF : Sprite.VINES;

            vines[i] = new Vines(platform, new Position(baseX, baseY + i * unitHeight), sprite);
        }

        return platform.attach(vines) ? vines : null;
    }

    @Override
    public Dynamics getDynamics() {
        return Dynamics.INTERACTIVE;
    }

    @Override
    public void onInteraction(GameObject other) {
        if (other instanceof PlayerAvatar) {
            ControllableMode mode = (ControllableMode)other.getMode();
            if (!(mode instanceof Climbing) && !(mode instanceof Hanging)
             && (!(mode instanceof Falling) || ((Falling)mode).getSourcePlatform() != this.platform)) {
                other.switchTo(new Hanging(mode.getDirection(), this.platform, (PlayerAvatar)other));
            }
        }
    }
    /**
     * Obtiene la plataforma en la cual se encuentra la celda unitaria de liana actual
     * @return
     */
    public Platform getPlatform() {
        return this.platform;
    }

    private static final Random randomGenerator = new Random();

    private Platform platform;
    /**
     * Construye una nueva celda unitaria de liana, dada una plataforma asociada, una posicion, y un sprite a utilizar para la celda unitaria
     * @param platform plataforma asociada a la celda unitaria
     * @param position posicion de la celda unitaria
     * @param sprite sprite que indica el aspecto estético de la celda unitaria(con o sin hojas)
     */
    private Vines(Platform platform, Position position, Sprite sprite) {
        super(sprite, position);
        this.platform = platform;
    }
}
