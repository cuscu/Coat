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

package coat.view.vcfreader;

import coat.utils.OS;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Stores a Vcf file into memory and the level of affection (UNAFFECTED, AFFECTED, HOMOZYGOUS, HETEROZYGOUS) in the VCF
 * advanced Combiner.
 *
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class VcfSample {

    private final File vcfFile;

    private final Property<Boolean> enabledProperty = new SimpleBooleanProperty(true);
    private final Property<Level> levelProperty = new SimpleObjectProperty<>(Level.AFFECTED);
    private final long numberOfVariants;
    private Property<File> bamFileProperty = new SimpleObjectProperty<>();
    private Property<File> mistFileProperty = new SimpleObjectProperty<>();

    public VcfSample(File vcfFile) {
        this.vcfFile = vcfFile;
        autodetectBamFile(vcfFile);
        autodetectMistFile(vcfFile);
        numberOfVariants = determineNumberOfVariants();
    }

    private void autodetectMistFile(File file) {
        final File mist = new File(file.getAbsolutePath().replace(".vcf", ".mist"));
        if (mist.exists()) mistFileProperty.setValue(mist);
    }

    private void autodetectBamFile(File file) {
        final File bam = new File(file.getAbsolutePath().replace(".vcf", ".bam"));
        if (bam.exists()) bamFileProperty.setValue(bam);
    }


    private long determineNumberOfVariants() {
        try (BufferedReader reader = new BufferedReader(new FileReader(vcfFile))) {
            return reader.lines().filter(line -> !line.startsWith("#")).count();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public String toString() {
        return vcfFile.getAbsolutePath();
    }

    public File getVcfFile() {
        return vcfFile;
    }

    public Level getLevel() {
        return levelProperty.getValue();
    }

    public void setLevel(Level level) {
        levelProperty.setValue(level);
    }

    public Property<Level> levelProperty() {
        return levelProperty;
    }

    public Property<Boolean> enabledProperty() {
        return enabledProperty;
    }

    public Property<File> bamFileProperty() {
        return bamFileProperty;
    }

    public Property<File> mistFileProperty() {
        return mistFileProperty;
    }

    public long getNumberOfVariants() {
        return numberOfVariants;
    }

    public File getBamFile() {
        return bamFileProperty.getValue();
    }

    public File getMistFile() {
        return mistFileProperty.getValue();
    }

    public void setBamFile(File file) {
        bamFileProperty.setValue(file);
    }

    public void setMistFile(File file) {
        mistFileProperty.setValue(file);
    }

    public enum Level {
        UNAFFECTED {
            @Override
            public String toString() {
                return OS.getString("unaffected");
            }
        }, AFFECTED {
            @Override
            public String toString() {
                return OS.getString("affected");
            }
        }, HETEROZYGOUS {
            @Override
            public String toString() {
                return OS.getString("heterozygous");
            }
        }, HOMOZYGOUS {
            @Override
            public String toString() {
                return OS.getString("homozygous");
            }
        }
    }

}
