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

package coat.core.vcfreader;

import coat.core.poirot.dataset.Dataset;
import coat.core.poirot.dataset.Instance;
import coat.core.poirot.dataset.VcfLoader;
import coat.utils.OS;
import javafx.collections.ObservableList;
import javafx.stage.FileChooser;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class VcfSaver {

    private VcfFile vcfFile;
    private File output;
    private List<Variant> saveVariants;

    public VcfSaver(VcfFile vcfFile, File output, List<Variant> variants) {
        this.vcfFile = vcfFile;
        this.output = output;
        this.saveVariants = variants;
    }

    public void invoke() {
        try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(output)))) {
            vcfFile.getUnformattedHeaders().forEach(writer::println);
            saveVariants.forEach(writer::println);
        } catch (IOException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void saveAs(Dataset dataset, ObservableList<Instance> variants, File f, FileChooser.ExtensionFilter extension) {

    }

    public static void saveToVcf(Dataset dataset, ObservableList<Instance> variants, File output, File reference) {
        final VcfHeader header = VcfLoader.loadHeader(reference);
        if (output.exists()) output.delete();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(output))) {
            writer.write("##fileformat=" + header.getFileFormat());
            writer.newLine();
            for (Map.Entry entry : header.getHeaders().entrySet()) {
                writer.write("##" + entry.getKey() + "=" + entry.getValue());
                writer.newLine();
            }
            for (Map<String, String> map : header.getInfos()) {
                writer.write("##INFO=<");
                for (Map.Entry<String, String> entry : map.entrySet()) {
                    writer.write(entry.getKey() + "=");
                    if (entry.getValue().contains(" ")) writer.write("\"" + entry.getValue() + "\"");
                    else writer.write(entry.getValue());
                }
                writer.write(">");
                writer.newLine();
            }
            for (Map<String, String> map : header.getFormats()) {
                writer.write("##FORMAT=<");
                for (Map.Entry<String, String> entry : map.entrySet()) {
                    writer.write(entry.getKey() + "=");
                    if (entry.getValue().contains(" ")) writer.write("\"" + entry.getValue() + "\"");
                    else writer.write(entry.getValue());
                }
                writer.write(">");
                writer.newLine();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveToTSV(Dataset dataset, ObservableList<Instance> variants, File f) {
        if (f.exists()) f.delete();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(f))) {
            final int size = dataset.getColumnNames().size();
            writer.write(OS.asString("\t", dataset.getColumnNames()));
            writer.newLine();
            for (Instance instance : variants) {
                writer.write(instance.getField(0).toString());
                for (int i = 1; i < size; i++) {
                    final Object value = instance.getField(i);
                    writer.write((value == null ? "." : value.toString()) + "\t");
                }
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
