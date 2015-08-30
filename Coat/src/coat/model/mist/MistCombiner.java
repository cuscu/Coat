package coat.model.mist;

import coat.CoatView;
import coat.utils.OS;
import javafx.concurrent.Task;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Combine two or more Mist files. A Mist file is a tab separated file (TSV) with following fields:
 * (1) chrom: chromosome, (2) exon_start: start position of the exon, (3) exon_end: end position of the exon,
 * (4) poor_start: start position of the poor region, (5) poor_end: end position of the poor region,
 * (6) gene_id: gene ID, (7) gene_name: gene name, (8) exon_number: exon number, (9) exon_id: exon ID,
 * (10) transcript_name: transcript name, (11) biotype: gene biotype,
 * (12) match: right, left, inside or overlap, depending on where the poor region affects the exon.
 * Empty fields are indicated with a dot (.).
 * <p>
 * Mist files belong to a UICHUIMI project [ref]. These files must have the header line with exact these names.
 * <p>
 * Mist files are combined by exon. Output file will have poor_start, poor_end and match fields filled with dots (.).
 *
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class MistCombiner extends Task<Integer> {

    private static final int ID_COLUMN = 8;
    private static final int START_POOR = 3;
    private static final int END_POOR = 4;
    private static final int MATCH = 11;

    private static final String[] HEADER = {"chrom", "exon_start", "exon_end", "poor_start", "poor_end",
            "gene_id", "gene_name", "exon_number", "exon_id", "transcript_name", "biotype", "match"};

    private final List<File> inputFiles;
    private final File output;

    /**
     * Creates a new MistCombiner. Run with <code>call()</code> or <code>new Thread(mistCombiner).start()</code>.
     *
     * @param inputFiles list of files to combine
     * @param output     path to output file
     */
    public MistCombiner(List<File> inputFiles, File output) {
        this.inputFiles = inputFiles;
        this.output = output;
    }


    @Override
    protected Integer call() throws Exception {
        updateMessage(String.format("Combining %d files", inputFiles.size()));
        final Map<String, String[]> exons = combine();
        saveToFile(exons);
        final String message = OS.getStringFormatted("combine.mist.success", output.getAbsolutePath(), exons.size());
        CoatView.printMessage(message, "success");
        updateMessage(message);
        return exons.size();
    }

    /**
     * Reads entire lines from first file and only the exon id from the rest of files. Then eliminates all the exons
     * that are not in all of the files.
     * @return a map: the key is the exon id, the value is the line split by tabulator (\t).
     */
    private Map<String, String[]> combine() {
        final Map<String, String[]> exons = readExons(inputFiles.get(0));
        final List<Set<String>> fileExonIds = readExonIds(inputFiles.subList(1, inputFiles.size()));
        fileExonIds.forEach(file -> exons.keySet().retainAll(file));
        return exons;
    }

    /**
     * Reads a MIST file and returns a <code>List<String[]></code> with all of the lines split by tabulator (\t).
     * Lines with the same exon are excluded.
     *
     * @param file The file to read
     * @return a list with line.split("\t")
     */
    private Map<String, String[]> readExons(File file) {
        final Map<String, String[]> exons = new LinkedHashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            reader.readLine(); // header line
            reader.lines().forEach(line -> storeExonLine(exons, line));
        } catch (IOException ex) {
            Logger.getLogger(MistCombiner.class.getName()).log(Level.SEVERE, null, ex);
        }
        return exons;
    }

    private void storeExonLine(Map<String, String[]> exons, String line) {
        final String[] row = line.split("\t");
        final String id = row[ID_COLUMN];
        exons.putIfAbsent(id, row);
    }

    private List<Set<String>> readExonIds(List<File> files) {
        return files.parallelStream().map(this::readExonIDs).collect(Collectors.toList());
    }

    /**
     * Reads a MIST file and returns a set with all of the unique exon_ids that file contains.
     *
     * @param file the MIST file to read
     * @return a Set with all unique identifiers
     */
    private Set<String> readExonIDs(File file) {
        final Set<String> ids = new TreeSet<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            reader.readLine();
            reader.lines().forEach(line -> ids.add(line.split("\t")[ID_COLUMN]));
        } catch (IOException ex) {
            Logger.getLogger(MistCombiner.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ids;
    }

    private void saveToFile(Map<String, String[]> exons) {
        updateMessage(String.format("%d matches found. Saving to file", exons.size()));
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(output))) {
            writeToFile(bw, HEADER);
            for (Map.Entry<String, String[]> entry : exons.entrySet()) writeExonInFile(bw, entry);
        } catch (Exception ex) {
            Logger.getLogger(MistCombiner.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void writeExonInFile(BufferedWriter bw, Map.Entry<String, String[]> entry) throws IOException {
        final String[] row = entry.getValue();
        row[START_POOR] = ".";
        row[END_POOR] = ".";
        row[MATCH] = ".";
        writeToFile(bw, row);
    }

    private void writeToFile(BufferedWriter bw, String[] row) throws IOException {
        bw.write(arrayToString(row, "\t"));
        bw.newLine();
    }

    private String arrayToString(String[] array, String separator) {
        if (array.length == 0) return "";
        String ret = array[0];
        for (int i = 1; i < array.length; i++) ret += separator + array[i];
        return ret;
    }


}
