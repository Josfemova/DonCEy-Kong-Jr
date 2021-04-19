package cr.ac.tec.ce3104.tc3.gameobjects;
public abstract class GameObject{
    private Integer bbox[];
    public GameObject(Integer x0, Integer y0, Integer xf, Integer yf){
        bbox = new Integer[]{x0,y0,xf,yf};
    }
    public Integer[] getCollisionBox(){
        return bbox;
    }
    public Boolean collides(GameObject gameObject){
        return true;
    }
}