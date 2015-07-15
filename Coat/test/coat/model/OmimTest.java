package coat.model;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class OmimTest {


    @Test
    public void testThree() {
        File omim = new File("omim/genemap");
        try (BufferedReader reader = new BufferedReader(new FileReader(omim))) {
            reader.lines().forEach((line) -> {
                String[] row = line.split("\\|");
                System.out.println("["+row[5] + "]\n" + row[7] + "\n" + row[11]);
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
