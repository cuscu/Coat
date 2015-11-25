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

package coat.core.poirot.dataset;

import coat.core.poirot.Pearl;
import coat.core.poirot.PearlGraph;
import coat.core.poirot.PearlRelationship;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.zip.GZIPInputStream;

/**
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class GeneToDiseaseRelator implements Relator {

    private final Dataset dataset = loadDataset();
    private final String FILE = "gene-to-disease.tsv.gz";

    private Dataset loadDataset() {
        final Dataset dataset = new Dataset();
        dataset.setColumnNames(Arrays.asList("database", "id", "gene", "status", "disease.name", "disease.id", "disease.status"));
        try (BufferedReader reader = getReader()) {
            reader.lines()
                    .map(line -> line.split("\t"))
                    .forEachOrdered(dataset::addInstance);
        } catch (IOException e) {
            e.printStackTrace();
        }
        dataset.createIndex(2);
        return dataset;
    }

    private BufferedReader getReader() throws IOException {
        return new BufferedReader(
                new InputStreamReader(
                        new GZIPInputStream(GeneToExpressionRelator.class.getResourceAsStream(FILE))));
    }

    @Override
    public void expand(Pearl pearl, PearlGraph database) {
        dataset.getInstances(pearl.getName(), 2).forEach(instance -> {
            final Pearl disease = database.getOrCreate(Pearl.Type.DISEASE, (String) instance.getField(4));
            disease.getProperties().putIfAbsent("name", instance.getField(4));
            disease.getProperties().putIfAbsent("id", instance.getField(5));
            disease.getProperties().putIfAbsent("status", instance.getField(6));
            final PearlRelationship relationship = pearl.createRelationshipTo(disease);
            relationship.getProperties().put("database", instance.getField(0));
            relationship.getProperties().put("type", instance.getField(3));
        });

    }
}
