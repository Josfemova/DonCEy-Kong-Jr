package cr.ac.tec.ce3104.tc3.gameobjects;

import cr.ac.tec.ce3104.tc3.modes.Mode;
import cr.ac.tec.ce3104.tc3.physics.Dynamics;
import cr.ac.tec.ce3104.tc3.physics.Position;

// Un cocodrilo
public abstract class Crocodile extends GameObject {
    // Siempre son interactivos
    @Override
    public Dynamics getDynamics() {
        return Dynamics.INTERACTIVE;
    }

    // Siempre matan al jugador
    @Override
    public Boolean isDangerous() {
        return true;
    }

    /**
     * Obtiene cada cuantos ticks se da un movimiento de la entidad
     * @param difficulty indica el nivel de dificultad, entre más grande sea, más afecta la velocidad del cocodrilo
     * @return cantidad de ticks para un movimiento por parte del jugador
     */
    protected static Integer getSpeedDenominator(Integer difficulty) {
        return Math.max(1, 3 - difficulty);
    }
    /**
     * Constructor que crea un cocodrilo a partir de un modo de operacion de cocodrilo existente
     * @param mode modo en el que debe crearse el cocodrilo
     * @param position posicion en la que se colocará el cocodrilo inicialmente
     */
    protected Crocodile(Mode mode, Position position) {
        super(mode, position);
    }

    @Override
    protected Integer getZ() {
        return 2;
    }
}
