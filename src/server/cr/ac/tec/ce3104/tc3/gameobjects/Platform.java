package cr.ac.tec.ce3104.tc3.gameobjects;
public class Platform extends EnvironmentObject{
    static Integer width=8;
    static Integer height=8;
    public Integer sprite;
    PlatformType type;
    public Platform(Integer x0, Integer y0, PlatformType type){
        super(x0,y0,width, height);
        sprite = type.spriteId;
        this.defSprite = sprite;
        this.type=type;
    }
}
