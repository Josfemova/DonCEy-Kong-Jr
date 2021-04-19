package cr.ac.tec.ce3104.tc3.gameobjects;

public abstract class Fruit extends GameObject{
    protected static Integer spriteWidth = 16;
    protected static Integer spriteHeight = 16;
    public Fruit(Integer x, Integer y){
        super(x,y, spriteWidth, spriteHeight);
    }
}
