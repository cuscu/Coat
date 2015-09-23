package coat.model.poirot;

import coat.model.vcfreader.VcfFile;
import de.saxsys.javafx.test.JfxRunner;
import javafx.application.Platform;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
@RunWith(JfxRunner.class)
public class PoirotTest {


    PearlDatabase database;

    @Before
    public void start() {
        final File file = new File("test/coat/model/poirot/agua.vcf");
        final VcfFile vcfFile = new VcfFile(file);
        final PoirotGraphAnalysis analysis = new PoirotGraphAnalysis(vcfFile.getVariants());
        Platform.runLater(analysis);
        try {
            database = analysis.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testDatabaseLoaded() {
        Assert.assertEquals(969, database.numberOfPearls("gene"));
    }

    @Test
    public void test2() {
        final List<String> phenotypes = Arrays.asList("Kidney", "Brain");
        final GraphEvaluator two = new GraphEvaluator(database, phenotypes);
        Platform.runLater(two);
        try {
            two.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        Assert.assertEquals(0, database.getPearl("Brain", "phenotype").getDistanceToPhenotype());
    }

}
