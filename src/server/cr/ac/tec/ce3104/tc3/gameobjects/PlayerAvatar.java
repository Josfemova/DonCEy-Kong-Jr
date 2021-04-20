package cr.ac.tec.ce3104.tc3.gameobjects;
public class PlayerAvatar extends GameObject{
    Integer walkingSprite[] = {4,5,6,7};
    Integer jumSprite[] = {7,8};//jump-fall
    Integer climbingSprite[] = {9,10};
    Integer doubleclimbingSprite=11;
    Integer fallDie[] = {12,13};
    public PlayerAvatar(Integer x0, Integer y0, Integer xf, Integer yf ){
        super(x0,y0,xf,yf);
    }
}
