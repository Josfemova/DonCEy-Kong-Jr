package cr.ac.tec.ce3104.tc3.physics;

public class Speed {
    public static Speed stationary() {
        Speed speed = new Speed();
        speed.x = SpeedRatio.stationary();
        speed.y = SpeedRatio.stationary();

        return speed;
    }

    public SpeedRatio getX() {
        return this.x;
    }

    public SpeedRatio getY() {
        return this.y;
    }

    private SpeedRatio x;
    private SpeedRatio y;

    private Speed() {}
}
