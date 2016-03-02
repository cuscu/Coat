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
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class PoirotDatabaseUpdater {

//    private Connection connection;

    private GraphDatabaseService graphDatabase;

    public void start() {
//        connection = createConnection();
        graphDatabase = new GraphDatabaseFactory().newEmbeddedDatabase(new File("graphDatabase"));
//        registerShutdownHook();
//        createStructure();
        performSomeTasks();
        closeConnection();
    }

    private void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void run() {
                graphDatabase.shutdown();
            }
        });
    }

//    private Connection createConnection() {
//        createDatabaseIfNotExists();
//        try {
//            Class.forName("org.sqlite.JDBC");
//            return DriverManager.getConnection("jdbc:sqlite:poirot.sqlite");
//        } catch (ClassNotFoundException | SQLException e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
//
//    private void createDatabaseIfNotExists() {
//        try {
//            final File file = new File("poirot.sqlite");
//            if (!file.exists()) file.createNewFile();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

//    private void createStructure() {
//        try {
//            final Statement statement = connection.createStatement();
//            statement.addBatch("CREATE TABLE IF NOT EXISTS Genes (" +
//                    "id INTEGER NOT NULL PRIMARY KEY," +
//                    "name UNIQUE" +
//                    ")");
//            statement.addBatch("CREATE TABLE IF NOT EXISTS Phenotypes (" +
//                    "id INTEGER NOT NULL PRIMARY KEY," +
//                    "name UNIQUE," +
//                    "database TEXT," +
//                    "database_id TEXT," +
//                    "status TEXT," +
//                    "date_updated TEXT" +
//                    ")");
//            statement.addBatch("CREATE TABLE IF NOT EXISTS Gene2Phenotype (" +
//                    "id INTEGER NOT NULL PRIMARY KEY," +
//                    "gene_id INTEGER," +
//                    "phenotype_id INTEGER," +
//                    "database TEXT," +
//                    "database_id TEXT," +
//                    "status TEXT" +
//                    ")");
//            statement.executeBatch();
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//    }

    private void performSomeTasks() {
        addOmimData();
        addBioGridData();
    }

    private void addOmimData() {
        final OmimNeo omimNeo = new OmimNeo(graphDatabase);
        omimNeo.start();
//        new OmimRetriever().updateData(connection, graphDatabase);
    }

    private void addBioGridData() {
//        new BioGridRetriever().updateData(connection);
    }

    private void closeConnection() {
//        try {
//            connection.close();
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
        graphDatabase.shutdown();
    }

}
