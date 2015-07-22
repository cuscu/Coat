package coat.model;

import coat.model.poirot.databases.HPRDDatabase;
import org.junit.Test;

import java.io.*;
import java.util.zip.GZIPInputStream;

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

    @Test
    public void test() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(HPRDDatabase.class.getResourceAsStream("hprd-gene-interactions.tsv.gz"))))) {
            reader.readLine();
            reader.lines().forEach(System.out::println);
            System.out.println("HGNC database successfully loaded");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
