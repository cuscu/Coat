package coat.model.poirot.databases;

import coat.CoatView;
import coat.model.poirot.DatabaseEntry;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

/**
 * Manages the Omim database.
 * <p>
 * 0 gene symbol.
 * 1 gene name.
 * 2 status (C,P,I or L).
 * 3 disorders.
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
    private final static Map<String, Integer> headers;

    static {
        headers = new HashMap<>();
        headers.put("symbol", 0);
        headers.put("name", 1);
        headers.put("status", 2);
        headers.put("disorders", 3);
        index = readFile();
        if (index != null) CoatView.printMessage("Omim database successfully loaded", "info");
    }


    private static Map<String, List<DatabaseEntry>> readFile() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(OmimDatabase.class.getResourceAsStream("omim-normalized.tsv.gz"))))) {
            return reader.lines()
                    .map(OmimDatabase::createEntry)
                    .collect(Collectors.groupingBy(databaseEntry -> databaseEntry.getField(0)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static DatabaseEntry createEntry(String line) {
        final String[] row = line.split("\t");
        String standardSymbol = HGNCDatabase.getStandardSymbol(row[0]);
        if (standardSymbol != null) row[0] = standardSymbol;
        return new DatabaseEntry(headers, row);
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
