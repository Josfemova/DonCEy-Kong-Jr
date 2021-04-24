package cr.ac.tec.ce3104.tc3.physics;

public class Speed {
    public static Speed stationary() {
        return new Speed(SpeedRatio.stationary(), SpeedRatio.stationary());
    }

    public static Speed horizontal(SpeedRatio horizontal) {
        return new Speed(horizontal, SpeedRatio.stationary());
    }

    public static Speed vertical(SpeedRatio vertical) {
        return new Speed(SpeedRatio.stationary(), vertical);
    }

    public SpeedRatio getX() {
        return this.x;
    }

    public SpeedRatio getY() {
        return this.y;
    }

    private SpeedRatio x;
    private SpeedRatio y;

    private Speed(SpeedRatio x, SpeedRatio y) {
        this.x = x;
        this.y = y;
    }
}
