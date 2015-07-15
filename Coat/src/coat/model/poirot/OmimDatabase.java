package coat.model.poirot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.zip.GZIPInputStream;

/**
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class OmimDatabase implements Database {

    private final static List<String> headers;
    private final static List<DatabaseEntry> entries;

    static {
        headers = new ArrayList<>();
        entries = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(OmimDatabase.class.getResourceAsStream("omim-normalized.tsv.gz"))))) {
            System.out.println("Loading Omim database");
            reader.lines().forEach((line) -> entries.add(new DatabaseEntry(Arrays.asList(line.split("\t")))));
            System.out.println("Omim database successfully loaded");
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
