package cr.ac.tec.ce3104.tc3.levels;

import cr.ac.tec.ce3104.tc3.Game;
import cr.ac.tec.ce3104.tc3.physics.Size;
import cr.ac.tec.ce3104.tc3.physics.Position;
import cr.ac.tec.ce3104.tc3.gameobjects.Nispero;
import cr.ac.tec.ce3104.tc3.gameobjects.PlayerAvatar;

public class Level1 implements Level {
    public Size getGameAreaSize() {
        //resolución original del NES
        //aspect ratio 16:15 en caso de querer escalar
        return new Size(256, 240);
    }

    @Override
    public PlayerAvatar setup(Game game) {
        //TODO
        game.spawn(new Nispero(new Position(50, 50), 32));

        PlayerAvatar player = new PlayerAvatar(new Position(100, 100));
        game.spawn(player);

        return player;
    }
}
