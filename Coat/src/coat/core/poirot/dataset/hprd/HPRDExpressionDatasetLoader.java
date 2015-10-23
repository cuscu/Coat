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

package coat.core.poirot.dataset.hprd;

import coat.core.poirot.dataset.Dataset;
import coat.core.poirot.dataset.Instance;
import coat.core.poirot.dataset.hgnc.HGNCDatabase;
import javafx.concurrent.Task;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

/**
 * Loads the HPRD expression database, which relates genes to where they are expressed in body. (0) hprd_id,
 * (1) refseq_id, (2) symbol, (3) expression, (4) status, (5) reference_id.
 *
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class HPRDExpressionDatasetLoader extends Task<Dataset> {

    public static final String[] HEADERS = new String[]{"hprd_id", "refseq_id", "symbol", "expression", "status", "reference_id"};
    private Dataset dataset = new Dataset();

    @Override
    protected Dataset call() throws Exception {
        loadEntries();
        setColumnNames();
        return dataset;
    }

    private void setColumnNames() {
        dataset.setColumnNames(Arrays.asList(HEADERS));
    }

    private void loadEntries() {
        final List<Instance> collect = readFileInstances();
        if (collect != null) {
            dataset.getInstances().addAll(collect);
            dataset.createIndex(2);
        }
    }

    private List<Instance> readFileInstances() {
        try (BufferedReader reader = getReader()) {
            return reader.lines()
                    .map(this::getFields)
                    .map(line -> new Instance(dataset, line))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private BufferedReader getReader() throws IOException {
        return new BufferedReader(
                new InputStreamReader(
                        new GZIPInputStream(getClass().getResourceAsStream("hprd-phenotypes.tsv.gz"))));
    }

    private String[] getFields(String line) {
        final String[] split = line.split("\t");
        split[2] = HGNCDatabase.getStandardSymbol(split[2]);
        return split;
    }


}
