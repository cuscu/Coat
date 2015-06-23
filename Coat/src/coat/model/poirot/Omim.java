package coat.model.poirot;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class Omim {

    private final static File genemap = new File("omim/genemap");
    private final static int GENE = 5;
    private final static int PHENOTYPE = 13;
    private final static int DISEASE = 15;
    private final static int DESCRIPTION = 7;

    public static List<String> getRelatedPhenotyes(String gene) {
        try (BufferedReader reader = new BufferedReader(new FileReader(genemap))) {
            final List<String> related = new ArrayList<>();
            reader.lines().forEach(line -> {
                final String[] row = line.split("\t");
                if (row[GENE].contains(gene)) if (!row[PHENOTYPE].equals(".")) related.add(row[PHENOTYPE]);
            });
            return related;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<String> getRelatedGenes(String phenotype) {
        final String lowerCased = phenotype.toLowerCase();
        List<String> related = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(genemap))) {
            reader.lines().forEach(line -> {
                final String[] row = line.split("\t");
                if (row[PHENOTYPE].toLowerCase().contains(lowerCased))
                    if (!row[GENE].equals(".")) related.addAll(Arrays.asList(row[GENE].split(", ")));
                if (row[DISEASE].toLowerCase().contains(lowerCased))
                    if (!row[GENE].equals(".")) related.addAll(Arrays.asList(row[GENE].split(", ")));
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return related;
    }

    public static String getGeneDescription(String gene) {
        try (BufferedReader reader = new BufferedReader(new FileReader(genemap))) {
            String line;
            while ((line = reader.readLine()) != null) {
                final String[] row = line.split("\t");
                if (row[GENE].toLowerCase().contains(gene.toLowerCase())) return row[DESCRIPTION];
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<Phenotype> getPhenotypes(String name) {
        List<Phenotype> list = new ArrayList<>();

        return list;
    }
}
