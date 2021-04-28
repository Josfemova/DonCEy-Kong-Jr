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
    /**
     * Crea una nueva entidad a partir de un sprite y una posicion
     * @param staticSprite sprite de la entidad cuando la misma se enceuntra estatica
     * @param position 
     */
    public GameObject(Sprite staticSprite, Position position) {
        this(new Static(staticSprite), position);
    }
    /**
     * Crea una nueva entidad de juego a partir de un modo(estado) y una posicion
     * @param mode Modo de la entidad
     * @param position posicion de la entidad
     */
    public GameObject(Mode mode, Position position) {
        this.id = nextId++;
        this.position = position;
        this.mode = mode;
    }

    /**
     * COnstruye un string para describir de una manera simple una entidad de juego
     */
    @Override
    public String toString() {
        return this.getClass().getSimpleName() + " #" + this.id
             + " at (" + this.position.getX() + ", " + this.position.getY() + ")";
    }

    /**
     * Obtiene el id de la entidad representada(mismo para cliente y servidor)
     * @return identificador de la entidad
     */
    public Integer getId() {
        return this.id;
    }

    /**
     * Obtiene el modo actual en el que se encuentra la entidad
     * @return Modo actual de la entidad
     */
    public Mode getMode() {
        return this.mode;
    }

    /**
     * Obtiene la posicion actual de la entidad
     * @return posicion actual de la entidad
     */
    public Position getPosition() {
        return this.position;
    }
    /**
     * Obtiene las dimensiones horizontales y verticales de la entidad
     * @return dimensiones de la entidad
     */
    public Size getSize() {
        return this.mode.getSequence().getSize();
    }

    /**
     * Obtiene la caja de colisión de la entidad
     * @return caja de colisión de la entidad
     */
    public Bounds getBounds() {
        return new Bounds(this.position, this.getSize());
    }

    /**
     * Fabrica el comando para solicitar la creacion de la entidad en los clientes del juego
     * @return Comando para enviar a los distintos clientes
     */
    public Command makePutCommand() {
        return Command.cmdPut(this.id, this.position, this.getZ(), this.mode.getSpeed(), this.mode.getSequence());
    }
    /**
     * Fabrica el comando para solicitar la eliminación de la entidad actual de los clientes del juego
     * @return Comando para enviar a los distintos clientes
     */
    public Command makeDeleteCommand() {
        return Command.cmdDelete(this.id);
    }

    /**
     * Agrega un suscriptor para ser notificado cuando se den cambios en el estado de la entidad
     * @param observer observador a suscribir a la entidad
     */
    public void addObserver(GameObjectObserver observer) {
        assert this.observer == null;
        this.observer = observer;
    }

    /**
     * Indica si la entidad forma parte de algun juego activo
     * @return true si la entidad esta registrada en un juego, falso de lo contrario
     */
    public Boolean exists() {
        return this.observer != null;
    }
    /**
     * Cambia el modo de la entidad representada
     * @param newMode modo al que se quiere transicionar
     */
    public void switchTo(Mode newMode) {
        Mode previous = this.mode;
        this.mode = newMode;

        if (this.observer != null) {
            this.observer.onObjectModeChanged(this);
            if (previous.getClass() != newMode.getClass()) {
                String previousName = previous.getClass().getSimpleName();
                String newName = newMode.getClass().getSimpleName();

                this.observer.log("Object " + this + " switched from " + previousName + " to " + newName);
            }
        }
    }
    
    /**
     * Cambia la posición de la entidad a una nueva posición dada
     * @param position posicion a trasladar la entidad
     */
    public void relocate(Position position) {
        this.position = position;
        this.mode.onRelocate(this);
    }
    /**
     * Elimina los registros de la entidad actual de su observador, tecnicamente eliminándose a sí mismo
     */
    public void delete() {
        if (this.observer != null) {
            this.observer.onObjectDeleted(this);
            this.observer.log("Object " + this.id + " deleted");
            this.observer = null;
        }
    }

    /**
     * inmoviliza la entidad
     */
    public void freeze() {
        if (!(this.mode instanceof Static)) {
            this.switchTo(new Static(this.mode.getSequence().freeze()));
        }
    }

    /**
     * Obtiene el tipo de colisiones que se pueden tener con el objeto
     * @return typo de colision con el objeto
     */
    public abstract Dynamics getDynamics();

    /**
     * Indica la rutina a ejecutar cuando el objeto interactua con alguna otra entidad
     * @param other objeto con el que se da la interaccion
     */
    public void onInteraction(GameObject other) {}

    /**
     * Indica la rutina a ejecutar si la entidad entra en contacto con un objeto flotante
     * @param floating objeto flotante con el que se interactua
     */
    public void onFloatingContact(GameObject floating) {}

    /**
     * Indica si la entidad actual es capaz de dañar al jugador
     * @return true si la entidad puede dañar al jugador, false de lo contrario
     */
    public Boolean isDangerous() {
        return false;
    }

    /**
     * Obtiene la prioridad de dibujo de la entidad
     * @return prioridad de dibujo
     */
    protected Integer getZ() {
        return 0;
    }

    /**
     * Cambia la entidad a modo de refresco
     */
    protected void refreshMode() {
        this.switchTo(this.mode);
    }

    private static Integer nextId = 0;

    private Integer id;
    private Position position;
    private Mode mode;
    private GameObjectObserver observer = null;
}
