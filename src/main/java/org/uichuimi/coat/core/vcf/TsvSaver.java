/*
 * Copyright (c) UICHUIMI 2017
 *
 * This file is part of Coat.
 *
 * Coat is free software:
 * you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * Coat is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with Coat.
 *
 * If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.uichuimi.coat.core.vcf;

import org.uichuimi.coat.utils.OS;
import org.uichuimi.coat.view.vcfreader.ValueUtils;
import org.uichuimi.vcf.header.VcfHeader;
import org.uichuimi.vcf.variant.Variant;

import java.io.*;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class TsvSaver {

    private static final int FIXED_COLUMNS = 7;
    private final File output;
    private final Set<Variant> variants;
    private List<String> infoNames;
    private String[] header;
    private int length;
    private final VcfHeader vcfHeader;

    private PrintWriter writer;

    public TsvSaver(File output, Set<Variant> variants) {
        this.output = output;
        this.variants = variants;
        this.vcfHeader = variants.iterator().next().getHeader();
    }

    public void invoke() {
        try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(output)))) {
            saveAsTsv(writer);
        } catch (IOException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void saveAsTsv(PrintWriter writer) {
        readInfoColumns();
        this.writer = writer;
        writer.println(OS.asString(header));
        variants.forEach(this::writeVariant);
    }

    private void readInfoColumns() {
        this.length = FIXED_COLUMNS
                + vcfHeader.getInfoLines().size()
                + vcfHeader.getSamples().size();
        this.infoNames = vcfHeader.getIdList("INFO");
        this.header = createHeader();
    }

    private String[] createHeader() {
        final String[] head = new String[length];
        setFixedHeaderColumns(head);
        int i = FIXED_COLUMNS;
        for (String info : infoNames) head[i++] = info;
        for (String name : vcfHeader.getSamples())
            head[i++] = name;
        return head;
    }

    private void setFixedHeaderColumns(String[] head) {
        head[0] = "CHROM";
        head[1] = "POS";
        head[2] = "ID";
        head[3] = "REF";
        head[4] = "ALT";
        head[5] = "QUAL";
        head[6] = "FILTER";
    }

    private void writeVariant(Variant var) {
        String[] line = getTabulatedVariant(var);
        writer.println(OS.asString(line));
    }

    private String[] getTabulatedVariant(Variant var) {
        String[] line = new String[length];
        setFixedColumns(var, line);
        setInfoColumns(var, line);
        setFormatColumns(var, line);
        return line;
    }

    private void setFixedColumns(Variant var, String[] line) {
        line[0] = var.getCoordinate().getChrom();
        line[1] = String.valueOf(var.getCoordinate().getPosition());
        line[2] = String.join(",", var.getIdentifiers());
        line[3] = String.join(",", var.getReferences());
        line[4] = String.join(",", var.getAlternatives());
        line[5] = String.format("%.4f", var.getQuality());
        line[6] = String.join(",", var.getFilters());
    }

    private void setInfoColumns(Variant var, String[] line) {
        for (int i = 0; i < infoNames.size(); i++) {
            final Object info = var.getInfo().get(infoNames.get(i));
            line[i + FIXED_COLUMNS] = ValueUtils.toString(info);
        }
    }



    private void setFormatColumns(Variant variant, String[] line) {
        final List<String> samples = vcfHeader.getSamples();
        final int start = line.length - samples.size();
        for (int i = 0; i < samples.size(); i++)
            line[i + start] = variant.getSampleInfo(i).get("GT");
    }
}
