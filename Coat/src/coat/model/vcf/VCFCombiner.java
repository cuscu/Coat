package coat.model.vcf;

import coat.view.vcf.Sample;
import javafx.concurrent.Task;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class VCFCombiner extends Task<Boolean> {

    private final List<BufferedReader> readers = new ArrayList<>();
    private final List<Sample> samples;
    private File output;

    private final List<Variant> variants = new ArrayList<>();

    public VCFCombiner(List<Sample> samples, File output) {
        this.samples = samples;
        this.output = output;
    }


    @Override
    protected Boolean call() throws Exception {
        try {
            openReaders();
            skipHeaders();
            while (allReadersAreOpen()) {
                findNextMatch();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            closeAllReaders();
        }
        return false;
    }

    private void skipHeaders() throws IOException {
        for (BufferedReader reader : readers) {
            String line = "##";
            while (line.startsWith("##")) line = reader.readLine();
        }
    }

    private Variant findNextMatch() throws IOException {
        return null;
    }

    private void closeAllReaders() throws IOException {
        for (BufferedReader reader : readers) reader.close();
    }

    private void openReaders() throws FileNotFoundException {
        for (Sample sample : samples) readers.add(new BufferedReader(new FileReader(sample.getFile())));
    }

    private boolean allReadersAreOpen() throws IOException {
        for (BufferedReader reader : readers) if (!reader.ready()) return false;
        return true;
    }

}
