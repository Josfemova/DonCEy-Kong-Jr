package cr.ac.tec.ce3104.tc3.gameobjects;

public enum PlatformType {
    BRICK(40,false),
    DIRT(39, false),
    WATER1(37, true),
    WATER2(38, true),
    GRASS1(41, false),
    GRASS2(42, false),
    GRASS3(43, false);
    public final Integer spriteId;
    public final Boolean dangerous;

    private PlatformType(Integer spriteId,Boolean dangerous){
        this.spriteId = spriteId;
        this.dangerous = dangerous;
    }
}
