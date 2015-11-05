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
import coat.core.poirot.PearlRelationship;
import coat.core.poirot.dataset.Dataset;
import coat.core.poirot.dataset.Instance;
import coat.core.poirot.dataset.biogrid.Relator;

import java.util.concurrent.ExecutionException;

/**
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class HPRDExpressionRelator implements Relator {

    private final Dataset hprdExpressionDataset;
    private PearlGraph database;

    public HPRDExpressionRelator() {
        hprdExpressionDataset = loadHPRDExpressionDataset();
    }

    private Dataset loadHPRDExpressionDataset() {
        try {
            final HPRDExpressionDatasetLoader loader = new HPRDExpressionDatasetLoader();
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
        hprdExpressionDataset.getInstances(pearl.getName(), 2).forEach(instance -> {
            final Pearl phenotype = getHPRDExpressionPearl(instance);
            final PearlRelationship relationship = pearl.createRelationshipTo(phenotype);
            relationship.getProperties().put("id", instance.getField(0));
            relationship.getProperties().put("database", "HPRD");
            relationship.getProperties().put("status", instance.getField(4));

        });
    }

    private Pearl getHPRDExpressionPearl(Instance instance) {
        final String expression = (String) instance.getField(3);
        final Pearl phenotype = database.getOrCreate(Pearl.Type.EXPRESSION, expression);
        phenotype.getProperties().putIfAbsent("database", "HPRD");
        phenotype.getProperties().putIfAbsent("name", instance.getField(3));
        return phenotype;
    }
}
