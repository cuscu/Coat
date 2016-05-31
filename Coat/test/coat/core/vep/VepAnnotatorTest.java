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

package coat.core.vep;

import org.junit.Assert;
import org.junit.Test;
import vcf.Variant;
import vcf.VcfFile;
import vcf.VcfFileFactory;

import java.io.File;

/**
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class VepAnnotatorTest {

    /*
     * 1	13273	rs531730856	G	C	124.7700	.	AC=1;MQRankSum=0.472;MQ=26.99;AF=0.500;MLEAC=1;BaseQRankSum=0.972;BIO=processed_transcript;GMAF=0.095;MLEAF=0.500;DP=26;ReadPosRankSum=-0.361;AN=2;FS=0.000;MQ0=0;CONS=non_coding_transcript_exon_variant,non_coding_transcript_variant;FEAT=ENST00000456328;QD=4.80;AMR_MAF=0.1455;SYMBOL=DDX11L1;GENE=ENSG00000223972;SOR=0.947;AFR_MAF=0.0204;ClippingRankSum=-0.972;EUR_MAF=0.1471	AD:DP:GQ:GT:PL	18,8:26:99:0/1:153,0,428
     * 1	13273	.	G	C	124.77	.	AC=1;AF=0.500;AN=2;BaseQRankSum=0.972;ClippingRankSum=-0.972;DP=26;FS=0.000;MLEAC=1;MLEAF=0.500;MQ=26.99;MQ0=0;MQRankSum=0.472;QD=4.80;ReadPosRankSum=-0.361;SOR=0.947	GT:AD:DP:GQ:PL	0/1:18,8:26:99:153,0,428

     */

    @Test
    public void test() {
//        final VcfFile vcfFile = VcfFileFactory.createFromFile(new File("test/coat/files/Sample1.vcf"));
//        final Variant variant = vcfFile.getVariants().get(0);
//        final VepAnnotator annotator = new VepAnnotator(vcfFile.getVariants().subList(0, 1));
//        try {
//            annotator.call();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        Assert.assertEquals("processed_transcript", variant.getInfo().get("BIO"));
//
    }
}
