package coat.model;

import coat.model.poirot.Omim;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

/**
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class OmimTest {

    @Test
    public void test () {
        final List<String> phenotypes = Arrays.asList();
        final List<String> get = Omim.getRelatedPhenotyes("CLECL1");
        Assert.assertEquals(phenotypes, get);
    }
    @Test
    public void testTwo () {
        final List<String> phenotypes = Arrays.asList("Medullary cystic kidney disease 1, 174000 (3)");
        final List<String> get = Omim.getRelatedPhenotyes("MUC1");
        Assert.assertEquals(phenotypes, get);
    }
}
