package coat.model.poirot;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class BioGridTest {

    @Test
    public void test() {
        final String gene = "DISC1";
        final List<String> genes = BioGrid.getRelatedGenes(gene);
        final List<String> expected = Arrays.asList("ACTG1", "ACTN2", "AGTPBP1", "AKAP6", "AKAP9", "ARIH2", "ATF4",
                "ATF5", "ATF7IP", "BICD1", "BZRAP1", "C14orf166", "CCDC136", "CCDC141", "CCDC24", "CCDC88A", "CDC5L",
                "CDK5RAP3", "CEP170", "CEP57L1", "CEP63", "CIT", "CLU", "COL4A1", "DCTN1", "DCTN2", "DMD", "DNAJC7",
                "DPYSL2", "DPYSL3", "DST", "DYNC1H1", "Disc1", "EEF2", "EIF3A", "EIF3H", "EXOC1", "EXOC4", "EXOC7",
                "FBXO41", "FEZ1", "FRYL", "GNB1", "GNPTAB", "GPRASP2", "HERC2P2", "IFT20", "IMMT", "ITSN1", "KALRN",
                "KANSL1", "KCNQ5", "KIAA1377", "KIF3A", "KIF3C", "KIFAP3", "MACF1", "MAP1A", "MATR3", "MEMO1", "MLLT10",
                "MPPED1", "MVP", "MYO1A", "MYT1L", "NDE1", "NDEL1", "NEFM", "NUP160", "OLFM1", "PAFAH1B1", "PCNXL4",
                "PDE4B", "PGK1", "PPM1E", "PPP4R1", "PPP5C", "RABGAP1", "RAD21", "RANBP9", "RASSF7", "RBSN", "ROGDI",
                "SH3BP5", "SMARCE1", "SMC2", "SMC3", "SNX6", "SPARCL1", "SPTAN1", "SPTBN1", "SPTBN4", "SRGAP2", "SRGAP3",
                "SRR", "STX18", "SYBU", "SYNE1", "Srr", "TFIP11", "TIAM2", "TNIK", "TNKS", "TRAF3IP1", "TRIO", "TUBB",
                "TUBB2A", "UTRN", "XPNPEP1", "XRN2", "YWHAE", "YWHAG", "YWHAQ", "YWHAZ", "ZNF197", "ZNF365");
        Assert.assertEquals(expected, genes);

    }

    @Test
    public void test2() {
        final String gene = "ZNF211";
        final List<String> genes = BioGrid.getRelatedGenes(gene);
        final List<String> expected = Collections.singletonList("NDEL1");
        Assert.assertEquals(expected, genes);
    }
}
