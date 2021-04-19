package cr.ac.tec.ce3104.tc3.gameobjects;

public abstract class Crocodile extends GameObject {
    private static Integer spriteWidth = 32;
    private static Integer spriteHeight = 32;
    public Crocodile(Integer x, Integer y){
        super(x,y, spriteWidth, spriteHeight);
    }
}
