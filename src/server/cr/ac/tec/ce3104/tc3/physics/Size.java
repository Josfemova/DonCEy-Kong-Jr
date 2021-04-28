package cr.ac.tec.ce3104.tc3.physics;

// Tamaño (ancho, alto)
public class Size {
    /**
     * Representa las dimensiones de un objeto
     * @param width ancho del objeto
     * @param height altura del objeto
     */
    public Size(Integer width, Integer height) {
        this.width = width;
        this.height = height;
    }
    /**
     * Obtiene el ancho 
     * @return ancho
     */
    public Integer getWidth() {
        return this.width;
    }
    /**
     * Obtiene el largo
     * @return largo
     */
    public Integer getHeight() {
        return this.height;
    }

    private Integer width;
    private Integer height;
}
