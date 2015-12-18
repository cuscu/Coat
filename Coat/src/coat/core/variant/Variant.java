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

package coat.core.variant;

import coat.core.Dictionary;
import coat.core.vcf.VcfFile;
import coat.utils.OS;

import java.util.*;

/**
 * Stores a variant. chrom, pos, ref, alt, filter and format are Strings. pos is an integer, qual a
 * double. Info is stored as a map of key==value. If value is null, key is treated as a flag.
 *
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class Variant implements Comparable<Variant> {

    private static final String TRUE = OS.getString("true");
    private String chrom, ref, alt, filter;
    private int pos;
    private double qual;
    private String id;

    private static final Dictionary dictionary = new Dictionary();

    private final Map<Integer, Integer> values = new TreeMap<>();
    private Map<Integer, Integer>[] formats;

    private final VcfFile vcfFile;
    private int chromIndex;

    /**
     * Parses the VCF line and creates a Variant.
     *
     * @param line    the line to parse
     * @param vcfFile the owner VcfFile
     */
    public Variant(String line, VcfFile vcfFile) {
        this.vcfFile = vcfFile;
        final String[] v = line.split("\t");
        chrom = v[0];
        chromIndex = OS.getStandardChromosomes().indexOf(chrom);
        pos = Integer.valueOf(v[1]);
        id = v[2];
        ref = v[3];
        alt = v[4];
        try {
            qual = Double.valueOf(v[5]);
        } catch (Exception ignored) {
        }
        filter = v[6];
        setInfos(v[7]);
        setFormats(v);
    }

    private void setInfos(String infoField) {
        final String[] fields = infoField.split(";");
        for (String field : fields) {
            if ((field.contains("="))) {
                final String[] pair = field.split("=");
                final int key = vcfFile.getHeader().addInfo(pair[0]);
                final int index = dictionary.addWord(pair[1]);
                values.put(key, index);
            } else {
                final int key = vcfFile.getHeader().addInfo(field);
                final int index = dictionary.addWord(TRUE);
                values.put(key, index);
            }
        }
    }

    private void setFormats(String[] v) {
        if (v.length > 8) {
            final String[] formatIds = v[8].split(":");
            for (String format : formatIds) vcfFile.getHeader().addFormat(format);
            formats = new Map[v.length - 9];
            for (int i = 9; i < v.length; i++) {
                final int sampleIndex = i - 9;
                String[] sample = v[i].split(":");
                formats[sampleIndex] = new HashMap<>();
                for (int j = 0; j < sample.length; j++) {
                    final int formatIndex = vcfFile.getHeader().getFormatIndex(formatIds[j]);
                    final int valueIndex = dictionary.addWord(sample[j]);
                    formats[sampleIndex].put(formatIndex, valueIndex);
                }
            }
        }
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

    @Override
    public int compareTo(Variant variant) {
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

    public String getFilter() {
        return filter;
    }

    public VcfFile getVcfFile() {
        return vcfFile;
    }

    public Object getInfo(String key) {
        final int index = vcfFile.getHeader().getInfoIndex(key);
        final Integer valueIndex = values.get(index);
        if (valueIndex == null) return null;
        return dictionary.getWord(valueIndex);
    }

    public Object getFormat(String sample, String key) {
        final int sampleIndex = vcfFile.getHeader().getSampleIndex(sample);
        final int formatIndex = vcfFile.getHeader().getFormatIndex(key);
        if (sampleIndex == -1 || formatIndex == -1) return null;
        final int valueIndex = formats[sampleIndex].get(formatIndex);
        if (valueIndex == -1) return true;
        return dictionary.getWord(valueIndex);
    }

    @Override
    public String toString() {
        return chrom +
                "\t" + pos +
                "\t" + id +
                "\t" + ref +
                "\t" + alt +
                "\t" + qual +
                "\t" + filter +
                "\t" + getInfoString() +
                getFormatString();
    }

    private String getInfoString() {
        final List<String> infos = new ArrayList<>();
        values.forEach((infoIndex, valueIndex) -> {
            final String key = vcfFile.getHeader().getInfo(infoIndex);
            final String value = dictionary.getWord(valueIndex);
            if (value != null) {
                if (value.equals(TRUE)) infos.add(key);
                else infos.add(key + "=" + value);
            }
        });
        Collections.sort(infos);
        return OS.asString(";", infos);
    }

    private String getFormatString() {
        if (formats.length == 0) return "";
        final StringBuilder builder = new StringBuilder();
        final List<String> fId = vcfFile.getHeader().getFormats();
        fId.sort((key1, key2) -> Integer.compare(vcfFile.getHeader().getFormatIndex(key1), vcfFile.getHeader().getFormatIndex(key2)));
        builder.append("\t").append(OS.asString(":", fId));
        for (Map<Integer, Integer> format : formats) {
            final List<String> vals = new ArrayList<>();
            format.forEach((fIndex, valIndex) -> vals.add(fIndex, dictionary.getWord(valIndex)));
            builder.append("\t").append(OS.asString(":", vals));
        }
        return builder.toString();
    }

    public void setInfo(String key, String value) {
        final int index = vcfFile.getHeader().getInfoIndex(key);
        if (index >= 0) {
            final int valueIndex = dictionary.addWord(value);
            values.put(index, valueIndex);
        }
    }
}
