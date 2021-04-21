package cr.ac.tec.ce3104.tc3.gameobjects;
public class PlayerAvatar extends GameObject{
    Integer walkingSprite[] = {4,5,6,7};
    Integer jumSprite[] = {7,8};//jump-fall
    Integer climbingSprite[] = {9,10};
    Integer doubleclimbingSprite=11;
    Integer fallDie[] = {12,13};
    static Integer spriteWidth[]={32,16};
    static Integer spriteHeight=16;

    public PlayerAvatar(Integer x0, Integer y0){
        super(x0,y0,spriteWidth[0], spriteHeight);
        this.defSprite = walkingSprite[0];
    }
    /**
     * intercambia el ancho del sprite entre 32 y 16
     */
    public void switchWidth(){
        this.x2 = ((this.x2-this.x1) == spriteWidth[0]) ? this.x1 + spriteWidth[1] : this.x1 + spriteWidth[0];
    }
}
