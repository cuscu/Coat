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
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import vcf.VcfFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public class VcfFileTest {

    @Rule
    public final TemporaryFolder temporaryFolder = new TemporaryFolder();

    private final static File TEMPORARY_FILES = new File("test/coat/files");

    @Test
    public void size() {
        // Given
        final File file = new File(TEMPORARY_FILES, "Sample1.vcf");
        // When
        final VcfFile vcfFile = new VcfFile(file);
        // Then
        Assert.assertEquals(15, vcfFile.getVariants().size());
        Assert.assertEquals(file, vcfFile.getFile());
    }

    @Test
    public void testWithOneSample() {
        // Given
        final VcfFile vcfFile = new VcfFile(new File(TEMPORARY_FILES, "Sample1.vcf"));
        final List<String> expected = new ArrayList<>(Collections.singletonList("sample01"));
        final List<String> samples = vcfFile.getHeader().getSamples();
        // Then
        Assert.assertEquals(expected, samples);
    }

    @Test
    public void testWithNoSample() {
        // Given
        final VcfFile vcfFile = new VcfFile(new File(TEMPORARY_FILES, "NoSample.vcf"));
        final List<String> expected = new ArrayList<>();
        final List<String> samples = vcfFile.getHeader().getSamples();
        // Then
        Assert.assertEquals(expected, samples);
    }

    @Test
    public void testWithMultipleSample() {
        // Given
        final VcfFile vcfFile = new VcfFile(new File(TEMPORARY_FILES, "MultiSample.vcf"));
        final List<String> expected = Arrays.asList("S_7", "S_75", "S_42", "S_81", "S_8", "S_76", "S_53", "S_82", "S_30", "S_77", "S_70", "S_83", "S_36", "S_78", "S_71", "S_84", "S_37", "S_79", "S_72", "S_85", "S_39", "S_80", "S_73", "S_86", "S_87", "S_99", "S_93", "S_110", "S_88", "S_100", "S_94", "S_111", "S_89", "S_102", "S_95", "S_120", "S_90", "S_103", "S_96", "S_185", "S_91", "S_104", "S_97", "PM", "S_92", "S_105", "S_98", "DAM");
        final List<String> samples = vcfFile.getHeader().getSamples();
        // Then
        Assert.assertEquals(expected, samples);
    }

    @Test
    public void testComplexHeader() {
        final VcfFile vcfFile = new VcfFile(new File(TEMPORARY_FILES, "HeaderTest.vcf"));
        final Map<String, List<Map<String, String>>> expected = new HashMap<>();
        final Map<String, String> filter1 = new HashMap<>();
        filter1.put("Description", "Low quality");
        filter1.put("ID", "LowQual");
        final Map<String, String> info1 = new HashMap<>();
        info1.put("Description", "If present, indicates that the position is in an MIST Zone");
        info1.put("ID", "MistZone");
        info1.put("Type", "Flag");
        final Map<String, String> info2 = new HashMap<>();
        info2.put("Description", "Allele Frequency, for each ALT allele, in the same order as listed");
        info2.put("ID", "AF");
        info2.put("Type", "Float");
        info2.put("Number", "A");
        final Map<String, String> contig1 = new HashMap<>();
        contig1.put("assembly", "b37");
        contig1.put("ID", "1");
        contig1.put("length", "249250621");
        final Map<String, String> contig2 = new HashMap<>();
        contig2.put("assembly", "b37");
        contig2.put("ID", "2");
        contig2.put("length", "243199373");
        expected.put("FILTER", Arrays.asList(filter1));
        expected.put("INFO", Arrays.asList(info1, info2));
        expected.put("contig", Arrays.asList(contig1, contig2));
        /*
         * ##fileformat=VCFv4.1
         * ##reference=file:///home/unidad03/DNA_Sequencing/HomoSapiensGRCh37/human_g1k_v37.fasta
         * ##FILTER=<Description="Low quality",ID=LowQual>
         * ##INFO=<Description="If present, indicates that the position is in an MIST Zone",ID=MistZone,Type=Flag>
         * ##INFO=<Description="Allele Frequency, for each ALT allele, in the same order as listed",ID=AF,Number=A,Type=Float>
         * ##contig=<ID=1,assembly=b37,length=249250621>
         * ##contig=<ID=2,assembly=b37,length=243199373>
         */
        final Map<String, List<Map<String, String>>> complexHeaders = vcfFile.getHeader().getComplexHeaders();
        Assert.assertEquals(expected, complexHeaders);
    }

    @Test
    public void testSimpleHeader() {
        /*
         * ##fileformat=VCFv4.1
         * ##reference=file:///home/unidad03/DNA_Sequencing/HomoSapiensGRCh37/human_g1k_v37.fasta
         */
        final VcfFile vcfFile = new VcfFile(new File(TEMPORARY_FILES, "HeaderTest.vcf"));
        final Map<String, String> expected = new HashMap<>();
        expected.put("fileformat", "VCFv4.1");
        expected.put("reference", "file:///home/unidad03/DNA_Sequencing/HomoSapiensGRCh37/human_g1k_v37.fasta");
        final Map<String, String> simpleHeaders = vcfFile.getHeader().getSimpleHeaders();
        Assert.assertEquals(expected, simpleHeaders);
    }

    @Test
    public void testSaveFile() {
        final VcfFile vcfFile = new VcfFile(new File(TEMPORARY_FILES, "Sample1.vcf"));
        final File expected = new File(TEMPORARY_FILES, "ExpectedSample1.vcf");
        final File saveFile;
        try {
            saveFile = temporaryFolder.newFile("saveFile.vcf");
            vcfFile.save(saveFile);
            Assert.assertTrue(filesAreEqual(expected, saveFile));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testFormats() {
        final VcfFile vcfFile = new VcfFile(new File(TEMPORARY_FILES, "Sample1.vcf"));
        Assert.assertEquals(Arrays.asList("AD", "DP", "GQ", "GT", "PL"), vcfFile.getHeader().getIdList("FORMAT"));
    }

    private boolean filesAreEqual(File expected, File file) {
        try (BufferedReader expectedReader = new BufferedReader(new FileReader(expected));
             BufferedReader fileReader = new BufferedReader(new FileReader(file))) {
            String expectedLine;
            String fileLine;
            int lineNumber = 0;
            while ((expectedLine = expectedReader.readLine()) != null) {
                fileLine = fileReader.readLine();
                if (lineIsNull(expectedLine, fileLine, lineNumber)) return false;
                if (lineIsDifferent(expectedLine, fileLine, lineNumber)) return false;
                lineNumber++;
            }
            fileLine = fileReader.readLine();
            if (fileLine != null) {
                System.err.println("At line " + lineNumber);
                System.err.println("No more lines expected");

            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean lineIsDifferent(String expectedLine, String fileLine, int lineNumber) {
        if (!fileLine.equals(expectedLine)) {
            printError(expectedLine, fileLine, lineNumber);
            return true;
        }
        return false;
    }

    private boolean lineIsNull(String expectedLine, String fileLine, int lineNumber) {
        if (fileLine == null) {
            printError(expectedLine, null, lineNumber);
            return true;
        }
        return false;
    }

    private void printError(String expectedLine, String fileLine, int lineNumber) {
        System.err.println("At line " + lineNumber);
        System.err.println("Expected: " + expectedLine);
        System.err.println("Current : " + fileLine);
    }

}
