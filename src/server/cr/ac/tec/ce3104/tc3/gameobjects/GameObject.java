package cr.ac.tec.ce3104.tc3.gameobjects;

import org.json.simple.JSONObject;

public abstract class GameObject{
    protected Integer x1;
    protected Integer y1;
    protected Integer x2;
    protected Integer y2;
    protected Integer id;
    protected static Integer nextid=0;
    protected Integer defSprite;
    
    public GameObject(Integer x, Integer y, Integer width, Integer height){
        this.x1 = x;
        this.y1 = y;
        this.x2 = x + width;
        this.y2 = y + height;
        this.id = nextid;
    }
    public Integer[] getCollisionBox(){
        return new Integer[]{x1,y1,x2,y2};
    }
    public void onStartUp(){
        //maneja la instruccion inicial de dibujo
    }
    public void onTick(){
        //No es requisito implementar para todos
        //Maneja animaciones
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
    public String getDrawCommand(){
        return String.format("{\"op\":\"new\", \"id\":%d,\"sprite\":%d, \"x\":%d,\"y\":%d}",
                            id, defSprite, this.x1, this.y1);
    }
}