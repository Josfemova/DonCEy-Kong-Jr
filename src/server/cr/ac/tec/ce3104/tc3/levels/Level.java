package cr.ac.tec.ce3104.tc3.levels;

import cr.ac.tec.ce3104.tc3.Game;
import cr.ac.tec.ce3104.tc3.physics.Size;
import cr.ac.tec.ce3104.tc3.gameobjects.PlayerAvatar;

public interface Level {
    Size getGameAreaSize();

    PlayerAvatar setup(Game game);
}
