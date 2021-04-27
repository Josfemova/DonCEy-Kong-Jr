package cr.ac.tec.ce3104.tc3.gameobjects;

import cr.ac.tec.ce3104.tc3.Game;
import cr.ac.tec.ce3104.tc3.modes.Falling;
import cr.ac.tec.ce3104.tc3.modes.Standing;
import cr.ac.tec.ce3104.tc3.resources.Sprite;
import cr.ac.tec.ce3104.tc3.physics.Dynamics;
import cr.ac.tec.ce3104.tc3.physics.Position;
import cr.ac.tec.ce3104.tc3.physics.HorizontalDirection;

public class PlayerAvatar extends GameObject{
    /**
     * Constructor para generar la entidad que representa al jugador de la partida
     * @param position posicion inicial del jugador
     * @param initialScore puntaje inicial asociado al jugador
     * @param game juego en el que se encuentra el jugador
     */
    public PlayerAvatar(Position position, Integer initialScore, Game game) {
        super(new Falling(new Standing(HorizontalDirection.RIGHT), position), position);
        this.score = initialScore;
        this.game = game;
    }

    @Override
    public Dynamics getDynamics() {
        return this.lost ? Dynamics.FLOATING : Dynamics.INTERACTIVE;
    }

    @Override
    public void onInteraction(GameObject other) {
        if (other.isDangerous()) {
            // Se ha tocado un enemigo o agua
            this.die();
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
    
    
    /**
     * Agrega la diferencia de puntaje dada al puntaje actual del jugador
     * @param difference diferencia entre puntaje actual y puntaje actualizado
     */
    private void updateScore(Integer difference) {
        this.score += difference;
        this.refreshMode();
    }

    private Integer score;
    private Boolean lost = false;
    private Boolean hasKey = false;
    private Game game;
}
