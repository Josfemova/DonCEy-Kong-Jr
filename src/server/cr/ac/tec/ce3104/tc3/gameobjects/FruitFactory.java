package cr.ac.tec.ce3104.tc3.gameobjects;
public class FruitFactory {
    public static Fruit createFruit(Integer x0, Integer y0, Integer xf, Integer yf, FruitType type){
        switch(type){
            case NISPERO:
                return new Nispero(x0,y0, xf, yf);
            case APPLE:
                return new Apple(x0,y0, xf, yf);
            case BANANA:
                return new Banana(x0,y0, xf, yf);
            default:
                return new Banana(x0,y0, xf, yf);
        }
    }
}
