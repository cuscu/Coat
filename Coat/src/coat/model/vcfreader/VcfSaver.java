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

package coat.model.vcfreader;

import coat.model.poirot.databases.Dataset;
import coat.model.poirot.databases.Instance;
import coat.utils.FileManager;
import javafx.collections.ObservableList;
import javafx.stage.FileChooser;

import java.io.*;
import java.util.List;
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

    public static void saveAs(Dataset dataset, ObservableList<Instance> variants, File f, FileChooser.ExtensionFilter extension) {
        if (extension == FileManager.TSV_FILTER) {
            if (f.exists()) f.delete();
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(f))) {
                final int size = dataset.getColumnNames().size();
                writer.write(dataset.getColumnNames().get(0));
                for (int i = 1; i < size; i++) writer.write(dataset.getColumnNames().get(i) + "\t");
                writer.newLine();
                for (Instance instance : dataset.getInstances()) {
                    writer.write(instance.getField(0).toString());
                    for (int i = 1; i < size; i++) writer.write(instance.getField(i).toString() + "\t");
                    writer.newLine();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
