/*
 * Copyright (c) UICHUIMI 2016
 *
 * This file is part of Coat.
 *
 * Coat is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * Coat is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Foobar.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package coat.core.poirot.dataset;

import coat.core.poirot.dataset.graph.PoirotGraphLabels;
import coat.core.poirot.dataset.graph.PoirotGraphRelationships;
import coat.json.JSONArray;
import coat.json.JSONObject;
import org.jetbrains.annotations.Nullable;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import sun.net.www.content.text.PlainTextInputStream;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Created by uichuimi on 29/02/16.
 */
public class OmimNeo {

    private final static String BASE_URL = "http://api.europe.omim.org";
    private final static String API_KEY = "85BA467CB41C620BA51F185E7D81150AF619BE7A";

    private final static String MIM_LIST = "http://www.omim.org/static/omim/data/mim2gene.txt";
    final Set<String> values = new HashSet<>();
    private final AtomicInteger counter = new AtomicInteger();
    private final GraphDatabaseService graphDatabase;

    public OmimNeo(GraphDatabaseService graphDatabase) {
        this.graphDatabase = graphDatabase;
    }

    public void start() {
        final List<OmimEntry> mimNumbers = readMimNumbers();
        for (int i = 0; i < 200; i++) updateMimEntry(mimNumbers.get(i));
        final List<String> toSort = new ArrayList<>(values);
    }

    private List<OmimEntry> readMimNumbers() {
        try {
            final URL url = new URL(MIM_LIST);
            url.openConnection();
            BufferedReader reader = new BufferedReader(new InputStreamReader((PlainTextInputStream) url.getContent()));
            return reader.lines().filter(line -> !line.startsWith("#")).map(OmimEntry::new).collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    private void updateMimEntry(OmimEntry mimEntry) {
        if (!isInLocalDatabase(mimEntry)) {
            final String result = getDataFromServer(mimEntry);
//            System.out.println(result);
            addEntry(mimEntry, result);
//            addDataToDatabase(mimEntry, result);
        }

    }

    private boolean isInLocalDatabase(OmimEntry mimEntry) {
        try (Transaction transaction = graphDatabase.beginTx()) {
            final String id = "omim:" + mimEntry.getMimNumber();
            switch (mimEntry.getType()) {
                case "gene":
                case "gene/phenotype":
                    final Node geneNode = graphDatabase.findNode(PoirotGraphLabels.GENE, "id", id);
                    return geneNode != null;
                case "phenotype":
                case "predominatly phenotypes":
                    final Node diseaseNode = graphDatabase.findNode(PoirotGraphLabels.DISEASE, "id", id);
                    return diseaseNode != null;
                case "moved/removed":
                    return true;
                default:
                    return false;
            }
        }
    }

    @Nullable
    private String getDataFromServer(OmimEntry mimEntry) {
        if (counter.incrementAndGet() % 100 == 0) System.out.println(counter.get() + "\t" + mimEntry.getMimNumber());
        String result = null;
        try {
            final String textUrl = BASE_URL + "/api/entry?include=dates,geneMap&format=json&mimNumber=" + mimEntry.getMimNumber() + "&apiKey=" + API_KEY;
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

    private void addEntry(OmimEntry mimEntry, String result) {
        /*
            # [phenotype]:
            # [phenotype]: phenotypeMapList
            % [phenotype]:
            % [phenotype]:  geneMap.phenotypeMapList
            * [gene]:
            * [gene]:  geneMap
            * [gene]:  geneMap.phenotypeMapList
            + [gene/phenotype]:  geneMap.phenotypeMapList
            NONE [predominantly phenotypes]:
            NONE [predominantly phenotypes]:  geneMap
            NONE [predominantly phenotypes]: phenotypeMapList
         */
        final JSONObject root = new JSONObject(result);
        final JSONObject entry = root.getJSONObject("omim").getJSONArray("entryList").getJSONObject(0).getJSONObject("entry");
        final String prefijo = entry.optString("prefix");
        if (prefijo.equals("*") || prefijo.equals("+")) {
            // Gene
            addGene(mimEntry, result);
        } else if (prefijo.equals("#") || prefijo.equals("")) {
            addPhenotype(mimEntry, result);
        }
    }

    private void addGene(OmimEntry mimEntry, String result) {
        System.out.println("GENE:" + result);
        String symbol = mimEntry.getApprovedGeneSymbol();
        final JSONObject root = new JSONObject(result);
        final JSONObject entry = root.getJSONObject("omim").getJSONArray("entryList").getJSONObject(0).getJSONObject("entry");
        final String title = entry.getJSONObject("titles").getString("preferredTitle");
        final String dateUpdated = entry.optString("dateUpdated");
        final JSONObject geneMap = entry.optJSONObject("geneMap");
        String confidence = "";
        String mappingMethod = "";
        if (geneMap != null) {
            if (symbol == null) symbol = geneMap.optString("geneSymbols");
            confidence = geneMap.getString("confidence");
            mappingMethod = geneMap.optString("mappingMethod");
        }
        final long geneId = addNewGene(symbol, title, dateUpdated, mappingMethod);
        if (geneMap != null && geneMap.containsKey("phenotypeMapList")) addRelatePhenotypes(mimEntry, geneMap, geneId, confidence);
        //        addGeneToSqLite(mimEntry[3], result);

    }

    private long addNewGene(String symbol, String title, String dateUpdated, String mappingMethod) {
        try (Transaction transaction = graphDatabase.beginTx()) {
            final Node node = graphDatabase.createNode(PoirotGraphLabels.GENE);
            if (symbol != null) node.setProperty("symbol", symbol);
            if (title != null) node.setProperty("title", title);
            if (dateUpdated != null) node.setProperty("dateUpdated", dateUpdated);
//            node.setProperty("confidence", confidence);
            node.setProperty("mappingMethod", mappingMethod);
            long geneId = node.getId();
            transaction.success();
            return geneId;
        }
    }

    private void addRelatePhenotypes(OmimEntry mimEntry, JSONObject geneMap, long geneId, String confidence) {
        final JSONArray phenotypeMapList = geneMap.getJSONArray("phenotypeMapList");
        for (int i = 0; i < phenotypeMapList.length(); i++)
            addRelatedPhenotype(mimEntry, geneId, phenotypeMapList.getJSONObject(i).getJSONObject("phenotypeMap"), confidence);
    }

    private void addRelatedPhenotype(OmimEntry mimEntry, long geneId, JSONObject phenotypeMap, String confidence) {
        int phenotypeMimNumber = phenotypeMap.containsKey("phenotypeMimNumber")
                ? phenotypeMap.getInt("phenotypeMimNumber")
                : Integer.valueOf(mimEntry.getMimNumber());
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
            final Relationship relationship = geneNode.createRelationshipTo(phenotypeNode, PoirotGraphRelationships.G2D);
            relationship.setProperty("confidence", confidence);
            relationship.setProperty("database", "omim");
            transaction.success();
        }
    }

    private void addPhenotype(OmimEntry mimEntry, String result) {
        System.out.println("PHENOTYPE:" + result);
        final String id = "omim:" + mimEntry.getMimNumber();
        try (Transaction transaction = graphDatabase.beginTx()) {
            Node node = graphDatabase.findNode(PoirotGraphLabels.PHENOTYPE, "id", id);
            if (node == null) node = graphDatabase.createNode(PoirotGraphLabels.PHENOTYPE);
            final JSONObject root = new JSONObject(result);
            if (root.containsKey("phenotypeMapList")) {
                final JSONObject entry = root.getJSONObject("omim").getJSONArray("entryList").getJSONObject(0).getJSONObject("entry");
                final JSONObject phenotypeMap = entry.getJSONArray("phenotypeMapList").getJSONObject(0).getJSONObject("phenotypeMap");
                node.setProperty("title",phenotypeMap.opt("phenotype"));
                node.setProperty("mappingKey",phenotypeMap.opt("phenotypeMappingKey"));
                node.setProperty("dateUpdated",entry.opt("dateUpdated"));
                if (!node.hasProperty("id")) node.setProperty("id", id);
            }
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
