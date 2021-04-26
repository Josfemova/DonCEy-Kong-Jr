package cr.ac.tec.ce3104.tc3.modes;

import cr.ac.tec.ce3104.tc3.physics.Speed;
import cr.ac.tec.ce3104.tc3.physics.Orientation;
import cr.ac.tec.ce3104.tc3.resources.Sequence;
import cr.ac.tec.ce3104.tc3.gameobjects.GameObject;

public interface Mode {
    Speed getSpeed();

    Sequence getSequence();
    /**
     * Funcion que maneja eventos de relocalizacion
     * @param object objeto de juego que se quiere manejar
     */
    default void onRelocate(GameObject object) {}

    /**
     * Indica como un modo de responder antes una situacion de caida libre
     * @param object objeto de juego en dicho modo
     */
    default void onFreeFall(GameObject object) {}

    /**
     * Indica como se administra un objeto dado que el mismo encuentre una colision con un objeto peligroso
     * @param object objeto a administrar
     * @param orientation direccion caracteristica del modo
     */    
    default void onHit(GameObject object, Orientation orientation) {}
}
