package cr.ac.tec.ce3104.tc3.physics;

public class SpeedRatio {
    public static SpeedRatio stationary() {
        return new SpeedRatio();
    }

    public SpeedRatio(Integer numerator, Integer denominator) {
        this.numerator = numerator;
        this.denominator = denominator;
    }

    public Integer getNumerator() {
        return this.numerator;
    }

    public Integer getDenominator() {
        return this.denominator;
    }

    public SpeedRatio negate() {
        return new SpeedRatio(-this.numerator, this.denominator);
    }

    private Integer numerator = 0;
    private Integer denominator = 0;

    private SpeedRatio() {}
}
