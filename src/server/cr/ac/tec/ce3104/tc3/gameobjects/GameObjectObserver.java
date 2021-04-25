package cr.ac.tec.ce3104.tc3.gameobjects;

public interface GameObjectObserver {
    void onObjectDeleted(GameObject object);

    void onObjectModeChanged(GameObject object);
}
