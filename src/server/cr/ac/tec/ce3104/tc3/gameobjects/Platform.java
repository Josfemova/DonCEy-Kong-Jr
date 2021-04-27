package cr.ac.tec.ce3104.tc3.gameobjects;

import cr.ac.tec.ce3104.tc3.levels.Level;
import cr.ac.tec.ce3104.tc3.physics.Size;
import cr.ac.tec.ce3104.tc3.physics.Dynamics;
import cr.ac.tec.ce3104.tc3.physics.Position;
import cr.ac.tec.ce3104.tc3.resources.Sprite;

public class Platform extends GameObject {
    /**
     * Crea una serie de plataformas consecutivas dada una posición inicial, el tipo de plataforma a repetir y la cantidad de celdas unitarias que deben crearse
     * @param base posicion de la primera celda de la pltaforma
     * @param type tipo de la celda unitaria de la plataforma
     * @param count extensión de la plataforma, dada en cantidad de celdas unitarias
     * @return Arreglo que contiene una serie de plataformas que se pueden registrar en un juego
     */
    public static Platform[] repeat(Position base, PlatformType type, Integer count) {
        Integer unitWidth = type.getSprite().getSize().getWidth();

        Platform[] combined = new Platform[count];
        for (Integer i = 0; i < count; ++i) {
            combined[i] = new Platform(new Position(base.getX() + i * unitWidth, base.getY()), type);
        }

        return combined;
    }
    /**
     * Crea plataformas de pasto, las cuales en el juego tienen una base de tierra que llega hasta el suelo o plataforma más cercana
     * @param base Posición en la que inicia la plataforma de pasto
     * @param level Nivel en el cual se quiere colocar la plataforma
     * @param plainLength Longitud de la plataforma, en cantidad de celdas unitarias en el centro
     * @param groundHeight Distancia del la plataforma al suelo más próximo
     * @return Arreglo que contiene una serie de plataformas para ser agregadas a un juego
     */
    public static Platform[] makeGrass(Position base, Level level, Integer plainLength, Integer groundHeight) {
        Integer leftEdgeWidth = Sprite.GRASS1.getSize().getWidth();
        Size plainSize = Sprite.GRASS2.getSize();
        Integer plainWidth = plainSize.getWidth();
        Integer plainHeight = plainSize.getWidth();
        Integer dirtHeight = Sprite.DIRT.getSize().getHeight();

        Integer dirtPerPlain = Math.max(0, (level.getGameAreaSize().getHeight() - base.getY() + dirtHeight - groundHeight) / dirtHeight - 2);
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
    /**
     * Construye una instancia de plataforma dada una posición e indicado un tipo de plataforma
     * @param position posición en la que se coloca la plataforma (según su esquina superior izquierda)
     * @param type tipo de plataforma a crear
     */
    public Platform(Position position, PlatformType type) {
        super(type.getSprite(), position);
        this.type = type;
    }

    @Override
    public Dynamics getDynamics() {
        switch (this.type) {
            case DIRT:
            case GRASS1:
            case GRASS3:
                // Solo se dibujan por estética
                return Dynamics.FLOATING;

            default:
                return this.type.isDangerous() ? Dynamics.INTERACTIVE : Dynamics.RIGID;
        }
    }

    @Override
    public Boolean isDangerous() {
        return this.type.isDangerous();
    }

    @Override
    public void delete() {
        this.detach();
        super.delete();
    }

    /**
     * Agrega una serie de celdas unitarias de liana que conforman una sola unidad para ser registradas como las lianas del cuadro actual
     * @param vines celdas unitarias que componen la liana
     * @return true si se lograron colocar las lianas, falso si hubo un error por presencia previa de lianas, o porque el tipo de plataforma no permite lianas debajo suyo
     */
    public boolean attach(Vines[] vines) {
        if (this.getDynamics() != Dynamics.RIGID || this.vines != null) {
            return false;
        }

        this.vines = vines;
        return true;
    }
    /**
     * Si la plataforma tiene una liana asociada, eliminar las unidades que componen la misma
     * @return true si la plataforma contenía lianas y las eliminó, false de lo contrario
     */
    public Boolean detach() {
        if (this.vines != null) {
            for (Vines object : this.vines) {
                object.delete();
            }

            this.vines = null;
            return true;
        }

        return false;
    }

    public Vines[] getAttached() {
        return this.vines;
    }

    private PlatformType type;
    private Vines[] vines = null;
}
