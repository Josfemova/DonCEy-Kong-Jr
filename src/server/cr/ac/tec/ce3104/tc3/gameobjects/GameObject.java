package cr.ac.tec.ce3104.tc3.gameobjects;

import cr.ac.tec.ce3104.tc3.modes.Mode;
import cr.ac.tec.ce3104.tc3.modes.Static;
import cr.ac.tec.ce3104.tc3.physics.Speed;
import cr.ac.tec.ce3104.tc3.physics.Dynamics;
import cr.ac.tec.ce3104.tc3.physics.Position;
import cr.ac.tec.ce3104.tc3.networking.Command;
import cr.ac.tec.ce3104.tc3.resources.Sprite;
import cr.ac.tec.ce3104.tc3.resources.Sequence;

public abstract class GameObject {
    public GameObject(Sprite staticSprite, Position position) {
        this(new Static(staticSprite), position);
    }

    public GameObject(Mode mode, Position position) {
        this.id = nextId++;
        this.position = position;
        this.mode = mode;
    }

    public abstract Dynamics getInteractionMode();

    public Integer getId() {
        return this.id;
    }

    public Command makePutCommand() {
        return Command.cmdPut(this.id, this.position, this.mode.getSpeed(), this.mode.getSequence());
    }

    public Mode getMode() {
        return this.mode;
    }

    public void addObserver(GameObjectObserver observer) {
        assert this.observer == null;
        this.observer = observer;
    }

    public void switchTo(Mode mode) {
        this.mode = mode;
        if (this.observer != null) {
            this.observer.onObjectModeChanged(this);
        }
    }

    public Boolean collides(GameObject gameObject){
        return false; //TODO
        /*Integer objectcoords[] = gameObject.getCollisionBox();/
        Boolean collides = false;
        Boolean rightCollision = (x1 <= objectcoords[0] && objectcoords[0] <= x2);
        Boolean leftCollision = (x1 <= objectcoords[2] && objectcoords[2]<= x2);
        Boolean upCollision = (y1 <= objectcoords[1] && objectcoords[1] <= y2);
        Boolean downCollision = (y1 <= objectcoords[3] && objectcoords[3] <= y2);
        if ((rightCollision || leftCollision)&&(upCollision || downCollision))
            collides = true;  
        return collides;*/
    }

    private static Integer nextId = 0;

    private Integer id;
    private Position position;
    private Mode mode;
    private GameObjectObserver observer = null;
}
