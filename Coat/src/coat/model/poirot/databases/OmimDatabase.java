package coat.model.poirot.databases;

import coat.CoatView;
import coat.model.poirot.DatabaseEntry;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.zip.GZIPInputStream;

/**
 * Manages the Omim database.
 * <p>
 * 0 gene symbol
 * 1 gene name
 * 2 status (C,P,I or L)
 * 3 disorders
 * <p>
 * C = confirmed: observed in at least two laboratories or in several families.
 * P = provisional: based on evidence from one laboratory or one family.
 * I = inconsistent: results of different laboratories disagree.
 * L = limbo: evidence not as strong as that provisional, but included for heuristic reasons. (Same as `tentative'.)
 *
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class OmimDatabase {


    private final static Map<String, List<DatabaseEntry>> index;

    static {
        index = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(OmimDatabase.class.getResourceAsStream("omim-normalized.tsv.gz"))))) {
            reader.lines().forEach((line) -> {
                final String[] row = line.split("\t");
                final DatabaseEntry entry = new DatabaseEntry(Arrays.asList(row));
                String standardSymbol = HGNCDatabase.getStandardSymbol(row[0]);
                if (standardSymbol == null) standardSymbol = row[0];
                addToIndex(entry, standardSymbol);

            });
            CoatView.printMessage("Omim database successfully loaded", "info");
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
