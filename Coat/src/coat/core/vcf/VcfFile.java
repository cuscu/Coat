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

import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;

/**
 * Stores in memory a Vcf file data.
 *
 * @author Lorente Arencibia, Pascual <pasculorente@gmail.com>
 */
public class VcfFile {

    private final ObservableList<Variant> variants = FXCollections.observableArrayList();
    private final VcfHeader header;

    private File file;
    private File temp;
    private Property<Boolean> changed = new SimpleBooleanProperty(false);

    public VcfFile(File file) {
        this.file = file;
        temp = new File(file.getParentFile(), ".~" + file.getName());
        this.header = new VcfHeader();
        readFile(file);
        temp.deleteOnExit();
    }

    public VcfFile() {
        this.header = new VcfHeader();
    }

    public VcfFile(VcfHeader header) {
        this.header = header;
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

    public File getTemp() {
        if (!temp.exists()) {
            try {
                Files.copy(file.toPath(), temp.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return temp;
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

    public synchronized void saveToTemp(List<Variant> variants) {
        final File temp = getTemp();
        final File temper = new File(temp.getAbsolutePath() + ".temp");
        try (BufferedReader reader = new BufferedReader(new FileReader(temp));
             BufferedWriter writer = new BufferedWriter(new FileWriter(temper))) {
            writer.write(getHeader().toString());
            reader.lines().forEach(line -> {
                try {
                    if (!line.startsWith("#")) {
                        final Variant variant = getVariant(line, variants);
                        if (variant == null) return;
                        if (variant.getTemp() != null) {
                            writer.write(variant.getTemp());
                            variant.setTemp(null);
                        } else writer.write(line);
                        writer.newLine();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        replaceTemp(temp, temper);
    }

    private void replaceTemp(File temp, File temper) {
        try {
            Files.copy(temper.toPath(), temp.toPath(), StandardCopyOption.REPLACE_EXISTING);
            temper.delete();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Variant getVariant(String line, List<Variant> variants) {
        String[] split = line.split("\t");
        final String chrom = split[0];
        final Integer pos = Integer.valueOf(split[1]);
        return variants.stream().filter(variant -> variant.getChrom().equals(chrom) && variant.getPos() == pos).findFirst().orElse(null);
    }
}
