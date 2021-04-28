package cr.ac.tec.ce3104.tc3.resources;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.ArrayList;

// Control de directorio "assets/"
public class Assets {
    /**
     * Obtiene todas las rutas de archivo de imágen de sprites en el folder de assets del juego
     * @return Rutas de archivos de imagen que contienen sprites
     * @throws IOException error que surge dado un problema encontrado al intentar abrir uno de los archivos
     */
    public static File[] listSpritePaths() throws IOException {
        ArrayList<File> paths = new ArrayList<>();
        for (File category : new File("assets/sprites").listFiles()) {
            paths.addAll(Arrays.asList(category.listFiles()));
        }

        return paths.toArray(new File[0]);
    }
}
