package coat.view.vcfreader;

import java.io.File;

/**
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class Sample {

    private File file;
    private String level = "unaffected";
    //"unaffected", "heterocygous", "homocygous", "affected"

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

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }
}
