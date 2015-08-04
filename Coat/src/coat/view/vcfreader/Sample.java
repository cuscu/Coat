package coat.view.vcfreader;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;

import java.io.File;

/**
 * Stores a Vcf file into memory and the level of affection (UNAFFECTED, AFFECTED, HOMOZYGOUS, HETEROZYGOUS) in the VCF
 * advanced Combiner.
 *
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class Sample {

    public enum Level {
        UNAFFECTED, AFFECTED, HETEROZYGOUS, HOMOZYGOUS
    }

    private final File file;

    private final Property<Level> levelProperty = new SimpleObjectProperty<>(Level.AFFECTED);

    public Sample(File file) {
        this.file = file;
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

}
