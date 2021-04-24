package cr.ac.tec.ce3104.tc3.modes;

import cr.ac.tec.ce3104.tc3.gameobjects.PlayerAvatar;

public interface ControllableMode extends Mode {
    default void onRelease(PlayerAvatar player) {}

    default void onJump(PlayerAvatar player) {}

    default void onMoveLeft(PlayerAvatar player) {}

    default void onMoveRight(PlayerAvatar player) {}

    default void onMoveUp(PlayerAvatar player) {}

    default void onMoveDown(PlayerAvatar player) {}
}
