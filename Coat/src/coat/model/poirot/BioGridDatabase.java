package coat.model.poirot;

import coat.CoatView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

/**
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class BioGridDatabase {

    private static Map<String, List<StringRelationship>> relationships;

    public static List<StringRelationship> getRelationships(String name) {
        if (relationships == null) loadRelationships();
        return relationships.get(name);
    }

    public static List<StringRelationship> getRelationships(String source, String target) {
        if (relationships == null) loadRelationships();
        final List<StringRelationship> stringRelationships = relationships.get(source);
        return stringRelationships == null ? null : stringRelationships.stream().filter(stringRelationship -> stringRelationship.getTarget().equals(target)).collect(Collectors.toList());
    }

    private static void loadRelationships() {
        System.out.println("Loading BioGrid");
        relationships = new HashMap<>();
        // id,source,target,database,type,method,score
        // 270092,KLHL20,GBP2,biogrid,direct interaction,two hybrid,2.0
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(BioGridDatabase.class.getResourceAsStream("biogrid.csv.gz"))))) {
            reader.readLine();
            reader.lines().forEach(BioGridDatabase::createRelationship);
        } catch (IOException e) {
            CoatView.printMessage("error loading BioGrid", "error");
            e.printStackTrace();
        }
        System.out.println("BioGrid database successfully loaded");
    }

    private static void createRelationship(String line) {
        try {
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
