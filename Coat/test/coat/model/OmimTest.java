package coat.model;

import coat.model.poirot.Omim;
import org.junit.Assert;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class OmimTest {

    @Test
    public void test() {
        final List<String> phenotypes = Arrays.asList();
        final List<String> get = Omim.getRelatedPhenotypes("CLECL1");
        Assert.assertEquals(phenotypes, get);
    }

    @Test
    public void testTwo() {
        final List<String> phenotypes = Arrays.asList("Medullary cystic kidney disease 1, 174000 (3)");
        final List<String> get = Omim.getRelatedPhenotypes("MUC1");
        Assert.assertEquals(phenotypes, get);
    }

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
    public void testFour() {
        String disease = "schizo";
        final List<String> phenotypes = Omim.getPhenotypes(disease);
        phenotypes.forEach(phenotype -> {
            System.out.println(phenotype);
            System.out.println(Omim.getRelatedGenes(phenotype));
        });
    }
}
