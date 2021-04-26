package cr.ac.tec.ce3104.tc3.gameobjects;

import cr.ac.tec.ce3104.tc3.modes.Mode;
import cr.ac.tec.ce3104.tc3.modes.Static;
import cr.ac.tec.ce3104.tc3.physics.Size;
import cr.ac.tec.ce3104.tc3.physics.Speed;
import cr.ac.tec.ce3104.tc3.physics.Bounds;
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

    public abstract Dynamics getDynamics();

    public Integer getId() {
        return this.id;
    }

    public Mode getMode() {
        return this.mode;
    }

    public Position getPosition() {
        return this.position;
    }

    public Size getSize() {
        return this.mode.getSequence().getSize();
    }

    public Bounds getBounds() {
        return new Bounds(this.position, this.getSize());
    }

    public Command makePutCommand() {
        return Command.cmdPut(this.id, this.position, this.mode.getSpeed(), this.mode.getSequence());
    }

    public Command makeDeleteCommand() {
        return Command.cmdDelete(this.id);
    }

    public void addObserver(GameObjectObserver observer) {
        assert this.observer == null;
        this.observer = observer;
    }

    public Boolean exists() {
        return this.observer != null;
    }

    public void switchTo(Mode mode) {
        this.mode = mode;
        if (this.observer != null) {
            this.observer.onObjectModeChanged(this);
        }
    }

    public void relocate(Position position) {
        this.position = position;
        this.mode.onRelocate(this);
    }

    public void delete() {
        if (this.observer != null) {
            this.observer.onObjectDeleted(this);
            this.observer = null;
        }
    }

    public void freeze() {
        if (!(this.mode instanceof Static)) {
            this.switchTo(new Static(this.mode.getSequence().freeze()));
        }
    }

    public void onInteraction(GameObject other) {}

    public void onFloatingContact(GameObject floating) {}

    public Boolean isDangerous() {
        return false;
    }

    protected void refreshMode() {
        this.switchTo(this.mode);
    }

    private static Integer nextId = 0;

    private Integer id;
    private Position position;
    private Mode mode;
    private GameObjectObserver observer = null;
}
