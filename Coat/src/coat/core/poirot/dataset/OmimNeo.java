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

import coat.Coat;
import coat.core.poirot.dataset.graph.PoirotGraphLabels;
import coat.core.poirot.dataset.graph.PoirotGraphRelationships;
import coat.core.poirot.dataset.hgnc.HGNC;
import coat.json.JSONArray;
import coat.json.JSONObject;
import coat.utils.OS;
import org.jetbrains.annotations.NotNull;
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
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Call <code>start()</code> to update the OMIM cache.
 * <p>
 * Created by uichuimi on 29/02/16.
 */
public class OmimNeo {

    private final static String BASE_URL = "http://api.europe.omim.org";
    private final static String API_KEY = "fywuEtWWRRWIpQX96-5BXQ";
    private final static String MIM_LIST = "http://www.omim.org/static/omim/data/mim2gene.txt";

    private final static int MAX_MIM_NUMBERS = 20;

    private final GraphDatabaseService graphDatabase;
    private long SYSTEM_EPOCH_UPDATED;
    private long SESSION_EPOCH_UPDATE;

    private long webTime = 0L;
    private long processTime = 0L;

    private int totalEntries;
    private int processedEntries = 0;

    public OmimNeo(GraphDatabaseService graphDatabase) {
        this.graphDatabase = graphDatabase;
    }

    public void start() {
        System.out.println("Updating OMIM data...");
        fetchLastOmimUpdate();
        System.out.println("Downloading OMIM list...");
        final List<OmimEntry> omimEntries = readMimNumbers();
        totalEntries = omimEntries.size();
        System.out.println("List updated");
//        final List<Integer> toUpdate = getMimEntriesToUpdate(omimEntries);
        update(omimEntries);
        Coat.getProperties().setProperty("last_omim_epoch_update", String.valueOf(SESSION_EPOCH_UPDATE));
        System.out.println("Updated to epoch: " + SESSION_EPOCH_UPDATE);
    }

    private void fetchLastOmimUpdate() {
        final String last_omim_epoch_update = Coat.getProperties().getProperty("last_omim_epoch_update", "1457683200");
        SYSTEM_EPOCH_UPDATED = Long.valueOf(last_omim_epoch_update);
        SESSION_EPOCH_UPDATE = SYSTEM_EPOCH_UPDATED;
        System.out.println("last system epoch: " + SYSTEM_EPOCH_UPDATED);
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


    private void update(List<OmimEntry> toUpdate) {
        System.out.println(toUpdate.size() + " entries must be updated");
        for (int i = 0; i < toUpdate.size(); i += MAX_MIM_NUMBERS) {
            int to = i + MAX_MIM_NUMBERS;
            if (to > toUpdate.size()) to = toUpdate.size();
            final JSONObject root = fetchWholeEntries(toUpdate.subList(i, to));
            addEntries(root);
        }
    }

    private JSONObject fetchWholeEntries(List<OmimEntry> omimEntryList) {
        final StringBuilder builder = new StringBuilder(BASE_URL);
        builder.append("/api/entry?include=dates,geneMap&format=json&apiKey=").append(API_KEY).append("&mimNumber=").append(omimEntryList.get(0).getMimNumber());
        for (int i = 1; i < omimEntryList.size(); i++) builder.append(",").append(omimEntryList.get(i).getMimNumber());
        return new JSONObject(getUrlResult(builder.toString()));
    }

    @NotNull
    private String generateId(String mimNumber) {
        return "omim:" + mimNumber;
    }

    @Nullable
    private String getUrlResult(String textUrl) {
        long startTime = System.currentTimeMillis();
        String result = null;
        try {
            final URL url = new URL(textUrl);
            url.openConnection();
            final BufferedReader reader = new BufferedReader(new InputStreamReader((InputStream) url.getContent()));
            final StringBuilder builder = new StringBuilder();
            reader.lines().forEach(builder::append);
            result = builder.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        webTime += System.currentTimeMillis() - startTime;
        return result;
    }

    private void printProgress() {
        if (++processedEntries % 1000 == 0) {
            if (processedEntries == 1000) System.out.println("Progress\tWebTime\tCpuTime");
            System.out.println(String.format("%d/%d (%.2f%%)\t%s\t%s", processedEntries, totalEntries,
                    (processedEntries * 100.0 / totalEntries),
                    OS.humanReadableTime(webTime), OS.humanReadableTime(processTime)));
        }
    }

    private void addEntries(JSONObject root) {
        final long startTime = System.currentTimeMillis();
        final JSONArray entryList = root.getJSONObject("omim").getJSONArray("entryList");
        for (int i = 0; i < entryList.length(); i++) addEntry(entryList.getJSONObject(i).getJSONObject("entry"));
        processTime += System.currentTimeMillis() - startTime;
    }

    private void addEntry(JSONObject entry) {
        printProgress();
        final long epochUpdated = entry.getLong("epochUpdated");
        if (epochUpdated > SESSION_EPOCH_UPDATE) SESSION_EPOCH_UPDATE = epochUpdated;
        if (epochUpdated > SYSTEM_EPOCH_UPDATED) addUpdatedEntry(entry);
    }

    private void addUpdatedEntry(JSONObject entry) {
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
        final String prefix = entry.optString("prefix");
        if (prefix.equals("*") || prefix.equals("+")) addGene(entry);
        else if (prefix.equals("%") || prefix.equals("#") || prefix.equals("")) addPhenotype(entry);
    }

    private void addGene(JSONObject entry) {
//        System.out.println("GENE: " + entry);
        if (!entry.containsKey("geneMap")) return;
        final JSONObject geneMap = entry.getJSONObject("geneMap");
        final String symbol = HGNC.getStandardSymbol(geneMap.getString("geneSymbols").split(",")[0]);
        final String name = entry.getJSONObject("titles").getString("preferredTitle");
        final String confidence = geneMap.getString("confidence");
        final String mappingMethod = geneMap.optString("mappingMethod");
        final String mimNumber = String.valueOf(entry.getLong("mimNumber"));
        final long geneId = createGene(generateId(mimNumber), symbol, name, mappingMethod);
        addRelatedPhenotypes(mimNumber, geneMap, geneId, confidence);

    }

    private long createGene(String id, String symbol, String name, String mappingMethod) {
//        System.out.println("Adding " + symbol);
        try (Transaction transaction = graphDatabase.beginTx()) {
            final Node node = graphDatabase.createNode(PoirotGraphLabels.GENE);
            node.setProperty("symbol", symbol);
            node.setProperty("name", name);
//            node.setProperty("id", id);
            node.setProperty("mappingMethod", mappingMethod);
            long geneId = node.getId();
            transaction.success();
            return geneId;
        }
    }

    private void addRelatedPhenotypes(String defaultMimNumber, JSONObject geneMap, long geneId, String confidence) {
        if (geneMap.containsKey("phenotypeMapList")) {
            final JSONArray phenotypeMapList = geneMap.getJSONArray("phenotypeMapList");
            for (int i = 0; i < phenotypeMapList.length(); i++)
                addRelatedPhenotype(defaultMimNumber, geneId, phenotypeMapList.getJSONObject(i).getJSONObject("phenotypeMap"), confidence);
        }
    }

    private void addRelatedPhenotype(String defaultMimNumber, long geneId, JSONObject phenotypeMap, String confidence) {
        final String phenotypeMimNumber = phenotypeMap.containsKey("phenotypeMimNumber")
                ? String.valueOf(phenotypeMap.getInt("phenotypeMimNumber"))
                : defaultMimNumber;
        final String omimId = generateId(phenotypeMimNumber);
        final String phenotypeName = phenotypeMap.getString("phenotype");
        try (Transaction transaction = graphDatabase.beginTx()) {
            final Node geneNode = graphDatabase.getNodeById(geneId);
            final Node phenotypeNode = getOrCreatePhenotype(omimId);
            phenotypeNode.setProperty("name", phenotypeName);
            phenotypeNode.setProperty("id", omimId);
            if (phenotypeMap.containsKey("phenotypeMappingKey")) {
                final int phenotypeMappingKey = phenotypeMap.getInt("phenotypeMappingKey");
                phenotypeNode.setProperty("mappingKey", phenotypeMappingKey);
            }
            addRelationship(confidence, geneNode, phenotypeNode);
            transaction.success();
        }
    }

    private Node getOrCreatePhenotype(String omimId) {
        final Node phenotypeNode = graphDatabase.findNode(PoirotGraphLabels.PHENOTYPE, "id", omimId);
        return phenotypeNode != null ? phenotypeNode : graphDatabase.createNode(PoirotGraphLabels.PHENOTYPE);
    }

    private void addRelationship(String confidence, Node geneNode, Node phenotypeNode) {
        final Relationship relationship = findOrCreate(geneNode, phenotypeNode);
        relationship.setProperty("confidence", confidence);
        relationship.setProperty("database", "omim");
    }

    private Relationship findOrCreate(Node geneNode, Node phenotypeNode) {
        final Iterable<Relationship> relationships = geneNode.getRelationships(PoirotGraphRelationships.G2D);
        for (Relationship r : relationships) if (r.getOtherNode(geneNode).equals(phenotypeNode)) return r;
        return geneNode.createRelationshipTo(phenotypeNode, PoirotGraphRelationships.G2D);
    }

    private void addPhenotype(JSONObject entry) {
//        System.out.println("PHENOTYPE:" + entry);
        final String id = generateId(String.valueOf(entry.getLong("mimNumber")));
        try (Transaction transaction = graphDatabase.beginTx()) {
            Node node = graphDatabase.findNode(PoirotGraphLabels.PHENOTYPE, "id", id);
            if (node == null) {
                node = graphDatabase.createNode(PoirotGraphLabels.PHENOTYPE);
//                System.out.println("Adding " + mimEntry);
                node.setProperty("id", id);
            }
            if (entry.containsKey("phenotypeMapList")) {
                final JSONObject phenotypeMap = entry.getJSONArray("phenotypeMapList").getJSONObject(0).getJSONObject("phenotypeMap");
                node.setProperty("title", phenotypeMap.opt("phenotype"));
                if (phenotypeMap.containsKey("phenotypeMappingKey"))
                    node.setProperty("mappingKey", phenotypeMap.opt("phenotypeMappingKey"));
//                node.setProperty("dateUpdated", entry.opt("dateUpdated"));
                node.setProperty("database", "omim");
            }
            if (!node.hasProperty("id")) node.setProperty("id", id);
            transaction.success();
        }

    }
}
