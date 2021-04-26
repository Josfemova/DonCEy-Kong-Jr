package cr.ac.tec.ce3104.tc3.gameobjects;

public interface GameObjectObserver {
    /**
     * Indica que acciones tomar cuando la entidad dada es eliminada del escenario de juego
     * @param object entidad eliminada
     */
    void onObjectDeleted(GameObject object);
    
    /**
     * Indica que acciones tomar dado un cambio en el modo de la entidad dada 
     * @param object entidad cuyo modo ha cambiado
     */
    void onObjectModeChanged(GameObject object);

    /**
     * Agrega un mensaje para imprimir en la consola de administrador
     * @param message mensaje a imprimir en la consola de administrador
     */
    void log(String message);
}
