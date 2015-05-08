package coat.model.mist;

import coat.utils.OS;
import javafx.concurrent.Task;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class MistCombinator extends Task<Integer> {


    private final int ID_COLUMN = 8;
    private final int START_POOR = 3;
    private final int END_POOR = 4;
    private final int MATCH = 11;

    private final String[] HEADER = {"chrom", "exon_start", "exon_end", "poor_start", "poor_end",
            "gene_id", "gene_name", "exon_number", "exon_id", "transcript_name", "biotype", "match"};

    private final List<File> inputs;
    private final File output;

    public MistCombinator(List<File> files, File output) {
        this.inputs = files;
        this.output = output;
    }


    @Override
    protected Integer call() throws Exception {
        // Let's count the matches so the user will see it in the 'Everything went OK' dialog.
        final AtomicInteger m = new AtomicInteger();
        // The firs file includes the lines with the exons info.
        final List<String[]> refFile = readExons(inputs.get(0));
        // For the rest of inputs only store the IDs.
        final List<Set<String>> files = new ArrayList<>();
        // Muahahaha, parallel reading of the rest of inputs.
        inputs.subList(1, inputs.size()).parallelStream().forEach(f -> files.add(readExonsID(f)));

        // Runs over refFile and checks if the exon_id is contained in the rest of inputs
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(output))) {
            printLine(bw, HEADER);
            for (String[] row : refFile) {
                // the NO-CONDITION algorithm. Look for someone who does not have it.
                boolean match = true;
                for (Set s : files) {
                    if (!s.contains(row[ID_COLUMN])) {
                        match = false;
                        break;
                    }
                }
                if (match) {
                    // This fields are set to .
                    row[START_POOR] = ".";
                    row[END_POOR] = ".";
                    row[MATCH] = ".";
                    printLine(bw, row);
                    m.incrementAndGet();
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(MistCombinator.class.getName()).log(Level.SEVERE, null, ex);
        }
        String message = OS.getStringFormatted("combine.mist.success", output.getAbsolutePath(), m);
        return m.get();
    }

    /**
     * Reads a MIST file and returns a <code>List<String[]></code> with all of the lines split by
     * \t. Lines with the same exon are excluded.
     *
     * @param file The file to read
     * @return a list with line.split("\t")
     */
    private List<String[]> readExons(File file) {
        final List<String[]> exons = new ArrayList<>();
        final Set<String> ids = new TreeSet<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            // Skip HEADER
            reader.readLine();
            reader.lines().forEach(line -> {
                final String[] row = line.split("\t");
                final String id = row[ID_COLUMN];
                // Put only genuine lines.
                if (ids.add(id)) {
                    exons.add(row);
                }
            });
        } catch (IOException ex) {
            Logger.getLogger(MistCombinator.class.getName()).log(Level.SEVERE, null, ex);
        }
        return exons;
    }

    /**
     * Reads a MIST file and returns a Set<String> with all of the unique exon_ids that file
     * contains.
     *
     * @param file the MIST file to read
     * @return a Set with all unique identifiers
     */
    private Set<String> readExonsID(File file) {
        final Set<String> ids = new TreeSet<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            // Skip HEADER
            reader.readLine();
            reader.lines().forEach(line -> ids.add(line.split("\t")[ID_COLUMN]));
        } catch (IOException ex) {
            Logger.getLogger(MistCombinator.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ids;
    }

    /**
     * Prints a String[] in a BufferedWriter, separating values by TAB (\t) and adds a line
     * separator.
     *
     * @param bw  The bufferedWriter to write. Often associated to a FileWriter.
     * @param row The String [] to write.
     * @throws IOException when problems with the BufferedWriter.
     */
    private void printLine(BufferedWriter bw, String[] row) throws IOException {
        // First field does not have \t prefix.
        bw.write(row[0]);
        for (int i = 1; i < row.length; i++) {
            bw.write("\t" + row[i]);
        }
        bw.newLine();
    }

}
