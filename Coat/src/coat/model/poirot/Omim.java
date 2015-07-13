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

    private static List<StringRelationship> relationships;

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

    private static void loadRelationships() {
        /*
            0  - Numbering system, in the format  Chromosome.Map_Entry_Number
            1  - Month entered
            2  - Day     "
            3  - Year    "
            4  - Cytogenetic location
            5  - Gene Symbol(s)
            6  - Gene Status (see below for codes)
            7  - Title
            8 - MIM Number
            9 - Method (see below for codes)
            10 - Comments
            11 - Disorders (each disorder is followed by its MIM number, if different from that of the locus,
                 and phenotype mapping method (see below).  Allelic disorders are separated by a semi-colon.
            12 - Mouse correlate
            13 - Reference
            1.42|11|4|93|1p36.31|RPL22, EAP|C|Ribosomal protein L22|180474|Ch, REc|fused with AML1 in t(3;21)|.|.|
         */
        relationships = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(genemap))) {
            reader.lines().forEach(Omim::loadRelationship);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void loadRelationship(String line) {
        final String [] row = line.split("\\|");
        final String[] genes = row[5].split(", ");


    }
}
