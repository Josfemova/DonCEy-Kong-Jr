package cr.ac.tec.ce3104.tc3.gameobjects;

import cr.ac.tec.ce3104.tc3.Game;
import cr.ac.tec.ce3104.tc3.modes.Mode;
import cr.ac.tec.ce3104.tc3.modes.Falling;
import cr.ac.tec.ce3104.tc3.modes.Running;
import cr.ac.tec.ce3104.tc3.modes.Hanging;
import cr.ac.tec.ce3104.tc3.modes.Climbing;
import cr.ac.tec.ce3104.tc3.modes.Standing;
import cr.ac.tec.ce3104.tc3.modes.ControllableMode;
import cr.ac.tec.ce3104.tc3.resources.Sprite;
import cr.ac.tec.ce3104.tc3.physics.Dynamics;
import cr.ac.tec.ce3104.tc3.physics.Position;
import cr.ac.tec.ce3104.tc3.physics.Placement;
import cr.ac.tec.ce3104.tc3.physics.HorizontalDirection;

public class PlayerAvatar extends GameObject{
    /**
     * Constructor para generar la entidad que representa al jugador de la partida
     * @param position posicion inicial del jugador
     * @param initialScore puntaje inicial asociado al jugador
     * @param game juego en el que se encuentra el jugador
     */
    public PlayerAvatar(Position position, Integer initialScore, Game game) {
        super(Standing.initial(), position);
        this.score = initialScore;
        this.game = game;
    }

    @Override
    public Dynamics getDynamics() {
        return this.lost ? Dynamics.FLOATING : Dynamics.INTERACTIVE;
    }

    /**
     * @brief Cambia el modo de dinámica del jugador.
     */
    @Override
    public void switchTo(Mode newMode) {
        if (this.getMode() != newMode && newMode instanceof Running && this.lastVines != null) {
            Placement placement = this.game.testCollisions(this, this.getPosition());

            GameObject target = placement.getInteractionTarget();
            if (target == null || !(target instanceof Vines) || !this.inLastVines((Vines)target)) {
                this.lastVines = null;
            }
        }

        super.switchTo(newMode);
    }

    @Override
    public void onInteraction(GameObject other) {
        if (other.isDangerous()) {
            // Se ha tocado un enemigo o agua
            this.die();
        } else if (other instanceof Vines) {
            Vines vines = (Vines)other;
            ControllableMode mode = (ControllableMode)this.getMode();

            if (!this.inLastVines(vines) && !(mode instanceof Climbing)
             && !(mode instanceof Hanging) && !(mode instanceof Running)) {
                Hanging newMode = new Hanging(mode.getDirection(), vines.getPlatform(), this);
                if (newMode.isValid()) {
                    this.lastVines = vines.getPlatform().getAttached();
                    this.switchTo(newMode);
                }
            }
        }
    }

    @Override
    public void onFloatingContact(GameObject floating) {
        if (floating instanceof Fruit) {
            this.updateScore(+((Fruit)floating).getScore());
            floating.delete();
        } else if (floating instanceof Key) {
            this.hasKey = true;
            floating.delete();
        }
    }
    /**
     * Obtiene el puntaje del jugador
     * @return puntaje del jugador
     */
    public Integer getScore() {
        return this.score;
    }

    /**
     * Indica si el jugador se encuentra en un estado en que ha perdido la partida
     * @return true si el jugador perdio la partida, false de lo contrario
     */
    public Boolean hasLost() {
        return this.lost;
    }
    /**
     * Indica si el jugador ha entrado en contacto con la llave que le permite liberar a Donkey Kong
     * @return true si el jugador posee la llave, false de lo contrario
     */
    public Boolean hasKey() {
        return this.hasKey;
    }
    /**
     * Obtiene el juego en el que se encuentra el jugador
     * @return juego en el que se encuentra el jugador
     */
    public Game getGame() {
        return this.game;
    }
    /**
     * Inicializa la rutina de estado de pérdida de partida
     */
    public void die() {
        if (!this.lost) {
            this.lost = true;
            this.refreshMode();
        }
    }

    @Override
    protected Integer getZ() {
        return 2;
    }
    
    private Integer score;
    private Boolean lost = false;
    private Boolean hasKey = false;
    private Game game;
    private Vines[] lastVines = null;
    
    /**
     * Agrega la diferencia de puntaje dada al puntaje actual del jugador
     * @param difference diferencia entre puntaje actual y puntaje actualizado
     */
    private void updateScore(Integer difference) {
        this.score += difference;
        this.refreshMode();
    }

    /**
     * @brief Determina si una liana se encuentra dentro de la blacklist temporal.
     *
     * @param other Liana contra la cual comparar.
     * @return Indicación de si se debe evitar temporalmente esta liana.
     */
    private Boolean inLastVines(Vines other) {
        if (this.lastVines != null) {
            for (Vines vines : this.lastVines) {
                if (vines == other) {
                    return true;
                }
            }
        }

        return false;
    }
}
