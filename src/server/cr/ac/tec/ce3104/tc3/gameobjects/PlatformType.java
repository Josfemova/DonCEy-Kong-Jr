package cr.ac.tec.ce3104.tc3.gameobjects;

import cr.ac.tec.ce3104.tc3.resources.Sprite;

// Tipo de plataforma/terreno
public enum PlatformType {
    BRICK(Sprite.BRICK, false),
    DIRT(Sprite.DIRT, false),
    WATER1(Sprite.WATER1, true),
    WATER2(Sprite.WATER2, true),
    GRASS1(Sprite.GRASS1, false),
    GRASS2(Sprite.GRASS2, false),
    GRASS3(Sprite.GRASS3, false);

    /**
     * Obtiene el sprite asociado al tipo de plataforma
     */
    public Sprite getSprite() {
        return this.sprite;
    }
    /**
     * Indica si la plataforma es peligrosa para el jugador
     * @return true si la plataforma puede ocasionar daño al jugador, false de lo contrario
     */
    public Boolean isDangerous() {
        return this.dangerous;
    }

    private final Sprite sprite;
    private final Boolean dangerous;

    /**
     * Constructor que permite generar los valors del enum con valores importantes asociados
     * @param sprite sprite asociado al tipo de plataforma
     * @param dangerous booleano que indica si la plataforma representa un peligro para el jugador
     */
    private PlatformType(Sprite sprite, Boolean dangerous){
        this.sprite = sprite;
        this.dangerous = dangerous;
    }
}
