package cr.ac.tec.ce3104.tc3.gameobjects;

import cr.ac.tec.ce3104.tc3.Game;
import cr.ac.tec.ce3104.tc3.physics.Size;
import cr.ac.tec.ce3104.tc3.physics.Bounds;
import cr.ac.tec.ce3104.tc3.physics.Position;
import cr.ac.tec.ce3104.tc3.modes.BlueSearching;
import cr.ac.tec.ce3104.tc3.resources.Animation;

public class BlueCrocodile extends Crocodile {
    public BlueCrocodile(Game game, Platform platform, Integer difficulty) {
        super
        (
            new BlueSearching(game, Crocodile.getSpeedDenominator(difficulty)),
            BlueCrocodile.positionFromPlatform(platform)
        );
    }

    private static Position positionFromPlatform(Platform platform) {
        Bounds platformBounds = platform.getBounds();
        Size spriteSize = Animation.BLUE_CROCODILE_RIGHT.getSize();

        return new Position
        (
            platformBounds.getHorizontalCenter() - spriteSize.getWidth() / 2,
            platformBounds.getOrigin().getY() - spriteSize.getHeight()
        );
    }
}
