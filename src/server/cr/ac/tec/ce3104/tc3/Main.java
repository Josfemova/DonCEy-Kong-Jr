package cr.ac.tec.ce3104.tc3;

// Punto de entrada de la aplicación
public class Main {
    /**
     * @brief Punto de entrada de la aplicación.
     *
     * @param argv Parámetros de línea de comandos
     */
    public static void main(String[] argv) {
        Server.getInstance().startUp();
    }
}
