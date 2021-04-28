package cr.ac.tec.ce3104.tc3.physics;

// Velocidad (x, y)
public class Speed {
    /**
     * Crea una instancia de velocidad que representa un estado estacionario
     * @return instancia de Speed para estado estacionario
     */
    public static Speed stationary() {
        return new Speed(SpeedRatio.stationary(), SpeedRatio.stationary());
    }
    /**
     * crea una velocidad unicamente horizontal
     * @param horizontal valor de velocidad
     * @return referencia a objeto Speed creado a partir del valor dado
     */
    public static Speed horizontal(SpeedRatio horizontal) {
        return new Speed(horizontal, SpeedRatio.stationary());
    }
    /**
     * Crea una velocidad únicamente vertical
     * @param vertical valor de velocidad
     * @return referencia a objeto Speed creado a partir del valor dado
     */
    public static Speed vertical(SpeedRatio vertical) {
        return new Speed(SpeedRatio.stationary(), vertical);
    }
    /**
     * Construye una representación de velocidad compuesta a partir de velocidades en dos ejes
     * @param x valor de velocidad en eje horizontal
     * @param y valor de velocidad en eje vertical
     */
    public Speed(SpeedRatio x, SpeedRatio y) {
        this.x = x;
        this.y = y;
    }
    /**
     * Obtiene valor de velocidad para el eje horizontal
     * @return velocidad en eje horizontal
     */
    public SpeedRatio getX() {
        return this.x;
    }

    /**
     * Obtiene valor de velocidad para el eje vertical
     * @return velocidad en eje vertical
     */
    public SpeedRatio getY() {
        return this.y;
    }

    private SpeedRatio x;
    private SpeedRatio y;
}
