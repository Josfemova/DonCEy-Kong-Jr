package cr.ac.tec.ce3104.tc3.gameobjects;

public class CrocodileFactory {
    public static Crocodile createCrocodile(Integer x, Integer y, CrocodileType type){
        switch(type){
            case RED:
                return new RedCrocodile(x,y);
            case BLUE:
                return new BlueCrocodile(x,y);
            default:
                return new BlueCrocodile(x,y);
        }
    }
}
