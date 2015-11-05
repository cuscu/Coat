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

import coat.core.poirot.Pearl;
import coat.core.poirot.PearlGraph;
import coat.core.poirot.PearlGraphFactory;
import coat.core.poirot.PearlRelationship;
import coat.core.poirot.dataset.Dataset;
import coat.core.poirot.dataset.Instance;
import coat.core.poirot.dataset.biogrid.Relator;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class HPRDRelator implements Relator {


    private final Dataset hprdDataset;
    private PearlGraph database;
    private Pearl pearl;

    public HPRDRelator() {
        hprdDataset = loadDataset();
    }

    private Dataset loadDataset() {
        try {
            final HPRDDatasetLoader loader = new HPRDDatasetLoader();
            loader.run();
            return loader.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void expand(Pearl pearl, PearlGraph database) {
        this.database = database;
        this.pearl = pearl;
        try {
            hprdDataset.getInstances(pearl.getName(), 1).forEach(instance -> createRelationship(instance, 2));
            hprdDataset.getInstances(pearl.getName(), 2).forEach(instance -> createRelationship(instance, 1));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createRelationship(Instance instance, int index) {
        final String targetSymbol = (String) instance.getField(index);
        if (PearlGraphFactory.notInBlacklist(targetSymbol)) {
            final Pearl target = database.getOrCreate(Pearl.Type.GENE, targetSymbol);
            pearl.getRelationships().putIfAbsent(target, new ArrayList<>()); // Ensure not null value
            final List<PearlRelationship> relationships = pearl.getRelationships().get(target);
            final String id = (String) instance.getField(0);
            if (!relationshipExists(id, relationships)) createRelationship(pearl, target, instance);

        }

    }

    private boolean relationshipExists(String id, List<PearlRelationship> relationships) {
        return relationships.stream()
                .filter(pearlRelationship -> pearlRelationship.getProperties().containsKey("id"))
                .anyMatch(pearlRelationship -> pearlRelationship.getProperties().get("id").equals(id));
    }

    private void createRelationship(Pearl pearl, Pearl target, Instance instance) {
        final PearlRelationship relationship = pearl.createRelationshipTo(target);
        relationship.getProperties().put("database", instance.getField(3));
        relationship.getProperties().put("type", instance.getField(4));
        relationship.getProperties().put("method", instance.getField(5));
    }
}
