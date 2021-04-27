package cr.ac.tec.ce3104.tc3.resources;

import cr.ac.tec.ce3104.tc3.physics.Size;

public abstract class Sequence {
    /**
     * Obtiene la lista de sprites que componen la secuencia
     * @return arreglo que contiene los sprites que componen la secuencia
     */
    public abstract Sprite[] getSprites();

    /**
     * Obtiene la dimensión de los sprites de la secuencia
     * @return dimensión de los sprites de la secuencia
     */
    public Size getSize() {
        return this.freeze().getSize();
    }
    /**
     * Obtiene cual es el sprite que indica un estado estático de la secuencia
     * @return
     */
    public Sprite freeze() {
        return this.getSprites()[0];
    }
}
