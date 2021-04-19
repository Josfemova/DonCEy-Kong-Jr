package cr.ac.tec.ce3104.tc3.gameobjects;
public abstract class GameObject{
    private Integer x1;
    private Integer y1;
    private Integer x2;
    private Integer y2;
    
    public GameObject(Integer x, Integer y, Integer width, Integer height){
        this.x1 = x;
        this.y1 = y;
        this.x2 = x + width;
        this.y2 = y + height;
    }
    public Integer[] getCollisionBox(){
        return new Integer[]{x1,y1,x2,y2};
    }
    
    public Boolean collides(GameObject gameObject){
        Integer objectcoords[] = gameObject.getCollisionBox();
        Boolean collides = false;
        Boolean rightCollision = (x1 <= objectcoords[0] && objectcoords[0] <= x2);
        Boolean leftCollision = (x1 <= objectcoords[2] && objectcoords[2]<= x2);
        Boolean upCollision = (y1 <= objectcoords[1] && objectcoords[1] <= y2);
        Boolean downCollision = (y1 <= objectcoords[3] && objectcoords[3] <= y2);
        if((rightCollision || leftCollision)&&(upCollision || downCollision))
            collides = true;  
        return collides;
    }
}