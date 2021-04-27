package cr.ac.tec.ce3104.tc3.physics;

public class SpeedRatio {
    /**
     * Crea una representación de velocidad para un objeto estacionario
     * @return velocidad para objeto estacionario
     */
    public static SpeedRatio stationary() {
        return new SpeedRatio();
    }
    /**
     * Crea una velocidad representada como una razón entre una cantidad de movimiento(desplazamiento), y una cantidad de tiempo
     * @param numerator cantidad de movimiento
     * @param denominator cantidad de tiempo
     */
    public SpeedRatio(Integer numerator, Integer denominator) {
        this.numerator = numerator;
        this.denominator = denominator;
    }
    /**
     * Obtiene cantidad de movimiento característica de la velocidad
     * @return cantidad de movimiento asociada a la velocidad
     */
    public Integer getNumerator() {
        return this.numerator;
    }
    /**
     * Obtiene la cantidad de tiempo característica de la velocidad
     * @return cantidad de tiempo caracterśitica de la velocidad
     */
    public Integer getDenominator() {
        return this.denominator;
    }
    /**
     * Invierte la dirección del movimiento asociado a la velocidad
     * @return representación de la velocidad actual invertida
     */
    public SpeedRatio negate() {
        return new SpeedRatio(-this.numerator, this.denominator);
    }

    private Integer numerator = 0;
    private Integer denominator = 0;
    /**
     * constructor utilizado para representaciones de velocidades nulas
     */
    private SpeedRatio() {}
}
