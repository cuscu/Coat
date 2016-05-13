/*
 * Copyright (c) UICHUIMI 2016
 *
 * This file is part of Coat.
 *
 * Coat is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * Coat is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Foobar.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package coat.core.variant;

import org.junit.Assert;
import org.junit.Test;
import vcf.Variant;
import vcf.VcfFile;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Lorente-Arencibia Pascual (pasculorente@gmail.com)
 */
public class VariantTest {

    private final VcfFile file = new VcfFile(new File("test/coat/files/Sample1.vcf"));
    /*
     * 1	13273	.	G	C	124.77	.	AC=1;AF=0.500;AN=2;BaseQRankSum=0.972;ClippingRankSum=-0.972;DP=26;FS=0.000;MLEAC=1;MLEAF=0.500;MQ=26.99;MQ0=0;MQRankSum=0.472;QD=4.80;ReadPosRankSum=-0.361;SOR=0.947	GT:AD:DP:GQ:PL	0/1:18,8:26:99:153,0,428
     * 1	69511	rs75062661	A	G	1592.77	.	AC=2;AF=1.00;AN=2;DB;DP=60;FS=0.000;MLEAC=2;MLEAF=1.00;MQ=31.44;MQ0=0;QD=26.55;SOR=1.316	GT:AD:DP:GQ:PL	1/1:0,59:59:99:1621,176,0
     * 1	133160	.	G	A	118.77	.	AC=1;AF=0.500;AN=2;BaseQRankSum=-1.221;ClippingRankSum=-0.322;DP=8;FS=0.000;MLEAC=1;MLEAF=0.500;MQ=35.55;MQ0=0;MQRankSum=-0.956;QD=14.85;ReadPosRankSum=0.322;SOR=1.329	GT:AD:DP:GQ:PL	0/1:3,5:8:89:147,0,89
     * 1	139213	.	A	G	67.77	.	AC=1;AF=0.500;AN=2;BaseQRankSum=0.296;ClippingRankSum=0.895;DP=10;FS=0.000;MLEAC=1;MLEAF=0.500;MQ=31.98;MQ0=0;MQRankSum=0.296;QD=6.78;ReadPosRankSum=0.296;SOR=0.223	GT:AD:DP:GQ:PL	0/1:7,3:10:96:96,0,334
     * 1	139233	.	C	A	69.77	.	AC=1;AF=0.500;AN=2;BaseQRankSum=1.683;ClippingRankSum=0.248;DP=9;FS=4.771;MLEAC=1;MLEAF=0.500;MQ=34.96;MQ0=0;MQRankSum=0.248;QD=7.75;ReadPosRankSum=1.001;SOR=0.045	GT:AD:DP:GQ:PL	0/1:6,3:9:98:98,0,308
     * 1	651149	.	C	T	40.74	.	AC=2;AF=1.00;AN=2;DP=2;FS=0.000;MLEAC=2;MLEAF=1.00;MQ=29.00;MQ0=0;QD=20.37;SOR=2.303	GT:AD:DP:GQ:PL	1/1:0,2:2:6:68,6,0
     * 1	715348	rs3131984	T	G	85.28	.	AC=2;AF=1.00;AN=2;DB;DP=3;FS=0.000;MLEAC=2;MLEAF=1.00;MQ=51.62;MQ0=0;QD=28.43;SOR=2.833	GT:AD:DP:GQ:PL	1/1:0,3:3:9:113,9,0
     * 1	752566	rs3094315	G	A	190.84	.	AC=2;AF=1.00;AN=2;DB;DP=6;FS=0.000;MLEAC=2;MLEAF=1.00;MQ=49.84;MQ0=0;QD=31.81;SOR=3.912	GT:AD:DP:GQ:PL	1/1:0,6:6:18:219,18,0
     * 1	752721	rs3131972	A	G	1228.77	.	AC=2;AF=1.00;AN=2;DB;DP=38;FS=0.000;MLEAC=2;MLEAF=1.00;MQ=49.11;MQ0=0;QD=32.34;SOR=1.670	GT:AD:DP:GQ:PL	1/1:0,38:38:99:1257,114,0
     * 1	752894	rs3131971	T	C	440.77	.	AC=2;AF=1.00;AN=2;DB;DP=15;FS=0.000;MLEAC=2;MLEAF=1.00;MQ=32.92;MQ0=0;QD=29.38;SOR=5.549	GT:AD:DP:GQ:PL	1/1:0,15:15:45:469,45,0
     * 1	754182	rs3131969	A	G	62.74	.	AC=2;AF=1.00;AN=2;DB;DP=2;FS=0.000;MLEAC=2;MLEAF=1.00;MQ=60.00;MQ0=0;QD=31.37;SOR=0.693	GT:AD:DP:GQ:PL	1/1:0,2:2:6:90,6,0
     * 1	754192	rs3131968	A	G	62.74	.	AC=2;AF=1.00;AN=2;DB;DP=2;FS=0.000;MLEAC=2;MLEAF=1.00;MQ=60.00;MQ0=0;QD=31.37;SOR=0.693	GT:AD:DP:GQ:PL	1/1:0,2:2:6:90,6,0
     * 7	150968234	.	C	T	58.28	.	AC=2;AF=1.00;AN=2;DP=3;FS=0.000;MLEAC=2;MLEAF=1.00;MQ=60.00;MQ0=0;QD=19.43;SOR=1.179	GT:AD:DP:GQ:PL	1/1:0,3:3:9:86,9,0
     * 7	150972189	.	G	A	667.77	.	AC=1;AF=0.500;AN=2;BaseQRankSum=0.146;ClippingRankSum=0.851;DP=59;FS=4.409;MLEAC=1;MLEAF=0.500;MQ=60.00;MQ0=0;MQRankSum=0.468;QD=11.32;ReadPosRankSum=1.035;SOR=0.284	GT:AD:DP:GQ:PL	0/1:34,25:59:99:696,0,1021
     * 7	150979714	.	T	A	75.78	.	AC=1;AF=0.500;AN=2;BaseQRankSum=0.727;ClippingRankSum=0.727;DP=4;FS=0.000;MLEAC=1;MLEAF=0.500;MQ=60.00;MQ0=0;MQRankSum=0.727;QD=18.95;ReadPosRankSum=0.727;SOR=1.609	GT:AD:DP:GQ:PL	0/1:1,3:4:25:104,0,25
     */

    @Test
    public void testVcfFile() {
        for (Variant variant : file.getVariants()) Assert.assertEquals(file, variant.getVcfFile());
    }

    @Test
    public void testChrom() {
        final String[] chroms = {"1", "1", "1", "1", "1", "1", "1", "1", "1", "1", "1", "1", "7", "7", "7"};
        for (int i = 0; i < chroms.length; i++) {
            Assert.assertEquals(chroms[i], file.getVariants().get(i).getChrom());
        }
    }

    @Test
    public void testPos() {
        final int[] pos = {13273, 69511, 133160, 139213, 139233, 651149, 715348, 752566, 752721, 752894, 754182, 754192, 150968234, 150972189, 150979714};
        for (int i = 0; i < pos.length; i++) {
            Assert.assertEquals(pos[i], file.getVariants().get(i).getPos());
        }
    }

    @Test
    public void testId() {
        final String[] values = {".", "rs75062661", ".", ".", ".", ".", "rs3131984", "rs3094315", "rs3131972", "rs3131971", "rs3131969", "rs3131968", ".", ".", "."};
        for (int i = 0; i < values.length; i++) {
            Assert.assertEquals(values[i], file.getVariants().get(i).getId());
        }
    }

    @Test
    public void testRef() {
        final String[] values = {"G", "A", "G", "A", "C", "C", "T", "G", "A", "T", "A", "A", "C", "G", "T"};
        for (int i = 0; i < values.length; i++) {
            Assert.assertEquals(values[i], file.getVariants().get(i).getRef());
        }
    }

    @Test
    public void testAlt() {
        final String[] values = {"C", "G", "A", "G", "A", "T", "G", "A", "G", "C", "G", "G", "T", "A", "A"};
        for (int i = 0; i < values.length; i++) {
            Assert.assertEquals(values[i], file.getVariants().get(i).getAlt());
        }
    }

    @Test
    public void testQual() {
        final double[] pos = {124.77, 1592.77, 118.77, 67.77, 69.77, 40.74, 85.28, 190.84, 1228.77, 440.77, 62.74, 62.74, 58.28, 667.77, 75.78};
        for (int i = 0; i < pos.length; i++) {
            Assert.assertEquals(pos[i], file.getVariants().get(i).getQual(), 0.00001);
        }
    }

    @Test
    public void testFilter() {
        final String[] values = {".", ".", ".", ".", ".", ".", ".", ".", ".", "PASS", ".", ".", ".", ".", "."};
        for (int i = 0; i < values.length; i++) {
            Assert.assertEquals(values[i], file.getVariants().get(i).getFilter());
        }
    }

    @Test
    public void testInfo() {
        final Map<String, Object[]> values = new HashMap<>();
        values.put("AC", new String[]{"1", "2", "1", "1", "1", "2", "2", "2", "2", "2", "2", "2", "2", "1", "1"});
        values.put("AF", new String[]{"0.500", "1.00", "0.500", "0.500", "0.500", "1.00", "1.00", "1.00", "1.00", "1.00", "1.00", "1.00", "1.00", "0.500", "0.500"});
        values.put("AN", new String[]{"2", "2", "2", "2", "2", "2", "2", "2", "2", "2", "2", "2", "2", "2", "2"});
        values.put("DB", new Boolean[]{null, true, null, null, null, null, true, true, true, true, true, true, null, null, null});
        for (Map.Entry<String, Object[]> entry : values.entrySet()) {
            for (int i = 0; i < entry.getValue().length; i++) {
                Assert.assertEquals(entry.getValue()[i], file.getVariants().get(i).getInfo().getInfo(entry.getKey()));
            }
        }
    }

    @Test
    public void testFormat() {
        final Map<String, Object[]> values = new HashMap<>();
        values.put("GT", new String[]{"0/1", "1/1", "0/1", "0/1", "0/1", "1/1", "1/1", "1/1", "1/1", "1/1", "1/1", "1/1", "1/1", "0/1", "0/1"});
        values.put("AD", new String[]{"18,8", "0,59", "3,5", "7,3", "6,3", "0,2", "0,3", "0,6", "0,38", "0,15", "0,2", "0,2", "0,3", "34,25", "1,3"});
        values.put("DP", new String[]{"26", "59", "8", "10", "9", "2", "3", "6", "38", "15", "2", "2", "3", "59", "4"});
        values.put("GQ", new String[]{"99", "99", "89", "96", "98", "6", "9", "18", "99", "45", "6", "6", "9", "99", "25"});
        values.put("PL", new String[]{"153,0,428", "1621,176,0", "147,0,89", "96,0,334", "98,0,308", "68,6,0", "113,9,0", "219,18,0", "1257,114,0", "469,45,0", "90,6,0", "90,6,0", "86,9,0", "696,0,1021", "104,0,25"});
        for (Map.Entry<String, Object[]> entry : values.entrySet()) {
            for (int i = 0; i < entry.getValue().length; i++) {
                Assert.assertEquals(entry.getValue()[i], file.getVariants().get(i).getSamples().getFormat(file.getHeader().getSampleIndex("sample01"), entry.getKey()));
            }
        }
    }

    @Test
    public void testSetId() {
        // Given
        Variant variant = file.getVariants().get(0);
        // When
        variant.setId("rs00002");
        // Then
        Assert.assertEquals("rs00002", variant.getId());
    }

    @Test
    public void testSetQual() {
        // Given
        Variant variant = file.getVariants().get(0);
        // When
        variant.setQual(123.45);
        // Then
        Assert.assertEquals(123.45, variant.getQual(), 0.001);
    }

    @Test
    public void testCompare() {
        for (int i = 0; i < file.getVariants().size() - 1; i++) {
            Assert.assertEquals(-1, file.getVariants().get(i).compareTo(file.getVariants().get(i + 1)));
        }
    }

    @Test
    public void testAddInfo() {
        final Variant variant = file.getVariants().get(0);
        variant.getInfo().setInfo("DP", "23");
        Assert.assertEquals("23", variant.getInfo().getInfo("DP"));
    }

}