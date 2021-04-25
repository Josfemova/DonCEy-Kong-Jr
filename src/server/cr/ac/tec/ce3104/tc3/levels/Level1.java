package cr.ac.tec.ce3104.tc3.levels;

import cr.ac.tec.ce3104.tc3.Game;
import cr.ac.tec.ce3104.tc3.physics.Size;
import cr.ac.tec.ce3104.tc3.physics.Position;
import cr.ac.tec.ce3104.tc3.resources.Sprite;
import cr.ac.tec.ce3104.tc3.gameobjects.Vines;
import cr.ac.tec.ce3104.tc3.gameobjects.Banana;
import cr.ac.tec.ce3104.tc3.gameobjects.Nispero;
import cr.ac.tec.ce3104.tc3.gameobjects.Platform;
import cr.ac.tec.ce3104.tc3.gameobjects.DonkeyKong;
import cr.ac.tec.ce3104.tc3.gameobjects.PlatformType;
import cr.ac.tec.ce3104.tc3.gameobjects.PlayerAvatar;

public class Level1 implements Level {
    public Size getGameAreaSize() {
        //resolución original del NES
        //aspect ratio 16:15 en caso de querer escalar
        return new Size(256, 240);
    }

    @Override
    public PlayerAvatar setup(Game game, Integer initialScore) {
        //TODO
        game.spawn(new DonkeyKong(new Position(40, 170 - Sprite.DONKEY_KONG.getSize().getHeight())));
        game.spawn(new Platform(new Position(170, 105), PlatformType.GRASS2));
        game.spawn(new Platform(new Position(180, 105), PlatformType.WATER1));
        game.spawn(Platform.makeGrass(new Position(210, 170), this, 3));

        Platform[] ps = Platform.repeat(new Position(80, 50), PlatformType.BRICK, 2);
        game.spawn(ps);
        game.spawn(Vines.makeChain(ps[0], 1));
        game.spawn(Vines.makeChain(ps[1], 2));

        game.spawn(new Nispero(new Position(100, 115), 32));
        game.spawn(new Banana(new Position(85, 70), 50));

        game.spawn(Platform.repeat(new Position(90, 130), PlatformType.BRICK, 15));
        game.spawn(Platform.repeat(new Position(40, 170), PlatformType.BRICK, 15));

        return game.spawn(new PlayerAvatar(new Position(120, 100), initialScore));
    }
}
