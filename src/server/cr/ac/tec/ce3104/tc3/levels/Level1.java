package cr.ac.tec.ce3104.tc3.levels;

import cr.ac.tec.ce3104.tc3.Game;
import cr.ac.tec.ce3104.tc3.physics.Size;
import cr.ac.tec.ce3104.tc3.physics.Position;
import cr.ac.tec.ce3104.tc3.resources.Sprite;
import cr.ac.tec.ce3104.tc3.gameobjects.*;

public class Level1 implements Level {
    public Size getGameAreaSize() {
        //resolución original del NES
        //aspect ratio 16:15 en caso de querer escalar
        return new Size(256, 240);
    }

    @Override
    public PlayerAvatar setup(Game game, Integer initialScore) {

        //TODO
        /**game.spawn(new Platform(new Position(170, 105), PlatformType.GRASS2));
        game.spawn(new Platform(new Position(180, 105), PlatformType.WATER1));
        game.spawn(new Platform(new Position(200, 105), PlatformType.DIRT));
        game.spawn(Platform.makeGrass(new Position(210, 170), this, 3));

        Platform[] ps = Platform.repeat(new Position(70, 10), PlatformType.BRICK, 6);
        game.spawn(ps);
        game.spawn(Vines.makeChain(ps[0], 1));
        game.spawn(Vines.makeChain(ps[2], 3));
        game.spawn(Vines.makeChain(ps[4], 2));
        game.spawn(Vines.makeChain(ps[5], 1));
        game.spawn(new RedCrocodile(ps[2]));

        game.spawn(new Nispero(new Position(100, 115), 32));
        game.spawn(new Banana(new Position(85, 70), 50));*/
        
        Integer waterHeight = Sprite.WATER1.getSize().getHeight()*2;
        Integer screenChunks = getGameAreaSize().getWidth()/Sprite.BRICK.getSize().getWidth();
        Platform[][] platforms = new Platform[][]{
            //ceiling
            Platform.repeat(new Position(0, -8), PlatformType.BRICK, screenChunks),
            //plataformas de pasto
            Platform.makeGrass(new Position(-8, 216), this, 9,waterHeight),
            Platform.makeGrass(new Position(136, 208), this, 1,waterHeight),
            Platform.makeGrass(new Position(96, 200), this, 2,waterHeight),
            Platform.makeGrass(new Position(170, 200), this, 2,waterHeight),
            Platform.makeGrass(new Position(208, 200), this, 2,waterHeight),
            //agua
            Platform.repeat(new Position(1, 232), PlatformType.WATER1, screenChunks),
            Platform.repeat(new Position(1, 224), PlatformType.WATER2, screenChunks),
            //plataformas
            Platform.repeat(new Position(48, 152), PlatformType.BRICK, 6),
            Platform.repeat(new Position(192, 136), PlatformType.BRICK, 8),
            Platform.repeat(new Position(48, 112), PlatformType.BRICK, 4),
            Platform.repeat(new Position(144, 72), PlatformType.BRICK, 8),
            Platform.repeat(new Position(0, 64), PlatformType.BRICK, 19),
            Platform.repeat(new Position(72, 40), PlatformType.BRICK, 3),
        };
        for(Platform[] surface: platforms){
            game.spawn(surface);
        }


        game.spawn(new DonkeyKong(new Position(16, 64 - Sprite.DONKEY_KONG.getSize().getHeight())));

        return game.spawn(new PlayerAvatar(new Position(8, 192), initialScore));
    }
}
