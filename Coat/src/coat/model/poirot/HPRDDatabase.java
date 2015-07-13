package coat.model.poirot;

import coat.CoatView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 * Provides access to HPRD relationships database. Accesses must be done via <code>getRelationships(String geneName)</code>.
 * The result is a list of StringRelationship.
 *
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class HPRDDatabase {

    private static Map<String, List<StringRelationship>> relationships;

    /**
     * Get a list of relationships where the argument gene is involved.
     *
     * @param name name of the gene
     * @return list of relationships of the gene
     */
    public static List<StringRelationship> getRelationships(String name) {
        if (relationships == null) loadRelationships();
        return relationships.get(name);
    }

    private static void addToIndex(String name, StringRelationship relationship) {
        List<StringRelationship> stringRelationships = relationships.get(name);
        if (stringRelationships == null) {
            stringRelationships = new ArrayList<>();
            relationships.put(name, stringRelationships);
        }
        stringRelationships.add(relationship);
    }

    private static void loadRelationships() {
        System.out.println("Loading HPRD");
        relationships = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(HPRDDatabase.class.getResourceAsStream("hprd-gene-interactions.tsv.gz"))))) {
            reader.readLine();
            reader.lines().forEach(HPRDDatabase::createRelationship);
        } catch (IOException e) {
            CoatView.printMessage("error loading HPRD", "error");
            e.printStackTrace();
        }
        System.out.println("HPRD database successfully loaded");
    }

    private static void createRelationship(String line) {
        try {
            /*
                0 interactor_1_geneSymbol   ERRFI1
                1 interactor_1_hprd_id	09218
                2 interactor_1_refseq_id	NP_061821.1
                3 interactor_2_geneSymbol	ERBB2
                4 interactor_2_hprd_id	01281
                5 interactor_2_refseq_id	NP_004439.2
                6 experiment_type	in vitro;yeast 2-hybrid
                7 reference_id	11003669
             */
            final String row[] = line.split(",");
            final String id = row[7];
            final String source = row[0];
            final String target = row[3];
            final String database = "hprd";
            final String type = row[6];
//            final String method = row[6];
            final StringRelationship relationship = new StringRelationship(source, target);
            relationship.getProperties().put("id", id);
            relationship.getProperties().put("type", type);
            relationship.getProperties().put("database", database);
//            relationship.getProperties().put("method", method);
            addToIndex(source, relationship);
            addToIndex(target, relationship);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


}
