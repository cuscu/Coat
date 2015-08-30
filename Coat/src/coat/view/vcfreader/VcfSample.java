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

    private final File file;
    private final Property<Boolean> enabledProperty = new SimpleBooleanProperty(true);
    private final Property<Level> levelProperty = new SimpleObjectProperty<>(Level.AFFECTED);
    private final long numberOfVariants;
    private Property<File> bamFileProperty = new SimpleObjectProperty<>();
    private Property<File> mistFileProperty = new SimpleObjectProperty<>();

    public VcfSample(File file) {
        this.file = file;
        autodetectBamFile(file);
        autodetectMistFile(file);
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
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            return reader.lines().filter(line -> !line.startsWith("#")).count();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public String toString() {
        return file.getAbsolutePath();
    }

    public File getFile() {
        return file;
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
