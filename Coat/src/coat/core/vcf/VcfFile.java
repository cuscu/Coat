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

import coat.core.variant.Variant;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.*;

/**
 * Stores in memory a Vcf file data.
 *
 * @author Lorente Arencibia, Pascual <pasculorente@gmail.com>
 */
public class VcfFile {

    private final ObservableList<Variant> variants = FXCollections.observableArrayList();
    private final VcfHeader header;

    private File file;
    private Property<Boolean> changed = new SimpleBooleanProperty(false);


    public VcfFile(File file) {
        this.file = file;
        this.header = new VcfHeader();
        readFile(file);
    }

    public VcfFile() {
        this.header = new VcfHeader();
    }

    public VcfFile(VcfHeader header) {
        this.header = header;
        this.file = new File(System.currentTimeMillis() + ".vcf");
        this.file.deleteOnExit();
    }

    private void readFile(File file) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            readLines(reader);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void readLines(final BufferedReader reader) {
        reader.lines().forEach(line -> {
            if (!line.startsWith("#")) variants.add(new Variant(line, this));
            else header.addHeader(line);
        });
    }


    public ObservableList<Variant> getVariants() {
        return variants;
    }

    public File getFile() {
        return file;
    }

    public VcfHeader getHeader() {
        return header;
    }

    public void setChanged(boolean changed) {
        this.changed.setValue(changed);
    }

    public Property<Boolean> changedProperty() {
        return changed;
    }

    public void save(File file) {
        save(file, variants);
    }

    public void save(File file, ObservableList<Variant> variants) {
        if (file.exists() && !file.delete()) System.err.println("No access on " + file);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(getHeader().toString());
            for (Variant variant : variants) {
                writer.write(variant.toString());
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
