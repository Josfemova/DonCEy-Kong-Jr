package cr.ac.tec.ce3104.tc3.gameobjects;

import cr.ac.tec.ce3104.tc3.resources.Sprite;

public enum PlatformType {
    BRICK(Sprite.BRICK, false),
    DIRT(Sprite.DIRT, false),
    WATER1(Sprite.WATER1, true),
    WATER2(Sprite.WATER2, true),
    GRASS1(Sprite.GRASS1, false),
    GRASS2(Sprite.GRASS2, false),
    GRASS3(Sprite.GRASS3, false);

    public Sprite getSprite() {
        return this.sprite;
    }

    public Boolean isDangerous() {
        return this.dangerous;
    }

    private final Sprite sprite;
    private final Boolean dangerous;

    private PlatformType(Sprite sprite, Boolean dangerous){
        this.sprite = sprite;
        this.dangerous = dangerous;
    }
}
