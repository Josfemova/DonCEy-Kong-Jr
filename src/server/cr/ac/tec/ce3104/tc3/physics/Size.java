package cr.ac.tec.ce3104.tc3.physics;

public class Size {
    public Size(Integer width, Integer height) {
        this.width = width;
        this.height = height;
    }

    public Integer getWidth() {
        return this.width;
    }

    public Integer getHeight() {
        return this.height;
    }

    private Integer width;
    private Integer height;
}
