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
import vcf.Variant;
import vcf.VcfFile;

import java.io.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class TsvSaver {

    private static final int FIXED_COLUMNS = 7;
    private final VcfFile vcfFile;
    private final File output;
    private final List<Variant> variants;
    private List<String> infoNames;
    private String[] header;
    private int length;

    private PrintWriter writer;

    public TsvSaver(VcfFile vcfFile, File output, List<Variant> variants) {
        this.vcfFile = vcfFile;
        this.output = output;
        this.variants = variants;
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
        this.length = FIXED_COLUMNS + vcfFile.getHeader().getComplexHeaders().get("INFO").size();
        this.infoNames = vcfFile.getHeader().getComplexHeaders().get("INFO").stream().map(map -> map.get("ID")).collect(Collectors.toList());
        this.header = createHeader();
    }

    private String[] createHeader() {
        final String[] head = new String[length];
        setFixedHeaderColumns(head);
        int i = FIXED_COLUMNS;
        for (String info : infoNames) head[i++] = info;
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
        return line;
    }

    private void setInfoColumns(Variant var, String[] line) {
//        fillEmpties(line);
        for (int i = 0; i < infoNames.size(); i++) {
            final String info = (String) var.getInfo().get(infoNames.get(i));
            line[i + FIXED_COLUMNS] = info == null ? "." : info;
        }
    }

    private void setInfo(String[] line, String key, Object val) {
        int index = infoNames.indexOf(key);
        // val == null when key is located, but has no value (a flag)
        if (index != -1) line[index + FIXED_COLUMNS] = (val == null) ? "yes" : val.toString();
    }

    private void fillEmpties(String[] line) {
        for (int k = FIXED_COLUMNS; k < line.length; k++) line[k] = ".";
    }

    private void setFixedColumns(Variant var, String[] line) {
        line[0] = var.getChrom();
        line[1] = String.valueOf(var.getPosition());
        line[2] = var.getId();
        line[3] = var.getRef();
        line[4] = var.getAlt();
        line[5] = String.format("%.4f", var.getQual());
        line[6] = var.getFilter();
    }

}
