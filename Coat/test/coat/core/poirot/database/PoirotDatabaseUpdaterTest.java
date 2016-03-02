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
import org.junit.Test;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import java.io.File;

/**
 * @author Pascual Lorente Arencibia (pasculorente@gmail.com)
 */
public class PoirotDatabaseUpdaterTest {

    @Test
    public void test() {
//        new DatabaseGenerator().start();
        clearDatabase();
        populateDatabase();
        testDatabase();
    }

    private void clearDatabase() {
        final GraphDatabaseService databaseService = new GraphDatabaseFactory().newEmbeddedDatabase(new File("graphDatabase"));
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
        // Check
        try (Transaction transaction = databaseService.beginTx()) {
            ResourceIterable<Node> nodes = databaseService.getAllNodes();
            nodes.forEach((node) -> System.out.println(node + " " +node.getLabels() + ":" + node.getAllProperties()));
            ResourceIterable<Relationship> allRelationships = databaseService.getAllRelationships();
            allRelationships.forEach((x) -> System.out.println(x + " " + x.getAllProperties()));
            transaction.success();
        }
        databaseService.shutdown();
    }
}
