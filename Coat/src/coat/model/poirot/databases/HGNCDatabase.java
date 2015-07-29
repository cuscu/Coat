package coat.model.poirot.databases;

import coat.CoatView;
import javafx.application.Platform;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 * Standard gene name database. Use <code>getStandardSymbol()</code> to get the standard HGNC symbol of the gene, and
 * <code>getName()</code> to get the whole name of the gene.
 * <p>
 * 0 HGNC ID
 * 1 Approved Symbol
 * 2 Approved Name
 * 3 Previous Symbols
 * 4 Synonyms
 *
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class HGNCDatabase {

    private final static Map<String, String> names = new HashMap<>();
    private final static Map<String, String> description = new HashMap<>();

    static {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(HGNCDatabase.class.getResourceAsStream("hgnc-complete.tsv.gz"))))) {
            reader.readLine();
            reader.lines().forEach(line -> {
                final String[] row = line.split("\t");
                final String standardName = row[1];
                final String[] previousNames = row[3].split("\\|");
                Arrays.stream(previousNames).forEach(name -> names.put(name, standardName));
                final String[] synonyms = row[4].split("\\|");
                Arrays.stream(synonyms).forEach(name -> names.put(name, standardName));
                description.put(standardName, row[2]);
            });
            Platform.runLater(() -> CoatView.printMessage("HGNC database successfully loaded", "info"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets the HGNC standard Symbol of the given gene.
     *
     * @param symbol current symbol
     * @return the standard symbol or null if not available
     */
    public static String getStandardSymbol(String symbol) {
        return names.get(symbol);
    }

    /**
     * Gets the name (or description) of the gene.
     *
     * @param symbol standard symbol. Get it using <code>getStandardSymbol()</code>
     * @return the name or null if not available
     */
    public static String getName(String symbol) {
        return description.get(symbol);
    }
}
