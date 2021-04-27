package cr.ac.tec.ce3104.tc3.modes;

import cr.ac.tec.ce3104.tc3.physics.Speed;
import cr.ac.tec.ce3104.tc3.resources.Sprite;
import cr.ac.tec.ce3104.tc3.resources.Sequence;

public class Static implements Mode {
    /**
     * crea un modo de juego que indica que una entidad de juego permanece estática
     * @param sprite referencia a sprite que contiene la imagen estática de la entidad
     */
    public Static(Sprite sprite) {
        this.sprite = sprite;
    }

    @Override
    public Speed getSpeed() {
        return Speed.stationary();
    }

    @Override
    public Sequence getSequence() {
        return this.sprite;
    }

    private Sprite sprite;
}
