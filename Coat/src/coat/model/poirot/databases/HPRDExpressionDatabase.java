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
    private final static Map<String, Integer> headers;


    static {
        headers = new HashMap<>();
        headers.put("hprd_id", 0);
        headers.put("refseq_id", 1);
        headers.put("symbol", 2);
        headers.put("expression", 3);
        headers.put("status", 4);
        headers.put("reference_id", 5);
        index = readFile();
        if (index != null) CoatView.printMessage("HPRD expression database successfully loaded", "info");

    }

    private static Map<String, List<DatabaseEntry>> readFile() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(HPRDExpressionDatabase.class.getResourceAsStream("hprd-phenotypes.tsv.gz"))))) {
            return reader.lines()
                    .map(HPRDExpressionDatabase::createEntry)
                    .collect(Collectors.groupingBy(databaseEntry -> databaseEntry.getField(2)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static DatabaseEntry createEntry(String line) {
        final String[] row = line.split("\t");
        final String standardSymbol = HGNCDatabase.getStandardSymbol(row[0]);
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
