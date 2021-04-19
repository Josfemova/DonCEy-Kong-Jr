package cr.ac.tec.ce3104.tc3.gameobjects;

public abstract class Fruit extends GameObject{
    private static Integer spriteWidth = 16;
    private static Integer spriteHeight = 16;
    public Fruit(Integer x, Integer y){
        super(x,y, spriteWidth, spriteHeight);
    }
}
