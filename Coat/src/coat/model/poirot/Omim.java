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
    private final static int DESCRIPTION = 7;
    private final static int DISEASE = 11;

    public static List<String> getRelatedPhenotypes(String gene) {
        final List<String> related = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(genemap))) {
            reader.lines().forEach(line -> {
                final String[] row = line.split("\\|");
                if (row[GENE].contains(gene)) if (!row[DESCRIPTION].equals(".")) related.add(row[DESCRIPTION]);
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return related;
    }

    public static List<String> getRelatedGenes(String phenotype) {
        final String lowerCased = phenotype.toLowerCase();
        List<String> related = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(genemap))) {
            reader.lines().forEach(line -> {
                final String[] row = line.split("\\|");
                if (row[DISEASE].contains(phenotype))
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
                final String[] row = line.split("\\|");
                if (row[GENE].toLowerCase().contains(gene.toLowerCase())) return row[DESCRIPTION];
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<String> getPhenotypes(String name) {
        final List<String> list = new ArrayList<>();
        final String lowerCased = name.toLowerCase();
        try (BufferedReader reader = new BufferedReader(new FileReader(genemap))) {
            reader.lines().forEach(line -> {
                final String[] row = line.split("\\|");
                if (!row[DISEASE].equals(".")) {
                    final String[] phenotypes = row[DISEASE].split("; ");
                    for (String phenotype : phenotypes)
                        if (phenotype.toLowerCase().contains(lowerCased)) {
                            if (!list.contains(phenotype)) list.add(phenotype);
                        }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }
}
