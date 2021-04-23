package cr.ac.tec.ce3104.tc3.physics;

public class Position {
    public Position(Integer x, Integer y) {
        this.x = x;
        this.y = y;
    }

    public Integer getX() {
        return this.x;
    }

    public Integer getY() {
        return this.y;
    }

    private Integer x;
    private Integer y;
}
