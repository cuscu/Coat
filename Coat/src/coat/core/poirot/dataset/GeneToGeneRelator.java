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


import poirot.core.Pearl;
import poirot.core.PearlGraph;
import poirot.core.PearlRelationship;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.zip.GZIPInputStream;

/**
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class GeneToGeneRelator implements Relator {

    private final String FILE = "gene-to-gene.tsv.gz";
    private final Dataset dataset = loadDataset();

    private Dataset loadDataset() {
        final Dataset dataset = new Dataset();
        dataset.setColumnNames(Arrays.asList("database", "id", "source", "target", "method", "type", "score"));
        try (BufferedReader reader = getReader()) {
            reader.lines()
                    .map(line -> line.split("\t"))
                    .forEachOrdered(dataset::addInstance);
        } catch (IOException e) {
            e.printStackTrace();
        }
        dataset.createIndex(2);
        dataset.createIndex(3);
        return dataset;
    }

    private BufferedReader getReader() throws IOException {
        return new BufferedReader(
                new InputStreamReader(
                        new GZIPInputStream(GeneToGeneRelator.class.getResourceAsStream(FILE))));
    }

    @Override
    public void expand(Pearl pearl, PearlGraph database) {
        dataset.getInstances(pearl.getId(), 2).forEach(instance -> {
            final Pearl target = database.getOrCreate(Pearl.Type.GENE, (String) instance.getField(3));
            createRelationship(pearl, instance, target);
        });
        dataset.getInstances(pearl.getId(), 3).forEach(instance -> {
            final Pearl target = database.getOrCreate(Pearl.Type.GENE, (String) instance.getField(2));
            createRelationship(pearl, instance, target);
        });
    }

    private void createRelationship(Pearl pearl, Instance instance, Pearl target) {
        if (relationshipExists(pearl, instance, target)) return;
        final PearlRelationship relationship = pearl.createRelationshipTo(target);
        relationship.getProperties().put("database", (String) instance.getField(0));
        relationship.getProperties().put("id", (String) instance.getField(1));
        relationship.getProperties().put("method", (String) instance.getField(4));
        relationship.getProperties().put("type", (String) instance.getField(5));
        relationship.getProperties().put("score", (String) instance.getField(6));
    }

    private boolean relationshipExists(Pearl pearl, Instance instance, Pearl target) {
        if (pearl.getRelationships().containsKey(target))
            for (PearlRelationship relationship : pearl.getRelationships().get(target))
                if (relationship.getProperties().get("id").equals(instance.getField(1))) return true;
        return false;
    }
}
