package cr.ac.tec.ce3104.tc3.modes;

import cr.ac.tec.ce3104.tc3.gameobjects.PlayerAvatar;
import cr.ac.tec.ce3104.tc3.physics.HorizontalDirection;

public interface ControllableMode extends Mode {
    /**
     * Obtiene en a qué direccionn mira el sprite del modo actual
     * @return dirección de sprite del modo actual
     */
    HorizontalDirection getDirection();

    /**
     * Indica como reaciona un modo ante un evento de liberacion de tecla
     * @param player Referencia al avatar de jugador
     */
    default void onRelease(PlayerAvatar player) {}
    
    /**
     * Indica como debe reaccionar el modo ante un evento Jump
     * @param player Referencia al avatar del jugador
     */
    default void onJump(PlayerAvatar player) {}
    
    /**
     * Indica como debe reaccionar el modo ante un evento de movimiento a la izquierda
     * @param player Referencia al avatar del jugador
     */
    default void onMoveLeft(PlayerAvatar player) {}
    
    /**
     * Indica como debe reaccionar el modo ante un evento de movimiento a la derecha
     * @param player Referencia al avatar del jugador
     */
    default void onMoveRight(PlayerAvatar player) {}
    
    /**
     * Indica como debe reaccionar el modo ante un evento de movimiento hacia arriba
     * @param player Referencia al avatar del jugador
     */
    default void onMoveUp(PlayerAvatar player) {}
    
    /**
     * Indica como debe reaccionar el modo ante un evento de movimiento hacia abajo
     * @param player Referencia al avatar del jugador
     */
    default void onMoveDown(PlayerAvatar player) {}
}
