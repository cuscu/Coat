package coat.model.vcfreader;

import coat.model.poirot.databases.Dataset;
import coat.model.poirot.databases.VcfLoader;
import org.junit.Test;

import java.io.File;

/**
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class VcfLoaderTest {

    @Test
    public void test() {
        final Dataset dataset = VcfLoader.createDataset(new File("test/s002.vcf"));
        dataset.printValue();
    }
}
