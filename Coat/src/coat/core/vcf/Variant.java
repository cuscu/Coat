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

package coat.core.vcf;

import coat.utils.OS;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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


    private final List<String[]> samples = new ArrayList<>();
    private final VcfFile vcfFile;

    private final Map<String, String> info = new HashMap<>();

    private String temp = null;

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
        preloadInfos(v[7]);
        setFormats(vcfFile, v);
    }

    private void preloadInfos(String infoField) {
        final String symbol = guessInfo("SYMBOL", infoField);
        info.put("SYMBOL", symbol);
        final String cod = guessInfo("COD", infoField);
        info.put("COD", cod);
        final String sifts = guessInfo("SIFTs", infoField);
        info.put("SIFTs", sifts);
        final String cons = guessInfo("CONS", infoField);
        info.put("CONS", cons);
    }

    private String guessInfo(String key, String info) {
        final Pattern pattern = Pattern.compile(".*" + key + "=([^;]*)[;]?.*");
        final Matcher matcher = pattern.matcher(info);
        return matcher.matches() ? matcher.group(1) : null;
    }

    public void setInfo(Map<String, String> info) {
        temp = toString(info);
    }

    private void setFormats(VcfFile vcfFile, String[] v) {
        if (v.length > 8) {
            String[] formatIds = v[8].split(":");
            for (int i = 9; i < v.length; i++) {
                samples.add(new String[vcfFile.getHeader().getComplexHeaders().get("FORMAT").size()]);
                String[] sample = v[i].split(":");
                for (int j = 0; j < sample.length; j++)
                    setSampleValue(vcfFile.getHeader().getSamples().get(i - 9), formatIds[j], sample[j]);
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

    public String getTemp() {
        return temp;
    }

    public void setTemp(String temp) {
        this.temp = temp;
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

    /**
     * Returns a String array. Each element contains genotype info about one sample in the vcf. For
     * instance, if vcf contains variants of one sample, the size of the array will be 1. If 3
     * samples are stored in the file, the size will be 3.
     *
     * @return an array with the genotype info of each sample, or null if no genotype info in the
     * file.
     */
    public List<String[]> getSamples() {
        return samples;
    }

//    }

//    }

    public String toString(Map<String, String> info) {
        final String formats = getFormatString();
        final String inf = infosToString(info);
        return String.format(Locale.US, "%s\t%d\t%s\t%s\t%s\t%.4f\t%s\t%s%s",
                chrom, pos, id, ref, alt, qual, filter, inf, formats);
    }

    private String getFormatString() {
        if (vcfFile.getHeader().getSamples().isEmpty()) return "";
        StringBuilder builder = new StringBuilder();
        final List<String> formats = vcfFile.getHeader().getIdList("FORMAT");
        if (vcfFile.getHeader().getSamples().size() > 0) builder.append("\t").append(OS.asString(":", formats));
        for (int i = 0; i < vcfFile.getHeader().getSamples().size(); i++) {
            builder.append("\t");
            builder.append(OS.asString(":", Arrays.asList(samples.get(i))));
        }
        return builder.toString();
    }

    private String infosToString(Map<String, String> map) {
        final List<String> pairs = new ArrayList<>();
        map.forEach((key, value) -> {
            if (value.equals(TRUE)) pairs.add(key);
            else pairs.add(key + "=" + value);
        });
        Collections.sort(pairs);
        return OS.asString(";", pairs);

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

//    }

    public VcfFile getVcfFile() {
        return vcfFile;
    }

//    }

    public String getSampleValue(String name, String id) {
        final int sampleIndex = getSampleIndex(name);
        final int valueIndex = getFormatIndex(id);
        if (sampleIndex >= 0 && valueIndex >= 0) return samples.get(sampleIndex)[valueIndex];
        return null;
    }

    private int getFormatIndex(String id) {
        final List<Map<String, String>> format = vcfFile.getHeader().getComplexHeaders().get("FORMAT");
        for (int i = 0; i < format.size(); i++) if (format.get(i).get("ID").equals(id)) return i;
        return -1;
    }

    private int getSampleIndex(String name) {
        return vcfFile.getHeader().getSamples().indexOf(name);
    }

    public void setSampleValue(String name, String id, String value) {
        final int sampleIndex = getSampleIndex(name);
        final int valueIndex = getFormatIndex(id);
        if (sampleIndex >= 0 && valueIndex >= 0) samples.get(sampleIndex)[valueIndex] = value;
    }

    public void addSample(String name, String[] values) {
        if (!getVcfFile().getHeader().getSamples().contains(name)) {
            getVcfFile().getHeader().getSamples().add(name);
            getSamples().add(values);
        } else {
            int index = getVcfFile().getHeader().getSamples().indexOf(name);
            while (index >= samples.size()) samples.add(null);
            samples.set(index, values);
        }
    }

    public Map<String, String> getInfo() {
        String[] line = vcfFile.getLine(chrom, pos);
        if (line == null) System.err.println(chrom + ":" + pos + " not found");
        return line == null ? null : toInfoMap(line[7]);
    }

    public String getInfo(String key) {
        if (info.containsKey(key)) return info.get(key);
        else {
            loadInfo(key);
            return info.get(key);

        }
    }

    private void loadInfo(String key) {
        final Map<String, String> map = getInfo();
        try {
            final String value = map.get(key);
            info.putIfAbsent(key, value);
        } catch (Exception e) {
            System.err.println(key);
            e.printStackTrace();
        }
    }

    @NotNull
    private Map<String, String> toInfoMap(String s) {
        final Map<String, String> map = new HashMap<>();
        Arrays.stream(s.split(";")).forEach(i -> {
            if (i.contains("=")) {
                final String[] pair = i.split("=");
                map.put(pair[0], pair[1]);
            } else map.put(i, TRUE);
        });
        return map;
    }
}
