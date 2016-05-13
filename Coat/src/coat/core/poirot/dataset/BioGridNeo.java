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
import coat.core.poirot.dataset.hgnc.HGNC;
import org.jetbrains.annotations.Nullable;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.GZIPInputStream;

/**
 * Created by uichuimi on 15/03/16.
 */
class BioGridNeo {

    private final static File BIOGRID_FILE = new File("/home/uichuimi/Descargas/BIOGRID-ALL-3.4.134.tab2.txt.gz");
    private final static BioGridNeo BIO_GRID_NEO = new BioGridNeo();
    private final static int CACHE_SIZE = 4;
    private List<Node> nodeCache = new ArrayList<>();
    private long findNodeTime = 0;
    private GraphDatabaseService graphDatabase;

    private BioGridNeo() {
    }

    public static BioGridNeo getInstance() {
        return BIO_GRID_NEO;
    }

    void update(GraphDatabaseService graphDatabase) {
        this.graphDatabase = graphDatabase;
        readInputFile(graphDatabase);
    }

    private void readInputFile(GraphDatabaseService graphDatabase) {
        AtomicInteger counter = new AtomicInteger();
        final long start = System.currentTimeMillis();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(BIOGRID_FILE))))) {
            try (Transaction transaction = graphDatabase.beginTx()) {
                reader.lines().filter(line -> !line.startsWith("#")).map(BioGridEntry::new).forEach(entry -> {
                    final Node aNode = getOrCreateGeneNode(entry.aSymbol);
                    final Node bNode = getOrCreateGeneNode(entry.bSymbol);
                    if (!relationshipExists(entry, aNode)) createRelationship(entry, aNode, bNode);
                    if (counter.incrementAndGet() % 1000 == 0) System.out.println(counter);
                });
                transaction.success();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        long totalTime = System.currentTimeMillis() - start;
        final DateFormat df = new SimpleDateFormat("HH:mm:ss");
        System.out.println("Total time: " + df.format(totalTime));
        System.out.println("Find node time: " + df.format(findNodeTime));
    }

    private Node getOrCreateGeneNode(String symbol) {
        final long start = System.currentTimeMillis();
        Node node = findInCache(symbol);
        if (node == null) {
            node = graphDatabase.findNode(PoirotGraphLabels.GENE, "symbol", symbol);
            if (node == null) {
                node = graphDatabase.createNode(PoirotGraphLabels.GENE);
                node.setProperty("symbol", symbol);
            }
            addToCache(node);
        }
        findNodeTime += System.currentTimeMillis() - start;
        return node;
    }

    @Nullable
    private Node findInCache(String symbol) {
        for (Node n : nodeCache) if (n.getProperty("symbol").equals(symbol)) return n;
        return null;
    }

    private void addToCache(Node node) {
        if (nodeCache.size() == CACHE_SIZE) nodeCache.remove(CACHE_SIZE - 1);
        nodeCache.add(node);
    }

    private boolean relationshipExists(BioGridEntry entry, Node node) {
        for (Relationship relationship : node.getRelationships())
            if (relationship.getProperty("id").equals(entry.db_id)) return true;
        return false;
    }

    private void createRelationship(BioGridEntry entry, Node aNode, Node bNode) {
        final Relationship relationshipTo = aNode.createRelationshipTo(bNode, PoirotGraphRelationships.G2G);
        relationshipTo.setProperty("id", entry.db_id);
        relationshipTo.setProperty("system", entry.system);
        relationshipTo.setProperty("type", entry.type);
        relationshipTo.setProperty("modification", entry.modification);
    }

    private class BioGridEntry {
        final String db_id;
        final String aSymbol;
        final String bSymbol;
        final String system;
        final String type;
        final String modification;

        BioGridEntry(String l) {
            String[] line = l.split("\t");
            db_id = line[23] + ":" + line[0];
            aSymbol = HGNC.getStandardSymbol(line[7]);
            bSymbol = HGNC.getStandardSymbol(line[8]);
            system = line[11];
            type = line[12];
            modification = line[19];
        }
    }

}
