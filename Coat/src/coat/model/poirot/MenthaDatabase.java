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
 * Provides access to Mentha database. Accesses must be done via <code>getRelationships(String geneName)</code>.
 * The result is a list of StringRelationship.
 *
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class MenthaDatabase {

    private static Map<String, List<StringRelationship>> relationships;

    public static List<StringRelationship> getRelationships(String name) {
        if (relationships == null) loadRelationships();
        return relationships.get(name);
    }

    private static void loadRelationships() {
        System.out.println("Loading Mentha");
        relationships = new HashMap<>();
        /*
            00 ID(s) interactor A	uniprotkb:Q86V88
            01 ID(s) interactor B	uniprotkb:P60981

            02 Alt. ID(s) interactor A	-
            03 Alt. ID(s) interactor B	-

            04 Alias(es) interactor A	uniprotkb:MDP1(gene name)
            05 Alias(es) interactor B	uniprotkb:DSTN(gene name)

            06 Interaction detection method(s)	psi-mi:"MI:0401"(biochemical)
            07 Publication 1st author(s)	-
            08 Publication Identifier(s)	pubmed:22939629

            09 Taxid interactor A	taxid:9606(Homo sapiens)
            10 Taxid interactor B 	taxid:9606(Homo sapiens)

            11 Interaction type(s) psi-mi:"MI:0403"(colocalization)
            12 Source database(s)	psi-mi:"MI:0463"(biogrid)
            13 Interaction identifier(s)    BIOGRID:749408
            14 Confidence value(s)	mentha-score:0.081
            15 Expansion method(s)	-

            16 Biological role(s) interactor A	-
            17 Biological role(s) interactor B -

            18 Experimental role(s) interactor A	-
            19 Experimental role(s) interactor B	-

            20 Type(s) interactor A	-
            21 Type(s) interactor B	-

            22 Xref(s) interactor A	-
            23 Xref(s) interactor B	-

            24 Interaction Xref(s)	-

            25 Annotation(s) interactor A	-
            26 Annotation(s) interactor B	-

            27 Interaction annotation(s)	-
            28 Host organism(s)	-
            29 Interaction parameter(s)	-
            30 Creation date	-
            31 Update date	-

            32 Checksum(s) interactor A -
            33 Checksum(s) interactor B	-

            34 Interaction Checksum(s)	-
            35 Negative	-

            36 Feature(s) interactor A	-
            37 Feature(s) interactor B	-

            38 Stoichiometry(s) interactor A	-
            39 Stoichiometry(s) interactor B	-

            40 Identification method participant A	-
            41 Identification method participant B -
         */
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(MenthaDatabase.class.getResourceAsStream("mentha.tsv.gz"))))) {
            reader.readLine();
            reader.lines().forEach(MenthaDatabase::createRelationship);
        } catch (IOException e) {
            CoatView.printMessage("error loading Mentha", "error");
            e.printStackTrace();
        }
        System.out.println("Mentha database successfully loaded");
    }

    private static void createRelationship(String line) {
        try {
            final String row[] = line.split("\t");
            final String source = extractGeneName(row[4]);
            final String target = extractGeneName(row[5]);
            final String method = extractMethod(row[6]);
            final String type = extractType(row[11]);
            final String database = extractDatabase(row[12]);
            final String id = extractId(row[13]);
            final String score = extractScore(row[14]);
            final StringRelationship relationship = new StringRelationship(source, target);
            relationship.getProperties().put("id", id);
            relationship.getProperties().put("method", method);
            relationship.getProperties().put("type", type);
            relationship.getProperties().put("database", database);
            relationship.getProperties().put("score", score);
            addToIndex(source, relationship);
            addToIndex(target, relationship);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static String extractScore(String text) {
        // mentha-score:0.081
        if (text.startsWith("mentha-score:")) return text.substring(13);
        if (!text.equals("-")) System.err.println(text);
        return null;
    }

    private static String extractDatabase(String text) {
        // psi-mi:"MI:0463"(biogrid)
        return extractMethod(text);
    }

    private static String extractType(String text) {
        // psi-mi:"MI:0403"(colocalization)
        return extractMethod(text);
    }

    private static String extractMethod(String text) {
        // psi-mi:"MI:0401"(biochemical)
        if (text.startsWith("psi-mi")) {
            final int startPos = text.indexOf("(");
            final int endPos = text.indexOf(")");
            if (startPos > 0 && endPos > 0) return text.substring(startPos + 1, endPos);
        }
        System.err.println(text);
        return null;
    }

    private static String extractGeneName(String text) {
        // uniprotkb:MDP1(gene name)
        if (text.startsWith("uniprotkb:")) {
            int index = text.indexOf("(");
            if (index > 0) {
                String id = text.substring(10, index);
                if (id.contains(" ")) id = id.split(" ")[0].replace("\"", "");
                return id;
            }
        }
        System.err.println(text);
        return null;
    }

    private static String extractId(String text) {
        // BIOGRID:749408
        int index = text.indexOf(":");
        if (index > 0) return text.substring(index + 1);
        System.err.println(text);
        return null;
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
