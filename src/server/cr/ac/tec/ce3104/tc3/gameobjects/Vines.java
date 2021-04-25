package cr.ac.tec.ce3104.tc3.gameobjects;

import cr.ac.tec.ce3104.tc3.physics.Bounds;
import cr.ac.tec.ce3104.tc3.physics.Dynamics;
import cr.ac.tec.ce3104.tc3.physics.Position;
import cr.ac.tec.ce3104.tc3.resources.Sprite;

public class Vines extends GameObject {
    public static Vines[] makeChain(Platform platform, Integer length) {
        assert length > 0;

        Integer unitHeight = Sprite.VINES.getSize().getHeight();
        Bounds platformBounds = platform.getBounds();
        Integer baseX = platformBounds.getHorizontalCenter() - Sprite.VINES.getSize().getWidth() / 2;
        Integer baseY = platformBounds.getBaseline();

        Vines[] vines = new Vines[length];
        for (Integer i = 0; i < length; ++i) {
            vines[i] = new Vines(platform, new Position(baseX, baseY + i * unitHeight));
        }

        return platform.attach(vines) ? vines : null;
    }

    @Override
    public Dynamics getDynamics() {
        return Dynamics.INTERACTIVE;
    }

    public Platform getPlatform() {
        return this.platform;
    }

    private Platform platform;

    private Vines(Platform platform, Position position) {
        super(Sprite.VINES, position);
        this.platform = platform;
    }
}
