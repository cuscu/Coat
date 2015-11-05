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

package coat.core.poirot.dataset.omim;

import coat.core.poirot.Pearl;
import coat.core.poirot.PearlGraph;
import coat.core.poirot.PearlRelationship;
import coat.core.poirot.dataset.Dataset;
import coat.core.poirot.dataset.Instance;
import coat.core.poirot.dataset.biogrid.Relator;

import java.util.concurrent.ExecutionException;

/**
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class OmimRelator implements Relator {

    private final Dataset omimDataset;
    private PearlGraph database;

    public OmimRelator() {
        this.omimDataset = loadOmimDataset();
    }


    private Dataset loadOmimDataset() {
        try {
            final OmimDatasetLoader loader = new OmimDatasetLoader();
            loader.run();
            return loader.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void expand(Pearl pearl, PearlGraph database) {
        this.database = database;
        omimDataset.getInstances(pearl.getName(), 0).forEach(instance -> {
            final Pearl phenotype = getOmimPhenotypePearl(instance);
            final PearlRelationship relationship = pearl.createRelationshipTo(phenotype);
            final String confidence = (String) instance.getField(3);
            relationship.getProperties().put("confidence", confidence);
            relationship.getProperties().put("database", "OMIM");
        });
    }

    private Pearl getOmimPhenotypePearl(Instance instance) {
        final String name = (String) instance.getField(4);
        final Pearl phenotype = database.getOrCreate(Pearl.Type.DISEASE, name);
        putProperties(instance, phenotype);
        return phenotype;
    }

    private void putProperties(Instance instance, Pearl phenotype) {
        phenotype.getProperties().putIfAbsent("name", instance.getField(4));
        phenotype.getProperties().putIfAbsent("mappingKey", instance.getField(6));
        phenotype.getProperties().putIfAbsent("mimNumber", instance.getField(5));
        phenotype.getProperties().putIfAbsent("database", "OMIM");
    }

}
