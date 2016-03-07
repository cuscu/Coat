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

import coat.core.vcf.VcfFile;

/**
 * Factory to create Variants. Use method <code>createVariant(line, file)</code> to get a new Variant. Line should be
 * a String corresponding to a VCF line in a text VCF file.
 *
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public class VariantFactory {

    /**
     * Generates a new Variant using line to populate.
     *
     * @param line a VCF line
     * @param file the owner VcfFile
     * @return a variant representing the line in the VCF file
     */
    public static Variant createVariant(String line, VcfFile file) {
        final String[] v = line.split("\t");
        final String chrom = v[0];
        final int pos = Integer.valueOf(v[1]);
        final String id = v[2];
        final String ref = v[3];
        final String alt = v[4];
        final String filter = v[6];
        double qual;
        try {
            qual = Double.valueOf(v[5]);
        } catch (Exception ignored) {
            qual = 0;
        }
        final Samples samples = getSamples(v);
        final Info info = getVariantInfo(v[7]);
        return new Variant(file, chrom, pos, id, ref, alt, qual, filter, info, samples);
    }

    private static Info getVariantInfo(String infoField) {
        final String[] fields = infoField.split(";");
        Info info = new Info();
        for (String field : fields) setInfo(info, field);
        return info;
    }

    private static void setInfo(Info info, String field) {
        if ((field.contains("="))) {
            final String[] pair = field.split("=");
            info.setInfo(pair[0], pair[1]);
        } else info.setInfo(field, true);
    }

    private static Samples getSamples(String[] v) {
        if (v.length > 8) {
            final String[] keys = v[8].split(":");
            final int numberOfSamples = v.length - 9;
            final Samples samples = new Samples(numberOfSamples);
            for (int i = 0; i < numberOfSamples; i++) {
                final String[] values = v[i + 9].split(":");
                for (int j = 0; j < values.length; j++) samples.setFormat(i, keys[j], values[j]);
            }
            return samples;
        }
        return new Samples(0);
    }

}
