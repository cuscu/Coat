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

import de.saxsys.mvvmfx.testingutils.jfxrunner.JfxRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import poirot.core.Pearl;
import poirot.core.PearlGraph;
import vcf.VcfFile;
import vcf.VcfFileFactory;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
@RunWith(JfxRunner.class)
public class PhenotypeScoreTest {

    private final File file = new File("/home/unidad03/Copy/DAM.VEP.05.prefino.vcf");

    @Test
    public void test() {
        final VcfFile vcfFile = VcfFileFactory.createFromFile(file);
        final GraphFactory generator = new GraphFactory(vcfFile.getVariants());
        try {
            generator.run();
            final PearlGraph pearlDatabase = generator.get();
            PhenotypeScore.score(pearlDatabase);
            final List<Pearl> pearls = pearlDatabase.getPearls(Pearl.Type.DISEASE);
            pearlDatabase.getPearls(Pearl.Type.TISSUE).forEach(pearls::add);
            Collections.sort(pearls, (p2, p1) -> Double.compare(p1.getScore(), p2.getScore()));
            final AtomicInteger count = new AtomicInteger();
            pearls.forEach(pearl -> System.out.println(count.incrementAndGet() + " \t" + pearl.getScore() + " \t" + pearl.getId()));
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

    }
}
