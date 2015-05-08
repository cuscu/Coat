/*
 * Copyright (C) 2015 UICHUIMI
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

import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Computes the Low Frequency Synonym Change. A tag that indicates the proportion between the
 * reference codon frequency and the alternative codon frequency in cells.
 *
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class LFS {

    /**
     * The list of frequencies. Each codon is given a real number between 0.0 and 1.0.
     */
    private static final Map<String, Double> frequencies = new TreeMap();
    /**
     * The list of aminoacids. Gives the corresponding aminoacid of a codon. Use this list to know
     * which codons are synonym.
     */
    private static final Map<String, String> aminoacids = new TreeMap();

    static {
        frequencies.put("TTT", 0.45);
        frequencies.put("TTC", 0.55);
        frequencies.put("TTA", 0.07);
        frequencies.put("TTG", 0.13);
        frequencies.put("TCT", 0.18);
        frequencies.put("TCC", 0.22);
        frequencies.put("TCA", 0.15);
        frequencies.put("TCG", 0.06);
        frequencies.put("TAT", 0.43);
        frequencies.put("TAC", 0.57);
        frequencies.put("TAA", 0.28);
        frequencies.put("TAG", 0.20);
        frequencies.put("TGT", 0.45);
        frequencies.put("TGC", 0.55);
        frequencies.put("TGA", 0.52);
        frequencies.put("TGG", 1.00);
        frequencies.put("CTT", 0.13);
        frequencies.put("CTC", 0.20);
        frequencies.put("CTA", 0.07);
        frequencies.put("CTG", 0.41);
        frequencies.put("CCT", 0.28);
        frequencies.put("CCC", 0.33);
        frequencies.put("CCA", 0.27);
        frequencies.put("CCG", 0.11);
        frequencies.put("CAT", 0.41);
        frequencies.put("CAC", 0.59);
        frequencies.put("CAA", 0.25);
        frequencies.put("CAG", 0.75);
        frequencies.put("CGT", 0.08);
        frequencies.put("CGC", 0.19);
        frequencies.put("CGA", 0.11);
        frequencies.put("CGG", 0.21);
        frequencies.put("ATT", 0.36);
        frequencies.put("ATC", 0.48);
        frequencies.put("ATA", 0.16);
        frequencies.put("ATG", 1.00);
        frequencies.put("ACT", 0.24);
        frequencies.put("ACC", 0.36);
        frequencies.put("ACA", 0.28);
        frequencies.put("ACG", 0.12);
        frequencies.put("AAT", 0.46);
        frequencies.put("AAC", 0.54);
        frequencies.put("AAA", 0.42);
        frequencies.put("AAG", 0.58);
        frequencies.put("AGT", 0.15);
        frequencies.put("AGC", 0.24);
        frequencies.put("AGA", 0.20);
        frequencies.put("AGG", 0.20);
        frequencies.put("GTT", 0.18);
        frequencies.put("GTC", 0.24);
        frequencies.put("GTA", 0.11);
        frequencies.put("GTG", 0.47);
        frequencies.put("GCT", 0.26);
        frequencies.put("GCC", 0.40);
        frequencies.put("GCA", 0.23);
        frequencies.put("GCG", 0.11);
        frequencies.put("GAT", 0.46);
        frequencies.put("GAC", 0.54);
        frequencies.put("GAA", 0.42);
        frequencies.put("GAG", 0.58);
        frequencies.put("GGT", 0.16);
        frequencies.put("GGC", 0.34);
        frequencies.put("GGA", 0.25);
        frequencies.put("GGG", 0.25);
        aminoacids.put("TTT", "F");
        aminoacids.put("TTC", "F");
        aminoacids.put("TTA", "L");
        aminoacids.put("TTG", "L");
        aminoacids.put("TCT", "S");
        aminoacids.put("TCC", "S");
        aminoacids.put("TCA", "S");
        aminoacids.put("TCG", "S");
        aminoacids.put("TAT", "Y");
        aminoacids.put("TAC", "Y");
        aminoacids.put("TAA", "*");
        aminoacids.put("TAG", "*");
        aminoacids.put("TGT", "C");
        aminoacids.put("TGC", "C");
        aminoacids.put("TGA", "*");
        aminoacids.put("TGG", "W");
        aminoacids.put("CTT", "L");
        aminoacids.put("CTC", "L");
        aminoacids.put("CTA", "L");
        aminoacids.put("CTG", "L");
        aminoacids.put("CCT", "P");
        aminoacids.put("CCC", "P");
        aminoacids.put("CCA", "P");
        aminoacids.put("CCG", "P");
        aminoacids.put("CAT", "H");
        aminoacids.put("CAC", "H");
        aminoacids.put("CAA", "Q");
        aminoacids.put("CAG", "Q");
        aminoacids.put("CGT", "R");
        aminoacids.put("CGC", "R");
        aminoacids.put("CGA", "R");
        aminoacids.put("CGG", "R");
        aminoacids.put("ATT", "I");
        aminoacids.put("ATC", "I");
        aminoacids.put("ATA", "I");
        aminoacids.put("ATG", "M");
        aminoacids.put("ACT", "T");
        aminoacids.put("ACC", "T");
        aminoacids.put("ACA", "T");
        aminoacids.put("ACG", "T");
        aminoacids.put("AAT", "N");
        aminoacids.put("AAC", "N");
        aminoacids.put("AAA", "K");
        aminoacids.put("AAG", "K");
        aminoacids.put("AGT", "S");
        aminoacids.put("AGC", "S");
        aminoacids.put("AGA", "R");
        aminoacids.put("AGG", "R");
        aminoacids.put("GTT", "V");
        aminoacids.put("GTC", "V");
        aminoacids.put("GTA", "V");
        aminoacids.put("GTG", "V");
        aminoacids.put("GCT", "A");
        aminoacids.put("GCC", "A");
        aminoacids.put("GCA", "A");
        aminoacids.put("GCG", "A");
        aminoacids.put("GAT", "D");
        aminoacids.put("GAC", "D");
        aminoacids.put("GAA", "E");
        aminoacids.put("GAG", "E");
        aminoacids.put("GGT", "G");
        aminoacids.put("GGC", "G");
        aminoacids.put("GGA", "G");
        aminoacids.put("GGG", "G");
    }

    /**
     * Adds the tag lfs to the variant.
     *
     * @param variant
     */
    public static void addLFS(Variant variant) {
        try {
            Map<String, Object> infos = variant.getInfos();
            String codons = (String) infos.get("COD");
            if (codons != null) {
                String[] cods = codons.split("[/-]");
                // Only supports 2 structures: aaT/aaC and tta-Gta
                if (cods.length != 2) {
                    return;
                }
                String from = aminoacids.get(cods[0].toUpperCase());
                String to = aminoacids.get(cods[1].toUpperCase());
                // Sometimes it's null
                if (from == null || to == null) {
                    return;
                }
                // Synonyms
                if (from.equals(to)) {
                    double source = frequencies.get(cods[0].toUpperCase());
                    double destiny = frequencies.get(cods[1].toUpperCase());
                    double score = source / destiny;
                    if (score > 1) infos.put("LFS", score);
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(LFS.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
