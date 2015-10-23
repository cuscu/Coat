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
import coat.core.poirot.dataset.hgnc.HGNCDatabase;
import javafx.concurrent.Task;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.zip.GZIPInputStream;

/**
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class HPRDDatasetLoader extends Task<Dataset> {
    private final static String[] HEADERS = {"id", "source", "target", "database", "type", "method"};

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
         /*
                0 interactor_1_geneSymbol   ERRFI1
                1 interactor_1_hprd_id	09218
                2 interactor_1_refseq_id	NP_061821.1
                3 interactor_2_geneSymbol	ERBB2
                4 interactor_2_hprd_id	01281
                5 interactor_2_refseq_id	NP_004439.2
                6 experiment_type	in vitro;yeast 2-hybrid
                7 reference_id	11003669
             */
        final String row[] = line.split("\t");
        final String source = row[0];
        final String target = row[3];
        final String method = row[6];
        final String type = row[6];
        final String database = "HPRD";
        final String id = row[7];
        final String fields[] = new String[HEADERS.length];
        fields[0] = id;
        fields[1] = HGNCDatabase.getStandardSymbol(source);
        fields[2] = HGNCDatabase.getStandardSymbol(target);
        fields[3] = database;
        fields[4] = type;
        fields[5] = method;
        return fields;
    }

    private BufferedReader getReader() throws IOException {
        return new BufferedReader(
                new InputStreamReader(
                        new GZIPInputStream(getClass().getResourceAsStream("hprd-gene-interactions.tsv.gz"))));
    }
}
