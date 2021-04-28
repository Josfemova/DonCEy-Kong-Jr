package cr.ac.tec.ce3104.tc3.physics;

import java.util.ArrayList;
import java.util.Collection;

import cr.ac.tec.ce3104.tc3.levels.Level;
import cr.ac.tec.ce3104.tc3.gameobjects.GameObject;

/**
 * Recolecta las implicaciones de hipotéticamente mover una entidad a una posición,
 * como colisiones e interacción con flotantes.
 */
public class Placement {
    /**
     * Crea un objeto que provee información sobre la posición de una entidad en el campo de juego
     * @param placed referencia a la entidad del juego
     * @param placedAt posicion en la que se encuentra la entidad
     * @param level nivel asociado a las entidades 
     * @param scene Colección de objetos que componen la escena de juego actual
     */
    public Placement(GameObject placed, Position placedAt, Level level, Collection<GameObject> scene) {
        this(placed, placedAt, level, scene, true);
    }
    /**
     * Crea un objeto que provee información sobre la posición de una entidad en el campo de juego. 
     * @param placed referencia a la entidad del juego
     * @param placedAt posicion en la que se encuentra la entidad
     * @param level nivel asociado a las entidades 
     * @param scene Colección de objetos que componen la escena de juego actual
     * @param correct indica si la posición de la entidad es correcta
     */
    public Placement(GameObject placed, Position placedAt, Level level, Collection<GameObject> scene, Boolean correct) {
        this(placed, new Bounds(placedAt, placed.getSize()), level, scene, correct);

        if (this.hitOrientation == null) {
            Integer nextVerticalDisplacement = Math.max(1, Math.abs(placed.getMode().getSpeed().getY().getNumerator()));
            Position nextPosition = new Position(placedAt.getX(), placedAt.getY() + nextVerticalDisplacement);
            Bounds nextBounds = new Bounds(nextPosition, this.bounds.getSize());

            // Caída libre ocurre si en dos movimientos consecutivos no existe un choque
            if (this.hitOrientation == null) {
                this.freeFall = new Placement(placed, nextBounds, level, scene, false).hitOrientation == null;
            }
        }
    }
    /**
     * Indica de qué dirección provino una interacción que desató un hit (colisión)
     * @return orinetación origen del hit
     */
    public Orientation getHitOrientation() {
        return this.hitOrientation;
    }

    /**
     * Obtiene con cual entidad se está interactuando
     * @return entidad con la que interactia la entidad asociada a la instancia actual
     */
    public GameObject getInteractionTarget() {
        return this.interactionTarget;
    }

    /**
     * Obtiene la colección de entidades flotantes con los que se ha interactuado
     * @return coleccion de entidades flotantes con las que se ha interactuado
     */
    public Collection<GameObject> getTouchedFloatings() {
        return this.touchedFloatings;
    }
    /**
     * Indica si la entidad de juego se encuentra en un estado de caída libre
     * @return true si la entidad se encuentra en caida libre, false de lo contrario
     */
    public Boolean inFreeFall() {
        return this.freeFall;
    }

    private GameObject placed;
    private Bounds bounds;
    private Orientation hitOrientation = null;
    private Integer bestDistanceSquare = null;
    private GameObject interactionTarget = null;
    private Collection<GameObject> touchedFloatings = new ArrayList<>();
    private Boolean freeFall = false;
    private Boolean correct;

    /**
     * Crea un objeto que provee información sobre la posición de una entidad en el campo de juego. 
     * @param placed referencia a la entidad del juego
     * @param Bounds caja de colision asociada a la entidad
     * @param level nivel asociado a las entidades 
     * @param scene Colección de objetos que componen la escena de juego actual
     * @param correct indica si la posición de la entidad es correcta
     */
    private Placement(GameObject placed, Bounds bounds, Level level, Collection<GameObject> scene, Boolean correct) {
        this.placed = placed;
        this.bounds = bounds;
        this.correct = correct;

        this.testWalls(level.getGameAreaSize());
        if (placed.getDynamics() != Dynamics.FLOATING) {
            this.testCollisions(scene);
        }
    }

    /**
     * Prueba si existen colisiones contra límites de la pantalla de juego 
     * @param gameAreaSize dimensiones del area de juego, dado como una resolución virtual
     */
    private void testWalls(Size gameAreaSize) {
        Integer areaWidth = gameAreaSize.getWidth();
        Integer areaHeight = gameAreaSize.getHeight();

        Bounds leftWall = new Bounds(new Position(-1, 0), new Size(1, areaHeight));
        Bounds rightWall = new Bounds(new Position(areaWidth, 0), new Size(1, areaHeight));
        Bounds bottomWall = new Bounds(new Position(0, areaHeight), new Size(areaWidth, 1));

        // Colisiones con paredes inherentes del mapa
        if (!this.bounds.rightOf(leftWall) || !this.bounds.leftOf(rightWall)) {
            this.tryHitOrientation(Orientation.HORIZONTAL);
        } else if (!this.bounds.aboveOf(bottomWall)) {
            Position current = this.placed.getPosition();
            Integer lowestValidY = areaHeight - this.bounds.getSize().getHeight();
            if (this.correct && current.getY() > lowestValidY) {
                this.placed.relocate(new Position(current.getX(), lowestValidY));
            }

            this.tryHitOrientation(Orientation.VERTICAL);
        }
    }
    /**
     * COmprueba colisiones contra el resto de entidades presentes en la escena de juego actual
     * @param scene colección de entidades que componen el nivel dibujado actualmente en pantalla
     */
    private void testCollisions(Collection<GameObject> scene) {
        Bounds beforeCollision = this.placed.getBounds();

        // Se buscan colisiones
        for (GameObject other : scene) {
            Bounds otherBounds = other.getBounds();
            if (other == this.placed || !this.bounds.collidesWith(otherBounds)) {
                continue;
            }

            // Casos de colisión/interacción
            switch (other.getDynamics()) {
                case RIGID:
                    // Se considera un posible cambio de orientación de choque
                    Orientation orientation = Orientation.HORIZONTAL;
                    Boolean above = beforeCollision.aboveOf(otherBounds);
                    Boolean below = beforeCollision.belowOf(otherBounds);

                    if (above) {
                        Integer lowestValidY = otherBounds.getOrigin().getY() - this.bounds.getSize().getHeight();
                        if (this.correct && beforeCollision.getBaseline() - 1 > lowestValidY) {
                            this.placed.relocate(new Position(beforeCollision.getOrigin().getX(), lowestValidY));
                        }
                    }

                    if (above || below) {
                        orientation = Orientation.VERTICAL;
                    }

                    this.tryHitOrientation(orientation);
                    break;

                case FLOATING:
                    this.touchedFloatings.add(other);
                    break;

                case INTERACTIVE:
                    this.tryInteractionTarget(other);
                    break;
            }
        }
    }
    /**
     * comprueba si existe una colisión en una orientación dada
     * @param hitOrientation orientación de la que proviene la posible colision
     */
    private void tryHitOrientation(Orientation hitOrientation) {
        // Las colisiones rígidas verticales tienen precedencia por sobre las horizontales
        if (this.hitOrientation == null || this.hitOrientation != Orientation.VERTICAL) {
            this.hitOrientation = hitOrientation;
        }
    }

    /**
     * Comprueba si se da una interacción/colisión con alguna otra entidad de juego, 
     * y resuelve que acciones se deben tomar en el contexto de juego, tal como
     * aumentar la cantidad de puntos del jugador, o reducir su cantidad de vidas
     * @param other entidad contra la cual se quiere comprobar si hay colision
     */
    private void tryInteractionTarget(GameObject other) {
        // Se prefiere siempre al target más cercano
        Integer deltaX = other.getPosition().getX() - this.bounds.getOrigin().getX();
        Integer deltaY = other.getPosition().getY() - this.bounds.getOrigin().getY();
        Integer distanceSquare = deltaX * deltaX + deltaY * deltaY;

        if (other.isDangerous() || this.bestDistanceSquare == null || distanceSquare <= this.bestDistanceSquare) {
            this.bestDistanceSquare = distanceSquare;
            this.interactionTarget = other;
        }
    }
}
