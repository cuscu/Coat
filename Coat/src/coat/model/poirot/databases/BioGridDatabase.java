package coat.model.poirot.databases;

import coat.CoatView;
import coat.model.poirot.StringRelationship;
import javafx.application.Platform;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 * Provides access to BioGrid relationships database. Accesses must be done via <code>getRelationships(String geneName)</code>.
 * The result is a list of StringRelationship.
 * <p>
 * source = KLHL20, target = GBP2,
 * properties = {id=270092, database = biogrid, type = direct interaction, method = two hybrid, score = 2.0}
 *
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class BioGridDatabase {

    private static Map<String, List<StringRelationship>> relationships = new HashMap<>();

    static {
        loadRelationships();
    }

    /**
     * Get a list of relationships where the argument gene is involved.
     *
     * @param name name of the gene
     * @return list of relationships of the gene
     */
    public static List<StringRelationship> getRelationships(String name) {
        return relationships.get(name);
    }

    private static void loadRelationships() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(BioGridDatabase.class.getResourceAsStream("biogrid.csv.gz"))))) {
            reader.readLine(); // Skip header
            reader.lines().forEach(BioGridDatabase::createRelationship);
            Platform.runLater(() -> CoatView.printMessage("BioGrid database successfully loaded", "info"));
        } catch (IOException e) {
            Platform.runLater(() -> CoatView.printMessage("error loading BioGrid", "error"));
            e.printStackTrace();
        }
    }

    private static void createRelationship(String line) {
        try {
            /*
                0 id  270092
                1 source  KLHL20
                2 target  GBP2
                3 database    biogrid
                4 type    direct interaction
                5 method  two hybrid
                6 score   2.0
             */
            final String row[] = line.split(",");
            final String id = row[0];
            final String source = row[1];
            final String target = row[2];
            final String database = row[3];
            final String type = row[4];
            final String method = row[5];
            final String score = row[6];
            final StringRelationship relationship = new StringRelationship(source, target);
            relationship.getProperties().put("id", id);
            relationship.getProperties().put("type", type);
            relationship.getProperties().put("database", database);
            relationship.getProperties().put("method", method);
            relationship.getProperties().put("score", score);
            addToIndex(source, relationship);
            addToIndex(target, relationship);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static void addToIndex(String name, StringRelationship relationship) {
        List<StringRelationship> stringRelationships = relationships.get(name);
        if (stringRelationships == null) {
            stringRelationships = new ArrayList<>();
            relationships.put(name, stringRelationships);
        }
        stringRelationships.add(relationship);
    }


}
