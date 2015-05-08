/*
 * Copyright (C) 2014 UICHUIMI
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package coat.mist;

import coat.CoatView;
import coat.combinevcf.FileList;
import coat.graphic.SizableImage;
import coat.utils.FileManager;
import coat.utils.OS;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for the Combine MIST Window.
 *
 * @author Pascual Lorente Arencibia (pasculorente@gmail.com)
 */
public class CombineMIST {

    @FXML
    private FileList files;
    @FXML
    private TextField output;
    @FXML
    private Button startButton;

    private final int ID_COLUMN = 8;
    private final int START_POOR = 3;
    private final int END_POOR = 4;
    private final int MATCH = 11;

    private final String[] HEADER = {"chrom", "exon_start", "exon_end", "poor_start", "poor_end",
        "gene_id", "gene_name", "exon_number", "exon_id", "transcript_name", "biotype", "match"};

    /**
     * Initializes the controller class.
     */
    @FXML
    private void initialize() {
        // The start Button is disabled until user selects an output file.
        startButton.setDisable(true);
        startButton.setOnAction(e -> intersect(files.getFiles(), new File(output.getText())));
        startButton.setGraphic(new SizableImage("coat/img/start.png", SizableImage.MEDIUM_SIZE));
        files.setFilters(FileManager.MIST_FILTER);
    }

    @FXML
    private void selectOutput() {
        String message = OS.getStringFormatted("select.file", "MIST");
        File f = FileManager.saveFile(output, message, FileManager.MIST_FILTER);
        if (f != null) startButton.setDisable(false);

    }

    /**
     * The main method of the intersection. Reads each file and finds which lines contain the same
     * exon for all of them.
     */
    private void intersect(List<File> inputs, File output) {
        // Let's count the matches so the user will see it in the 'Everything went OK' dialog.
        final AtomicInteger m = new AtomicInteger();
        // The firs file includes the lines with the exons info.
        final List<String[]> refFile = readExons(inputs.get(0));
        // For the rest of files only store the IDs.
        final List<Set<String>> files = new ArrayList<>();
        // Muahahaha, parallel reading of the rest of files.
        inputs.subList(1, inputs.size()).parallelStream().forEach(f -> files.add(readExonsID(f)));

        // Runs over refFile and checks if the exon_id is contained in the rest of files
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
            Logger.getLogger(CombineMIST.class.getName()).log(Level.SEVERE, null, ex);
        }
        String message = OS.getStringFormatted("combine.mist.success", output.getAbsolutePath(), m);
        CoatView.printMessage(message, "success");
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
            Logger.getLogger(CombineMIST.class.getName()).log(Level.SEVERE, null, ex);
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
            Logger.getLogger(CombineMIST.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ids;
    }

    /**
     * Prints a String[] in a BufferedWriter, separating values by TAB (\t) and adds a line
     * separator.
     *
     * @param bw The bufferedWriter to write. Often associated to a FileWriter.
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
