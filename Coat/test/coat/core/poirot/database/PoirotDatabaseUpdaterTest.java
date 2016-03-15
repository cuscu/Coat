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
package coat.core.poirot.database;

import coat.core.poirot.dataset.PoirotDatabaseUpdater;
import coat.core.poirot.dataset.graph.PoirotGraphLabels;
import org.junit.Test;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import java.io.File;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Pascual Lorente Arencibia (pasculorente@gmail.com)
 */
public class PoirotDatabaseUpdaterTest {

    @Test
    public void test() {
//        clearDatabase();
        populateDatabase();
        testDatabase();
    }

    private void clearDatabase() {
        final GraphDatabaseService databaseService = new GraphDatabaseFactory().newEmbeddedDatabase(new File("graphDatabase"));
        shutdownWithSystem(databaseService);
        try (Transaction transaction = databaseService.beginTx()) {
            databaseService.getAllNodes().forEach((node) -> {
                node.getRelationships().forEach(Relationship::delete);
                node.delete();
            });
            transaction.success();
        }
        databaseService.shutdown();
    }

    private void populateDatabase() {
        new PoirotDatabaseUpdater().start();
    }

    private void testDatabase() {
        final GraphDatabaseService databaseService = new GraphDatabaseFactory().newEmbeddedDatabase(new File("graphDatabase"));
        shutdownWithSystem(databaseService);
        // Check
        try (Transaction transaction = databaseService.beginTx()) {
            final AtomicInteger phenotypeCounter = new AtomicInteger();
            final AtomicInteger geneCount = new AtomicInteger();
            final AtomicInteger diseaseCount = new AtomicInteger();
            final AtomicInteger tissueCount = new AtomicInteger();
            final AtomicInteger relationshipCount = new AtomicInteger();
            databaseService.findNodes(PoirotGraphLabels.GENE).forEachRemaining(node -> {
                geneCount.incrementAndGet();
                node.getRelationships(Direction.OUTGOING).forEach(relationship -> relationshipCount.incrementAndGet());
            });
            databaseService.findNodes(PoirotGraphLabels.PHENOTYPE).forEachRemaining(node -> {
                phenotypeCounter.incrementAndGet();
                node.getRelationships(Direction.OUTGOING).forEach(relationship -> relationshipCount.incrementAndGet());
            });
            databaseService.findNodes(PoirotGraphLabels.DISEASE).forEachRemaining(node -> {
                diseaseCount.incrementAndGet();
                node.getRelationships(Direction.OUTGOING).forEach(relationship -> relationshipCount.incrementAndGet());
            });
            databaseService.findNodes(PoirotGraphLabels.TISSUE).forEachRemaining(node -> {
                tissueCount.incrementAndGet();
                node.getRelationships(Direction.OUTGOING).forEach(relationship -> relationshipCount.incrementAndGet());
            });
            System.out.println("Genes: " + geneCount.toString());
            System.out.println("Phenotypes: " + phenotypeCounter.toString());
            System.out.println("Diseases: " + diseaseCount.toString());
            System.out.println("Tissues: " + tissueCount.toString());
            System.out.println("Relationships: " + relationshipCount.toString());
            transaction.success();
        }
        databaseService.shutdown();
    }

    private void shutdownWithSystem(final GraphDatabaseService databaseService) {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public synchronized void start() {
                if (databaseService.isAvailable(1000)) databaseService.shutdown();
            }
        });
    }
}
