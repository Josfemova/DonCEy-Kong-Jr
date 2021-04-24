package cr.ac.tec.ce3104.tc3.resources;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.ArrayList;

public class Assets {
    public static File[] listSpritePaths() throws IOException {
        ArrayList<File> paths = new ArrayList<>();
        for (File category : new File("assets/sprites").listFiles()) {
            paths.addAll(Arrays.asList(category.listFiles()));
        }

        return paths.toArray(new File[0]);
    }
}
