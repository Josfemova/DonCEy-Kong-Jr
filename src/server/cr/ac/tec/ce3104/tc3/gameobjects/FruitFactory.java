package cr.ac.tec.ce3104.tc3.gameobjects;
public class FruitFactory {
    public static Fruit createFruit(Integer x, Integer y, FruitType type){
        switch(type){
            case NISPERO:
                return new Nispero(x,y);
            case APPLE:
                return new Apple(x,y);
            case BANANA:
                return new Banana(x,y);
            default:
                return new Banana(x,y);
        }
    }
}
