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

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import java.io.File;

/**
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class PoirotDatabaseUpdater {


    private GraphDatabaseService graphDatabase;

    public void start() {
        graphDatabase = new GraphDatabaseFactory().newEmbeddedDatabase(new File("graphDatabase"));
        shutdownWithSystem(graphDatabase);
        performSomeTasks();
        closeConnection();
    }

    private void performSomeTasks() {
//        addOmimData();
        addBioGridData();
    }

    private void addOmimData() {
        final OmimNeo omimNeo = new OmimNeo(graphDatabase);
        omimNeo.start();
    }

    private void addBioGridData() {
        BioGridNeo.update(graphDatabase);
    }

    private void closeConnection() {
        graphDatabase.shutdown();
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
