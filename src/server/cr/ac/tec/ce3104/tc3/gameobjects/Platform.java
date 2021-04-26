package cr.ac.tec.ce3104.tc3.gameobjects;

import cr.ac.tec.ce3104.tc3.levels.Level;
import cr.ac.tec.ce3104.tc3.physics.Size;
import cr.ac.tec.ce3104.tc3.physics.Dynamics;
import cr.ac.tec.ce3104.tc3.physics.Position;
import cr.ac.tec.ce3104.tc3.resources.Sprite;

public class Platform extends GameObject {
    public static Platform[] repeat(Position base, PlatformType type, Integer count) {
        Integer unitWidth = type.getSprite().getSize().getWidth();

        Platform[] combined = new Platform[count];
        for (Integer i = 0; i < count; ++i) {
            combined[i] = new Platform(new Position(base.getX() + i * unitWidth, base.getY()), type);
        }

        return combined;
    }

    public static Platform[] makeGrass(Position base, Level level, Integer plainLength) {
        Integer leftEdgeWidth = Sprite.GRASS1.getSize().getWidth();
        Size plainSize = Sprite.GRASS2.getSize();
        Integer plainWidth = plainSize.getWidth();
        Integer plainHeight = plainSize.getWidth();
        Integer dirtHeight = Sprite.DIRT.getSize().getHeight();

        Integer dirtPerPlain = Math.max(0, (level.getGameAreaSize().getHeight() - base.getY() + dirtHeight) / dirtHeight - 1);
        Platform[] grass = new Platform[1 + plainLength * (1 + dirtPerPlain) + 1];
        grass[0] = new Platform(base, PlatformType.GRASS1);

        // Cada uno de los segmentos intermedios
        for (Integer i = 0; i < plainLength; ++i) {
            Integer baseIndex = 1 + i * (1 + dirtPerPlain);
            Integer x = base.getX() + leftEdgeWidth + i * plainWidth;

            grass[baseIndex] = new Platform(new Position(x, base.getY()), PlatformType.GRASS2);

            // Bloques de tierra
            for (Integer j = 0; j < dirtPerPlain; ++j) {
                Position dirtPosition = new Position(x, base.getY() + plainHeight + j * dirtHeight);
                grass[baseIndex + 1 + j] = new Platform(dirtPosition, PlatformType.DIRT);
            }
        }

        Position rightEdgePosition = new Position(base.getX() + leftEdgeWidth + plainLength * plainWidth, base.getY());
        grass[grass.length - 1] = new Platform(rightEdgePosition, PlatformType.GRASS3);

        return grass;
    }

    public Platform(Position position, PlatformType type) {
        super(type.getSprite(), position);
        this.type = type;
    }

    @Override
    public Dynamics getDynamics() {
        if (this.type == PlatformType.DIRT) {
            // La tierra solo se dibuja por estética
            return Dynamics.FLOATING;
        } else {
            return this.type.isDangerous() ? Dynamics.INTERACTIVE : Dynamics.RIGID;
        }
    }

    @Override
    public Boolean isDangerous() {
        return this.type.isDangerous();
    }

    public boolean attach(Vines[] vines) {
        if (this.getDynamics() != Dynamics.RIGID || this.vines != null) {
            return false;
        }

        this.vines = vines;
        return true;
    }

    public void detach() {
        if (this.vines != null) {
            for (Vines object : this.vines) {
                object.delete();
            }

            this.vines = null;
        }
    }

    public Vines[] getAttached() {
        return this.vines;
    }

    private PlatformType type;
    private Vines[] vines = null;
}
