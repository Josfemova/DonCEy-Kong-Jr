package cr.ac.tec.ce3104.tc3.physics;

// Posición (x, y)
public class Position {
    /**
     * Crea una representación de coordenadas
     * @param x valor componente horizontal
     * @param y valor componente vertical 
     */
    public Position(Integer x, Integer y) {
        this.x = x;
        this.y = y;
    }
    /**
     * Obtiene componente horizontal de coordenada
     * @return componente horizontal
     */
    public Integer getX() {
        return this.x;
    }
    /**
     * Obtiene componente vertical de coordenada
     * @return componente vertical
     */
    public Integer getY() {
        return this.y;
    }

    private Integer x;
    private Integer y;
}
