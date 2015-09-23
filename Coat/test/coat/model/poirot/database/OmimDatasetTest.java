package coat.model.poirot.database;

import coat.model.poirot.databases.Dataset;
import coat.model.poirot.databases.OmimDatasetLoader;
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
public class OmimDatasetTest {

    private static Dataset dataset;

    @BeforeClass
    public static void start() {
        final OmimDatasetLoader loader = new OmimDatasetLoader();
        Platform.runLater(loader);
        try {
            dataset = loader.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        dataset.getInstances().stream()
                .filter(instance -> instance.getField(4) != null && ((String) instance.getField(4)).contains("Schizo"))
                .forEach(instance -> System.out.println(instance.getField(5) + " " + instance.getField(4)));
    }

    @Test
    public void size() {
        Assert.assertEquals(6938, dataset.getInstances().size());
    }

    @Test
    public void columnNames() {
        Assert.assertEquals("gene_symbol", dataset.getColumnNames().get(0));
    }

    @Test
    public void directInstanceAccess() {
        Assert.assertEquals("P", dataset.getInstances().get(3000).getField(3));
    }

    @Test
    public void indexInstanceAccess() {
        Assert.assertEquals(2, dataset.getInstances("DISC1", 0).size());
        Assert.assertEquals("disrupted in schizophrenia 1", dataset.getInstances("DISC1", 0).get(0).getField(1));
    }


}
