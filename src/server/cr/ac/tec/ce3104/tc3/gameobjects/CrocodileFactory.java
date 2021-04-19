package cr.ac.tec.ce3104.tc3.gameobjects;

public class CrocodileFactory {
    public static Crocodile createCrocodile(Integer x0, Integer y0, Integer xf, Integer yf, CrocodileType type){
        switch(type){
            case RED:
                return new RedCrocodile(x0,y0, xf, yf);
            case BLUE:
                return new BlueCrocodile(x0,y0, xf, yf);
            default:
                return new BlueCrocodile(x0,y0, xf, yf);
        }
    }
}
