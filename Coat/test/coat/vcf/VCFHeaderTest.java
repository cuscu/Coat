/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package coat.vcf;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author UICHUIMI
 */
public class VCFHeaderTest {

    /**
     * Test of getInfos method, of class VCFHeader.
     */
    @Test
    public void testGetInfos() {
        System.out.println("getInfos");
        File testFile = new File("/home/unidad03/NetBeansProjects/Coat/Coat/test/s002.vcf");
        VCFHeader instance = new VCFHeader(testFile);
        /*
         ##INFO=<ID=AC,Number=A,Type=Integer,Description="Allele count in genotypes, for each ALT allele, in the same order as listed">
         ##INFO=<ID=AF,Number=A,Type=Float,Description="Allele Frequency, for each ALT allele, in the same order as listed">
         ##INFO=<ID=AN,Number=1,Type=Integer,Description="Total number of alleles in called genotypes">
         ##INFO=<ID=HaplotypeScore,Number=1,Type=Float,Description="Consistency of the site with at most two segregating haplotypes">
         ##INFO=<ID=ReadPosRankSum,Number=1,Type=Float,Description="Z-score from Wilcoxon rank sum test of Alt vs. Ref read position bias">
         ##INFO=<ID=SOR,Number=1,Type=Float,Description="Symmetric Odds Ratio of 2x2 contingency table to detect strand bias">
         */
        Map<String, String> ac = new LinkedHashMap();
        ac.put("ID", "AC");
        ac.put("Number", "A");
        ac.put("Type", "Integer");
        ac.put("Description", "Allele count in genotypes, for each ALT allele, in the same order as listed");
        Map<String, String> af = new LinkedHashMap();
        af.put("ID", "AF");
        af.put("Number", "A");
        af.put("Type", "Float");
        af.put("Description", "Allele Frequency, for each ALT allele, in the same order as listed");
        Map<String, String> an = new LinkedHashMap();
        an.put("ID", "AN");
        an.put("Number", "1");
        an.put("Type", "Integer");
        an.put("Description", "Total number of alleles in called genotypes");
        Map<String, String> hs = new LinkedHashMap();
        hs.put("ID", "HaplotypeScore");
        hs.put("Number", "1");
        hs.put("Type", "Float");
        hs.put("Description", "Consistency of the site with at most two segregating haplotypes");
        Map<String, String> rp = new LinkedHashMap();
        rp.put("ID", "ReadPosRankSum");
        rp.put("Number", "1");
        rp.put("Type", "Float");
        rp.put("Description", "Z-score from Wilcoxon rank sum test of Alt vs. Ref read position bias");
        Map<String, String> sor = new LinkedHashMap();
        sor.put("ID", "SOR");
        sor.put("Number", "1");
        sor.put("Type", "Float");
        sor.put("Description", "Symmetric Odds Ratio of 2x2 contingency table to detect strand bias");
        List<Map<String, String>> expResult = new ArrayList();
        expResult.add(ac);
        expResult.add(af);
        expResult.add(an);
        expResult.add(hs);
        expResult.add(rp);
        expResult.add(sor);
        List<Map<String, String>> result = instance.getInfos();
        assertEquals(expResult, result);
    }

}
