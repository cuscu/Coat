package coat.model.vcf;

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
}
