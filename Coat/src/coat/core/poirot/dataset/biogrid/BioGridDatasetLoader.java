/******************************************************************************
 * Copyright (C) 2015 UICHUIMI                                                *
 *                                                                            *
 * This program is free software: you can redistribute it and/or modify it    *
 * under the terms of the GNU General Public License as published by the      *
 * Free Software Foundation, either version 3 of the License, or (at your     *
 * option) any later version.                                                 *
 *                                                                            *
 * This program is distributed in the hope that it will be useful, but        *
 * WITHOUT ANY WARRANTY; without even the implied warranty of                 *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                       *
 * See the GNU General Public License for more details.                       *
 *                                                                            *
 * You should have received a copy of the GNU General Public License          *
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.      *
 ******************************************************************************/

package coat.core.poirot.dataset.biogrid;

import coat.core.poirot.dataset.Dataset;
import coat.core.poirot.dataset.hgnc.HGNCDatabase;
import javafx.concurrent.Task;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.zip.GZIPInputStream;

/**
 * Interactions between proteins: (0) id, (1) source, (2) target, (3) database, (4) type, (5) method, (6) score
 *
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class BioGridDatasetLoader extends Task<Dataset> {

    private final static String[] HEADERS = {"id", "source", "target", "database", "type", "method", "score"};

    @Override
    protected Dataset call() throws Exception {
        final Dataset dataset = new Dataset();
        dataset.setColumnNames(Arrays.asList(HEADERS));
        try (BufferedReader reader = getReader()) {
            reader.readLine();
            reader.lines().map(this::getFields).forEach(dataset::addInstance);
        }
        dataset.createIndex(1);
        dataset.createIndex(2);
        return dataset;
    }

    private String[] getFields(String line) {
        final String[] split = line.split(",");
        split[1] = HGNCDatabase.getStandardSymbol(split[1]);
        split[2] = HGNCDatabase.getStandardSymbol(split[2]);
        return split;
    }

    private BufferedReader getReader() throws IOException {
        return new BufferedReader(
                new InputStreamReader(
                        new GZIPInputStream(getClass().getResourceAsStream("biogrid.csv.gz"))));
    }
}
