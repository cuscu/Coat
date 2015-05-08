/*
 * Copyright (C) 2014 UICHUIMI
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package coat.vcf;

import coat.CoatView;
import coat.utils.OS;
import com.sun.istack.internal.Nullable;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

/**
 * Stores a variant. chrom, pos, ref, alt, filter and format are Strings. pos is an integer, qual a
 * double. Info is stored as a map of key==value. If value is null, key is treated as a flag.
 *
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class Variant implements Comparable<Variant> {

    private String chrom, ref, alt, info, format;
    private int pos;
    private double qual;
    private String[] samples;
    private Map<String, Object> infos = new TreeMap<>();
    private String id;

    private int chromIndex;

    /**
     * Parses the VCF line and creates a Variant.
     *
     * @param line the line to parse
     */
    public Variant(String line) {
        final String[] v = line.split("\t");
        chrom = v[0];
        chromIndex = OS.getStandardChromosomes().indexOf(chrom);
        pos = Integer.valueOf(v[1]);
        id = v[2];
        ref = v[3];
        alt = v[4];
        try {
            qual = Double.valueOf(v[5]);
        } catch (Exception e) {
            CoatView.printMessage("Quality " + v[5] + " is not a valid number", "error");
        }
        infos.put("FILTER", v[6]);
        info = v[7];
        // Split by ;
        Arrays.stream(info.split(";")).forEach(i -> {
            String[] pair = i.split("=");
            String key = pair[0];
            // If it is a flag, value is null
            String value = pair.length > 1 ? pair[1] : null;
            infos.put(key, value);
        });
        if (v.length > 8) {
            format = v[8];
            final int nSamples = v.length - 9;
            samples = new String[nSamples];
            System.arraycopy(v, 9, samples, 0, nSamples);
        } else {
            format = null;
            samples = null;
        }
    }

    public Variant(String chrom, int position, String ref, String alt, String id) {
        this.chrom = chrom;
        this.pos = position;
        this.ref = ref;
        this.alt = alt;
        this.id = id;
        chromIndex = OS.getStandardChromosomes().indexOf(chrom);
    }

    /**
     * Gets the chromosome of the variant.
     *
     * @return the chromosome of the variant
     */
    public String getChrom() {
        return chrom;
    }

    /**
     * Gets the ID of the variant.
     *
     * @return the ID of the variant
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the REF value of the variant.
     *
     * @return the ref value
     */
    public String getRef() {
        return ref;
    }

    /**
     * Gets the ALT value of the variant.
     *
     * @return the alt value
     */
    public String getAlt() {
        return alt;
    }

    /**
     * Gets the INFO value of the variant.
     *
     * @return the info value
     */
    public String getInfo() {
        return info;
    }

    /**
     * Gets the FORMAT value of the variant.
     *
     * @return the format value
     */
    public String getFormat() {
        return format;
    }

    /**
     * Gets the position of the variant.
     *
     * @return the position
     */
    public int getPos() {
        return pos;
    }

    /**
     * Gets the QUAL of the variant.
     *
     * @return the quality
     */
    public double getQual() {
        return qual;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setQual(double qual) {
        this.qual = qual;
    }

    /**
     * Returns a String array. Each element contains genotype info about one sample in the vcf. For
     * instance, if vcf contains variants of one sample, the size of the array will be 1. If 3
     * samples are stored in the file, the size will be 3.
     *
     * @return an array with the genotype info of each sample, or null if no genotype info in the
     * file.
     */
    public String[] getSamples() {
        return samples;
    }

    public Map<String, Object> getInfos() {
        return infos;
    }

    @Override
    public String toString() {
        String formats = "";
        if (format != null)
            formats = "\t" + format + OS.asString("\t", samples);
        String inf = "";
        for (Map.Entry<String, Object> entry : infos.entrySet()) {
            String key = entry.getKey();
            if (key.equals("FILTER"))
                continue;
            String value = String.valueOf(entry.getValue());
            if (value == null)
                inf += key + ";";
            else
                inf += key + "=" + value + ";";
        }
        // Remove last comma
        inf = inf.substring(0, inf.length() - 1);
        return String.format(Locale.US, "%s\t%d\t%s\t%s\t%s\t%.4f\t%s\t%s%s",
                chrom, pos, id, ref, alt, qual, infos.get("FILTER"), inf, formats);
    }

    @Override
    public int compareTo(@Nullable Variant variant) {
        // Variants with no standard chromosome goes to the end
        if (chromIndex != -1 && variant.chromIndex == -1) return -1;
        if (chromIndex == -1 && variant.chromIndex != -1) return 1;
        // Non-standard chromosomes are ordered alphabetically
        int compare = (chromIndex == -1)
                ? chrom.compareTo(variant.chrom)
                : Integer.compare(chromIndex, variant.chromIndex);
        if (compare != 0) return compare;
        return Integer.compare(pos, variant.pos);
    }
}
