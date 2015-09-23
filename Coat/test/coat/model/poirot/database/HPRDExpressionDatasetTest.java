package coat.model.poirot.database;

import coat.model.poirot.databases.Dataset;
import coat.model.poirot.databases.HPRDExpressionDatasetLoader;
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
public class HPRDExpressionDatasetTest {

    private static Dataset dataset;

    @BeforeClass
    public static void start() {
        final HPRDExpressionDatasetLoader loader = new HPRDExpressionDatasetLoader();
        Platform.runLater(loader);
        try {
            dataset = loader.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void size() {
        Assert.assertEquals(112158, dataset.getInstances().size());
    }

    @Test
    public void columnNames() {
        Assert.assertEquals("hprd_id", dataset.getColumnNames().get(0));
    }

    @Test
    public void directInstanceAccess() {
        Assert.assertEquals("Keratinocyte", dataset.getInstances().get(3000).getField(3));
    }

    @Test
    public void indexInstanceAccess() {
        Assert.assertEquals(3, dataset.getInstances("ENO3", 2).size());
        Assert.assertEquals("Muscle", dataset.getInstances("ENO3", 2).get(0).getField("expression"));
    }


}
