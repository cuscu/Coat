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
package coat.core.vcfreader;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.util.*;

import static org.junit.Assert.assertEquals;

/**
 * @author UICHUIMI
 */
public class VcfFileTest {

    private static final File s002 = new File("test/s002.vcf");
    private static final File test = new File("test/test.vcf");

    @Test
    public void size() {
        // Given
        VcfFile vcfFile = new VcfFile(s002);
        // Then
        Assert.assertEquals(18, vcfFile.getVariants().size());
    }

    @Test
    public void size2() {
        // Given
        VcfFile vcfFile = new VcfFile(test);
        // Then
        Assert.assertEquals(21956, vcfFile.getVariants().size());
    }

    @Test
    public void infos() {
        // Given
        VcfFile vcfFile = new VcfFile(test);
        // Then
        Assert.assertEquals(3, vcfFile.getInfos().size());
    }

    @Test
    public void infos2() {
        // Given
        VcfFile vcfFile = new VcfFile(s002);
        // Then
        Assert.assertEquals(6, vcfFile.getInfos().size());
    }

    @Test
    public void testGetInfos() {
        File testFile = new File("test/s002.vcf");

        VcfFile vcfFile = new VcfFile(testFile);

        /*
         ##INFO=<ID=AC,Number=A,Type=Integer,Description="Allele count in genotypes, for each ALT allele, in the same order as listed">
         ##INFO=<ID=AF,Number=A,Type=Float,Description="Allele Frequency, for each ALT allele, in the same order as listed">
         ##INFO=<ID=AN,Number=1,Type=Integer,Description="Total number of alleles in called genotypes">
         ##INFO=<ID=HaplotypeScore,Number=1,Type=Float,Description="Consistency of the site with at most two segregating haplotypes">
         ##INFO=<ID=ReadPosRankSum,Number=1,Type=Float,Description="Z-score from Wilcoxon rank sum test of Alt vs. Ref read position bias">
         ##INFO=<ID=SOR,Number=1,Type=Float,Description="Symmetric Odds Ratio of 2x2 contingency table to detect strand bias">
         */
        Map<String, String> ac = new LinkedHashMap<>();
        ac.put("ID", "AC");
        ac.put("Number", "A");
        ac.put("Type", "Integer");
        ac.put("Description", "Allele count in genotypes, for each ALT allele, in the same order as listed");
        Map<String, String> af = new LinkedHashMap<>();
        af.put("ID", "AF");
        af.put("Number", "A");
        af.put("Type", "Float");
        af.put("Description", "Allele Frequency, for each ALT allele, in the same order as listed");
        Map<String, String> an = new LinkedHashMap<>();
        an.put("ID", "AN");
        an.put("Number", "1");
        an.put("Type", "Integer");
        an.put("Description", "Total number of alleles in called genotypes");
        Map<String, String> hs = new LinkedHashMap<>();
        hs.put("ID", "HaplotypeScore");
        hs.put("Number", "1");
        hs.put("Type", "Float");
        hs.put("Description", "Consistency of the site with at most two segregating haplotypes");
        Map<String, String> rp = new LinkedHashMap<>();
        rp.put("ID", "ReadPosRankSum");
        rp.put("Number", "1");
        rp.put("Type", "Float");
        rp.put("Description", "Z-score from Wilcoxon rank sum test of Alt vs. Ref read position bias");
        Map<String, String> sor = new LinkedHashMap<>();
        sor.put("ID", "SOR");
        sor.put("Number", "1");
        sor.put("Type", "Float");
        sor.put("Description", "Symmetric Odds Ratio of 2x2 contingency table to detect strand bias");
        List<Map<String, String>> expResult = new ArrayList<>();
        expResult.addAll(Arrays.asList(ac, af, an, hs, rp, sor));
        List<Map<String, String>> result = vcfFile.getInfos();
        assertEquals(expResult, result);
    }
}
