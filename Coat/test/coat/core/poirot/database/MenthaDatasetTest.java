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

package coat.core.poirot.database;

import coat.core.poirot.dataset.Dataset;
import coat.core.poirot.dataset.mentha.MenthaDatasetLoader;
import de.saxsys.javafx.test.JfxRunner;
import javafx.application.Platform;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.ExecutionException;

/**
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
@RunWith(JfxRunner.class)
public class MenthaDatasetTest {

    private static Dataset dataset;

    @BeforeClass
    public static void start() {
        final MenthaDatasetLoader loader = new MenthaDatasetLoader();
        Platform.runLater(loader);
        try {
            dataset = loader.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void size() {
        Assert.assertEquals(385856, dataset.getInstances().size());
    }

    @Test
    public void columnNames() {
        Assert.assertEquals("id", dataset.getColumnNames().get(0));
    }

    @Test
    public void directInstanceAccess() {
        Assert.assertEquals("biogrid", dataset.getInstances().get(3000).getField(3));
    }

    @Test
    public void indexInstanceAccess() {
        dataset.createIndex(1);
        Assert.assertEquals(284, dataset.getInstances("DISC1", 1).size());
        Assert.assertEquals("EEF2", dataset.getInstances("DISC1", 1).get(0).getField(2));
    }
}
