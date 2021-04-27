package cr.ac.tec.ce3104.tc3.physics;

public class Bounds {
    /**
     * Crea una caja de colisión en base a un tamaño
     * @param origin posición de origen de la caja de colision
     * @param size dimensiones del objeto cuya caja de colisiones se quiere crear
     */
    public Bounds(Position origin, Size size) {
        this.origin = origin;
        this.size = size;
    }
    /**
     * Obtiene la coordenada de origen de la caja de colisión
     * @return coordenada de origen de la caja de colisión
     */
    public Position getOrigin() {
        return this.origin;
    }
    /**
     * Obtiene las dimensiones de la caja de colision
     * @return dimensiones de la caja de colision
     */
    public Size getSize() {
        return this.size;
    }

    /**
     * Obtiene la coordenada vertical en la que se encuentra la base de la caja de colision
     * @return coordenada vertical de la base de la caja de colisión
     */
    public Integer getBaseline() {
        return this.origin.getY() + this.size.getHeight();
    }
    /**
     * Obtiene la coordenada horizontal central de la caja de colisión
     * @return coordenada horizontal central de la caja de colisión
     */
    public Integer getHorizontalCenter() {
        return this.origin.getX() + this.size.getWidth() / 2;
    }

    /**
     * Dada otra caja de colision, verifica si respecto a la misma, la caja de colision se encuentra su izquierda
     * @param reference caja de colisión respecto a la cual se quiere saber si la caja de colisión actual se encuentra a su izquierda
     * @return true si la caja de colision actual se encuentra a la izquierda del objeto dado, false de lo contrario
     */
    public Boolean leftOf(Bounds reference) {
        return this.origin.getX() + this.size.getWidth() <= reference.origin.getX();
    }
    /**
     * Dada otra caja de colision, verifica si respecto a la misma, la caja de colision se encuentra su derecha
     * @param reference caja de colisión respecto a la cual se quiere saber si la caja de colisión actual se encuentra a su derecha
     * @return true si la caja de colision actual se encuentra a la derecha del objeto dado, false de lo contrario
     */
    public Boolean rightOf(Bounds reference) {
        return this.origin.getX() >= reference.origin.getX() + reference.size.getWidth();
    }
    /**
     * Dada otra caja de colision, verifica si respecto a la misma, la caja de colision se encuentra su encima
     * @param reference caja de colisión respecto a la cual se quiere saber si la caja de colisión actual se encuentra encima
     * @return true si la caja de colision actual se encuentra encima del objeto dado, false de lo contrario
     */
    public Boolean aboveOf(Bounds reference) {
        return this.origin.getY() + this.size.getHeight() <= reference.origin.getY();
    }   
    /**
     * Dada otra caja de colision, verifica si respecto a la misma, la caja de colision se encuentra debajo
     * @param reference caja de colisión respecto a la cual se quiere saber si la caja de colisión actual se encuentra debajo
     * @return true si la caja de colision actual se encuentra debajo del objeto dado, false de lo contrario
     */
    public Boolean belowOf(Bounds reference) {
        return this.origin.getY() >= reference.origin.getY() + reference.size.getHeight();
    }
    /**
     * Dado otra caja de colsión, indica si existe un traslape con la caja de colisión actual
     * @param other caja de colision de referencia 
     * @return true si existe una colisión entre cajas, false de lo contrario
     */
    public Boolean collidesWith(Bounds other) {
        return !this.leftOf(other) && !this.rightOf(other) && !this.aboveOf(other) && !this.belowOf(other);
    }

    private final Position origin;
    private final Size size;
}
