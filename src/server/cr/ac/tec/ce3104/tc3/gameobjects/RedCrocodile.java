package cr.ac.tec.ce3104.tc3.gameobjects;

import cr.ac.tec.ce3104.tc3.physics.Bounds;
import cr.ac.tec.ce3104.tc3.physics.Position;
import cr.ac.tec.ce3104.tc3.modes.RedCrawling;
import cr.ac.tec.ce3104.tc3.resources.Animation;

public class RedCrocodile extends Crocodile {
    public RedCrocodile(Platform platform) {
        super(new RedCrawling(platform), RedCrocodile.positionFromPlatform(platform));
    }

    private static Position positionFromPlatform(Platform platform) {
        Vines[] attached = platform.getAttached();
        assert attached != null && attached.length > 0;

        Bounds firstBounds = attached[0].getBounds();
        Integer x = firstBounds.getHorizontalCenter() - Animation.RED_CROCODILE_DOWN.getSize().getWidth() / 2;

        return new Position(x, firstBounds.getOrigin().getY());
    }
}
