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

package coat.core.vcf.combine;

import coat.core.variant.Variant;
import coat.core.vcf.VcfFile;
import coat.core.vcf.VcfHeader;
import coat.view.vcfreader.VcfSample;

import java.io.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class VariantStream {

    private final VcfSample vcfSample;
    private BufferedReader reader;
    private BufferedReader mistReader;
    private Variant variant;
    private String[] mistRegion;
    private VcfFile vcfFile;

    public VariantStream(VcfSample vcfSample) {
        this.vcfSample = vcfSample;
        loadHeaders();
        openReaders();
        loadFirstVariant();
        loadFirstMistRegion();
    }

    private void loadHeaders() {
        final VcfHeader vcfHeader = loadHeader(vcfSample.getVcfFile());
        vcfHeader.addHeader("##INFO=<ID=MistZone,Type=Flag,Description=\"If present, indicates that the position is in an MIST Zone\">");
        this.vcfFile = new VcfFile(vcfHeader);
    }

    private void openReaders() {
        try {
            reader = new BufferedReader(new FileReader(vcfSample.getVcfFile()));
            if (vcfSample.getMistFile() != null)
                mistReader = new BufferedReader(new FileReader(vcfSample.getMistFile()));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static VcfHeader loadHeader(File reference) {
        final VcfHeader header = new VcfHeader();
        try (BufferedReader reader = new BufferedReader(new FileReader(reference))) {
            reader.lines().filter(line -> line.startsWith("#")).forEach(header::addHeader);
            return header;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return header;
    }

    private void loadFirstVariant() {
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                if (!line.startsWith("#")) {
                    variant = new Variant(line, vcfFile);
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadFirstMistRegion() {
        if (mistReader != null)
            try {
                String line = mistReader.readLine();
                while (line != null && (line.startsWith("#") || line.startsWith("chrom"))) line = mistReader.readLine();
                mistRegion = line == null ? null : line.split("\t");
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    public VcfSample getVcfSample() {
        return vcfSample;
    }

    public List<Variant> getVariants() {
        try (BufferedReader reader = new BufferedReader(new FileReader(vcfSample.getVcfFile()))) {
            return reader.lines().filter(line -> !line.startsWith("#")).map(s -> new Variant(s, vcfFile)).collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean filter(Variant variant) {
        Variant currentVariant = this.variant;
        while (currentVariant != null) {
            final int compareTo = currentVariant.compareTo(variant);
            // Current variant is lower
            if (compareTo < 0) currentVariant = nextVariant();
                // The variant is present
            else if (compareTo == 0) return checkZygotic(currentVariant, variant);
                // Current variant is higher
            else break;
        }
        // The variant is not present
        if (inMist(variant) && vcfSample.getLevel() != VcfSample.Level.UNAFFECTED) {
            addMistInfo(variant);
            return true;
        }
        return vcfSample.getLevel() == VcfSample.Level.UNAFFECTED;
    }

    private void addMistInfo(Variant variant) {
        variant.setInfo("MistZone", Variant.TRUE);
    }

    private String[] nextMistRegion() {
        try {
            String line = mistReader.readLine(); // Skip header
            return line == null ? null : line.split("\t");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private boolean inMist(Variant variant) {
        while (mistRegion != null) {
            final int start = Integer.valueOf(mistRegion[3]);
            final int end = Integer.valueOf(mistRegion[4]);
            if (start <= variant.getPos() && variant.getPos() < end) return true;
            else if (start > variant.getPos()) return false;
            else mistRegion = nextMistRegion();
        }
        return false;
    }

    private boolean checkZygotic(Variant localVariant, Variant variant) {
        if (vcfSample.getLevel() == VcfSample.Level.UNAFFECTED) return false;
        mergeInfo(localVariant, variant);
        return true;
    }

    private void mergeInfo(Variant localVariant, Variant variant) {
//        for (int i = 0; i < localVariant.getSamples().size(); i++) {
//            final String[] values = localVariant.getSamples().get(i);
//            final String name = vcfFile.getHeader().getSamples().get(i);
//            variant.addSample(name, values);
//        }
    }


    private Variant nextVariant() {
        if (variant == null) return null;
        try {
            final Variant v = variant;
            final String line = reader.readLine();
            variant = (line == null) ? null : new Variant(line, vcfFile);
            return v;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public VcfFile getVcfFile() {
        return vcfFile;
    }
}
