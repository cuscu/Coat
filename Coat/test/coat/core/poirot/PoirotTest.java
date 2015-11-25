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

package coat.core.poirot;

import coat.core.vcf.VcfFile;
import de.saxsys.javafx.test.JfxRunner;
import javafx.application.Platform;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.concurrent.ExecutionException;

/**
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
@RunWith(JfxRunner.class)
public class PoirotTest {


    PearlGraph database;

    @Before
    public void start() {
        final File file = new File("test/coat/core/poirot/agua.vcf");
        final VcfFile vcfFile = new VcfFile(file);
        final GraphFactory analysis = new GraphFactory(vcfFile.getVariants());
        Platform.runLater(analysis);
        try {
            database = analysis.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testDatabaseLoaded() {
        Assert.assertEquals(1138, database.numberOfPearls(Pearl.Type.GENE));
    }

    @Test
    public void test2() {
//        final List<String> phenotypes = Arrays.asList("Kidney", "Brain");
//        final GraphEvaluator two = new GraphEvaluator(database, phenotypes);
//        Platform.runLater(two);
//        try {
//            two.get();
//        } catch (InterruptedException | ExecutionException e) {
//            e.printStackTrace();
//        }
//        Assert.assertEquals(0, database.getPearl(Pearl.Type.EXPRESSION, "Brain").getDistanceToPhenotype());
    }

}
