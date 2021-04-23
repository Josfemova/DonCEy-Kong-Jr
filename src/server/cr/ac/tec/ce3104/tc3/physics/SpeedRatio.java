package cr.ac.tec.ce3104.tc3.physics;

public class SpeedRatio {
    public static SpeedRatio stationary() {
        return new SpeedRatio();
    }

    public Integer getNumerator() {
        return this.numerator;
    }

    public Integer getDenominator() {
        return this.denominator;
    }

    private Integer numerator = 0;
    private Integer denominator = 0;

    private SpeedRatio() {}
}
