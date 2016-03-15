/******************************************************************************
 * Copyright (C) 2015 UICHUIMI                                                *
 * *
 * This program is free software: you can redistribute it and/or modify it    *
 * under the terms of the GNU General Public License as published by the      *
 * Free Software Foundation, either version 3 of the License, or (at your     *
 * option) any later version.                                                 *
 * *
 * This program is distributed in the hope that it will be useful, but        *
 * WITHOUT ANY WARRANTY; without even the implied warranty of                 *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                       *
 * See the GNU General Public License for more details.                       *
 * *
 * You should have received a copy of the GNU General Public License          *
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.      *
 ******************************************************************************/
package coat.core.poirot.dataset;

import coat.core.poirot.dataset.graph.PoirotGraphLabels;
import coat.json.JSONArray;
import coat.json.JSONObject;
import org.jetbrains.annotations.Nullable;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import sun.net.www.content.text.PlainTextInputStream;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author Pascual Lorente Arencibia (pasculorente@gmail.com)
 */
public class OmimRetriever {

    private final static String BASE_URL = "http://api.europe.omim.org";
    private final static String API_KEY = "85BA467CB41C620BA51F185E7D81150AF619BE7A";

    private final static String MIM_LIST = "http://www.omim.org/static/omim/data/mim2gene.txt";

    private final AtomicInteger counter = new AtomicInteger();

    private Connection connection;
    private GraphDatabaseService graphDatabase;

    public void updateData(Connection connection, GraphDatabaseService graphDatabase) {
        this.connection = connection;
        this.graphDatabase = graphDatabase;
        updateOmimCache();
        values.forEach(System.out::println);
    }

    private void updateOmimCache() {
//        createOmimStructure();
        final List<String[]> mimNumbers = readMimNumbers();
        for (int i = 0; i < 200; i++) updateMimEntry(mimNumbers.get(i));
//        mimNumbers.forEach(this::updateMimEntry);
    }

    private void createOmimStructure() {
        try {
            final Statement statement = connection.createStatement();
            statement.addBatch("" +
                    "CREATE TABLE IF NOT EXISTS Omim (" +
                    "id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
                    "name TEXT UNIQUE," +
                    "type TEXT," +
                    "value TEXT" +
                    ")");
            statement.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private List<String[]> readMimNumbers() {
        try {
            final URL url = new URL(MIM_LIST);
            url.openConnection();
            BufferedReader reader = new BufferedReader(new InputStreamReader((PlainTextInputStream) url.getContent()));
            return reader.lines().filter(line -> !line.startsWith("#")).map(line -> line.split("\t")).collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    private void updateMimEntry(String[] mimEntry) {
        if (!isInLocalDatabase(mimEntry)) {
            final String result = getDataFromServer(mimEntry);
//            System.out.println(result);
            addEntry(result);
//            addDataToDatabase(mimEntry, result);
        }

    }

    final Set<String> values = new HashSet<>();

    private void addEntry(String result) {
        final JSONObject root = new JSONObject(result);
        final JSONObject entry = root.getJSONObject("omim").getJSONArray("entryList").getJSONObject(0).getJSONObject("entry");
        final String prefix = entry.getString("prefix");
        String tree = "";
        if (entry.containsKey("geneMap")) {
            tree += " geneMap, ";
            if (entry.getJSONObject("geneMap").containsKey("phenotypeMapList")) tree += "phenotypeMapList";
        }
        if (entry.containsKey("phenotypeMapList")) tree += "phenotypeMapList";

        values.add(String.format("%s: %s", prefix, tree));
        //        if (entry.containsKey("geneMap")) addGeneEntry(entry);
//        else if (entry.containsKey("phenotypeMapList")) addPhenotyeEntry(entry);
    }

    private void addPhenotyeEntry(JSONObject entry) {

    }

    private void addGeneEntry(JSONObject entry) {

    }

    private boolean isInLocalDatabase(String[] mimEntry) {
        try {
            final Statement statement = connection.createStatement();
            switch (mimEntry[1]) {
                case "gene":
                case "gene/phenotype":
                    return geneIsInLocalDatabase(mimEntry);
                case "phenotype":
                case "predominatly phenotypes":
                    return phenotypeIsInLocalDatabase(mimEntry);
                case "moved/removed":
                default:
                    return true;

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean phenotypeIsInLocalDatabase(String[] mimEntry) {
        final String id = "omim:" + mimEntry[0];
        try (Transaction transaction = graphDatabase.beginTx()) {
            return graphDatabase.findNode(PoirotGraphLabels.PHENOTYPE, "id", id) != null;
        }
    }

    private boolean geneIsInLocalDatabase(String[] mimEntry) {
        try (Transaction transaction = graphDatabase.beginTx()) {
            return mimEntry.length > 2 && graphDatabase.findNode(PoirotGraphLabels.GENE, "name", mimEntry[3]) != null;
        }
    }

    @Nullable
    private String getDataFromServer(String[] mimEntry) {
        if (counter.incrementAndGet() % 100 == 0) System.out.println(counter.get() + "\t" + mimEntry[0]);
        String result = null;
        try {
            final String textUrl = BASE_URL + "/api/entry?include=dates,geneMap&format=json&mimNumber=" + mimEntry[0] + "&apiKey=" + API_KEY;
            final URL url = new URL(textUrl);
            url.openConnection();
            BufferedReader reader = new BufferedReader(new InputStreamReader((InputStream) url.getContent()));
            StringBuilder builder = new StringBuilder();
            reader.lines().forEach(builder::append);
            result = builder.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    private void addDataToDatabase(String[] mimEntry, String result) {
        final String mimNumber = mimEntry[0];
        final String type = mimEntry[1];
        if (result != null && !type.equals("moved/removed"))
            try {
                switch (type) {
                    case "gene":
                    case "gene/phenotype":
                        addGene(mimEntry, result);
                        break;
                    case "moved/removed":
                    case "predominatly phenotypes":
                    case "phenotype":
                        addPhenotype(mimEntry, result);
                        break;
                    default:
                        break;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
    }

    private void addGene(String[] mimEntry, String result) throws SQLException {
        System.out.println("GENE:" + result);
        final String name = mimEntry[3];
        final JSONObject root = new JSONObject(result);
        final JSONObject entry = root.getJSONObject("omim").getJSONArray("entryList").getJSONObject(0).getJSONObject("entry");
        final String title = entry.getJSONObject("titles").getString("preferredTitle");
        final String dateUpdated = entry.getString("dateUpdated");
        final JSONObject geneMap = entry.getJSONObject("geneMap");
        final String confidence = geneMap.getString("confidence");
        final String mappingMethod = geneMap.getString("mappingMethod");
        final long geneId = addNewGene(name, title, dateUpdated, confidence, mappingMethod);
        if (geneMap.containsKey("phenotypeMapList")) addRelatePhenotypes(mimEntry, geneMap, geneId);

    }

    private void addGeneToSqLite(String s, String result) throws SQLException {
        final JSONObject root = new JSONObject(result);
        final JSONObject geneMap = root.getJSONObject("omim").getJSONArray("entryList").getJSONObject(0).getJSONObject("entry");
        final String confidence = geneMap.getString("confidence");
        final String command = String.format("INSERT OR IGNORE INTO Genes (name) VALUES (\"%s\")", s);
        System.out.println(command);
        final Statement statement = connection.createStatement();
        statement.execute(command);
        if (geneMap.containsKey("phenotypeMapList")) {
            if (statement.execute(String.format("SELECT id FROM Genes WHERE name = \"%s\"", s))) {
                statement.getResultSet().first();
                final int gene_id = statement.getResultSet().getInt("id");
                // INSERT OR IGNORE PHENOTYPES
                final JSONArray phenotypeMapList = geneMap.getJSONArray("phenotypeMapList");
                for (int i = 0; i < phenotypeMapList.length(); i++) {
                    final JSONObject phenotypeMap = phenotypeMapList.getJSONObject(i);
                    final String mimNumber = phenotypeMap.getString("phenotypeMimNumber");
                    final String name = phenotypeMap.getString("phenotype");
                    final String mappingKey = phenotypeMap.getString("phenotypeMappingKey");
                    statement.execute(String.format("INSERT OR UPDATE Phenotypes (database,database_id,name)" +
                            "VALUES(\"%s\",\"%s\",\"%s\")", "omim", mimNumber, name));
                }

            }
        }
    }

    private void addRelatePhenotypes(String[] mimEntry, JSONObject geneMap, long geneId) {
        final JSONArray phenotypeMapList = geneMap.getJSONArray("phenotypeMapList");
        for (int i = 0; i < phenotypeMapList.length(); i++)
            addRelatedPhenotype(mimEntry, geneId, phenotypeMapList.getJSONObject(i).getJSONObject("phenotypeMap"));
    }

    private void addRelatedPhenotype(String[] mimEntry, long geneId, JSONObject phenotypeMap) {
        int phenotypeMimNumber = phenotypeMap.containsKey("phenotypeMimNumber")
                ? phenotypeMap.getInt("phenotypeMimNumber")
                : Integer.valueOf(mimEntry[0]);
        final String phenotypeName = phenotypeMap.getString("phenotype");
        final int phenotypeMappingKey = phenotypeMap.getInt("phenotypeMappingKey");
        try (Transaction transaction = graphDatabase.beginTx()) {
            final Node geneNode = graphDatabase.getNodeById(geneId);
            final String omimId = "omim:" + phenotypeMimNumber;
            Node phenotypeNode = graphDatabase.findNode(PoirotGraphLabels.PHENOTYPE, "id", omimId);
            if (phenotypeNode == null) {
                phenotypeNode = graphDatabase.createNode(PoirotGraphLabels.PHENOTYPE);
                phenotypeNode.setProperty("name", phenotypeName);
                phenotypeNode.setProperty("id", omimId);
                phenotypeNode.setProperty("mappingKey", phenotypeMappingKey);
            } else System.out.println("Node already in db: " + omimId);
            transaction.success();
        }
    }

    private long addNewGene(String name, String title, String dateUpdated, String confidence, String mappingMethod) {
        try (Transaction transaction = graphDatabase.beginTx()) {
            final Node node = graphDatabase.createNode(PoirotGraphLabels.GENE);
            node.setProperty("name", name);
            node.setProperty("title", title);
            node.setProperty("dateUpdated", dateUpdated);
            node.setProperty("confidence", confidence);
            node.setProperty("mappingMethod", mappingMethod);
            long geneId = node.getId();
            transaction.success();
            return geneId;
        }
    }

    private void addPhenotype(String[] mimEntry, String result) {
        System.out.println("PHENOTYPE:" + result);
        final String id = "omim:" + mimEntry[0];
        try (Transaction transaction = graphDatabase.beginTx()) {
            Node node = graphDatabase.findNode(PoirotGraphLabels.PHENOTYPE, "id", id);
            if (node == null) node = graphDatabase.createNode(PoirotGraphLabels.PHENOTYPE);
            if (!node.hasProperty("id")) node.setProperty("id", id);
            if (!node.hasProperty("id")) node.setProperty("id", id);
            if (!node.hasProperty("id")) node.setProperty("id", id);
            if (!node.hasProperty("id")) node.setProperty("id", id);
        }
//        final String database = "omim";
//        final String database_id = mimEntry[0];
//        final JSONObject json = new JSONObject(result);
//        final JSONObject entry = json.getJSONObject("omim").getJSONArray("entryList").getJSONObject(0).getJSONObject("entry");
//        final String name = entry.getJSONObject("titles").getString("preferredTitle");
//        final String status = entry.getString("status");
//        final String date_updated = entry.getString("dateUpdated");
//        try {
//            final Statement statement = connection.createStatement();
//            final String command = String.format("" +
//                    "INSERT OR REPLACE INTO Phenotypes(database,database_id,name,status,date_updated) " +
//                    "VALUES(\"%s\",\"%s\",\"%s\",\"%s\",\"%s\")", database, database_id, name, status, date_updated);
//            System.out.println(command);
//            statement.execute(command);
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }


    }
}
