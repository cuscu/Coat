package coat.model.poirot.databases;

import coat.CoatView;
import coat.model.poirot.DatabaseEntry;
import javafx.application.Platform;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.zip.GZIPInputStream;

/**
 * 0 hprd_id   00004
 * 1 refseq_id NP_000682.3
 * 2 geneSymbol    ALDH3A1
 * 3 expression_term   Stomach
 * 4 status    General
 * 5 reference_id  1737758
 *
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class HPRDExpressionDatabase {

    private final static Map<String, List<DatabaseEntry>> index;

    static {
        index = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(HPRDExpressionDatabase.class.getResourceAsStream("hprd-phenotypes.tsv.gz"))))) {
            reader.lines().forEach((line) -> {
                final String[] row = line.split("\t");
                final DatabaseEntry entry = new DatabaseEntry(Arrays.asList(row));
                String standardSymbol = HGNCDatabase.getStandardSymbol(row[2]);
                if (standardSymbol == null) standardSymbol = row[2];
                addToIndex(entry, standardSymbol);
            });
            Platform.runLater(() -> CoatView.printMessage("HPRD database successfully loaded", "info"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void addToIndex(DatabaseEntry entry, String standardSymbol) {
        List<DatabaseEntry> databaseEntries = index.get(standardSymbol);
        if (databaseEntries == null) {
            databaseEntries = new ArrayList<>();
            index.put(standardSymbol, databaseEntries);
        }
        databaseEntries.add(entry);
    }

    /**
     * Gets the related entries associated to the HGNC standard symbol.
     *
     * @param symbol a standard HGNC gene symbol
     * @return a list with the entries related to the gene symbol, or an empty list if no entries are associated
     */
    public static List<DatabaseEntry> getEntries(String symbol) {
        return index.getOrDefault(symbol, Collections.emptyList());

    }
}
