package coat.model.poirot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.zip.GZIPInputStream;

/**
 * 0 HGNC ID
 * 1 Approved Symbol
 * 2 Approved Name
 * 3 Previous Symbols
 * 4 Synonyms
 *
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class HGNCDatabase implements Database {
    private final static List<String> headers;
    private final static List<DatabaseEntry> entries;

    static {
        entries = new ArrayList<>();
        headers = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(HGNCDatabase.class.getResourceAsStream("hgnc-complete.tsv.gz"))))) {
            System.out.println("Loading HGNC database");
            headers.addAll(Arrays.asList(reader.readLine().split("\t")));
            reader.lines().forEach((line) -> entries.add(new DatabaseEntry(Arrays.asList(line.split("\t")))));
            System.out.println("HGNC database successfully loaded");
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
