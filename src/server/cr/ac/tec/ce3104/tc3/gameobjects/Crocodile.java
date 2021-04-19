package cr.ac.tec.ce3104.tc3.gameobjects;

public abstract class Crocodile extends GameObject {
    protected static Integer spriteWidth = 32;
    protected static Integer spriteHeight = 32;
    public Crocodile(Integer x, Integer y){
        super(x,y, spriteWidth, spriteHeight);
    }
}
