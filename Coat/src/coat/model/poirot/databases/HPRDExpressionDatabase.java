package coat.model.poirot.databases;

import coat.model.poirot.Database;
import coat.model.poirot.DatabaseEntry;

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
public class HPRDExpressionDatabase implements Database {


    private final static List<String> headers;
    private final static List<DatabaseEntry> entries;

    static {
        headers = new ArrayList<>();
        entries = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(OmimDatabase.class.getResourceAsStream("hprd-phenotypes.tsv.gz"))))) {
            System.out.println("Loading HPRD database");
            reader.lines().forEach((line) -> entries.add(new DatabaseEntry(Arrays.asList(line.split("\t")))));
            System.out.println("HPRD database successfully loaded");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Collection<String> getUnmodifiableHeaders() {
        return Collections.unmodifiableCollection(headers);
    }

    @Override
    public Collection<DatabaseEntry> getUnmodifiableEntries() {
        return Collections.unmodifiableCollection(entries);
    }
}
