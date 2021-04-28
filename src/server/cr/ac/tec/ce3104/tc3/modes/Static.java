package cr.ac.tec.ce3104.tc3.modes;

import cr.ac.tec.ce3104.tc3.physics.Speed;
import cr.ac.tec.ce3104.tc3.resources.Sprite;
import cr.ac.tec.ce3104.tc3.resources.Sequence;

// Modo usado para objetos estáticos (eg, plataformas y frutas) y para congelar entidades
public class Static implements Mode {
    /**
     * crea un modo de juego que indica que una entidad de juego permanece estática
     * @param sprite referencia a sprite que contiene la imagen estática de la entidad
     */
    public Static(Sprite sprite) {
        this.sprite = sprite;
    }

    // Velocidad estática
    @Override
    public Speed getSpeed() {
        return Speed.stationary();
    }

    // Secuencia estática
    @Override
    public Sequence getSequence() {
        return this.sprite;
    }

    // Sprite estático
    private Sprite sprite;
}
