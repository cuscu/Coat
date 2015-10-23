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

package coat.view.vcfcombiner;

import coat.core.vcfreader.Variant;
import coat.view.vcfreader.VcfSample;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
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

    public VariantStream(VcfSample vcfSample) {
        this.vcfSample = vcfSample;
        try {
            reader = new BufferedReader(new FileReader(vcfSample.getVcfFile()));
            if (vcfSample.getMistFile() != null)
                mistReader = new BufferedReader(new FileReader(vcfSample.getMistFile()));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        loadFirstVariant();
        loadFirstMistRegion();
    }

    private void loadFirstVariant() {
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                if (!line.startsWith("#")) {
                    variant = new Variant(line);
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadFirstMistRegion() {
        try {
            mistReader.readLine();
            String line = mistReader.readLine();
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
            return reader.lines().filter(line -> !line.startsWith("#")).map(Variant::new).collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean filter(Variant variant) {
        Variant currentVariant = this.variant;
        while (currentVariant != null) {
            final int compareTo = currentVariant.compareTo(variant);
            // The variant is present
            if (compareTo == 0) return checkZygotic(currentVariant);
                // Current variant is lower
            else if (compareTo < 0) currentVariant = nextVariant();
                // Current variant is higher
            else break;
        }
        // The variant is not present
        if (inMist(variant)) {
            variant.getInfos().put("MistZone", true);
            return true;
        }
        return vcfSample.getLevel() == VcfSample.Level.UNAFFECTED;
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

    private boolean checkZygotic(Variant variant) {
        if (vcfSample.getLevel() == VcfSample.Level.UNAFFECTED) return false;
        if (vcfSample.getLevel() == VcfSample.Level.AFFECTED) return true;
        final String AF = (String) variant.getInfos().get("AF");
        if (AF.equals("0.500")) return vcfSample.getLevel() == VcfSample.Level.HETEROZYGOUS;
        else return vcfSample.getLevel() == VcfSample.Level.HOMOZYGOUS;
    }


    private Variant nextVariant() {
        if (variant == null) return null;
        try {
            final Variant v = variant;
            final String line = reader.readLine();
            variant = (line == null) ? null : new Variant(line);
            return v;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
