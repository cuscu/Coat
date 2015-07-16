package coat.model.poirot;

import coat.CoatView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.zip.GZIPInputStream;

/**
 * Manages the Omim database.
 *
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class OmimDatabase implements Database {

    private final static Collection<String> headers;
    private final static Collection<DatabaseEntry> entries;

    static {
        headers = Collections.singletonList("symbol\tname\tstatus\tdisorders");
        entries = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(OmimDatabase.class.getResourceAsStream("omim-normalized.tsv.gz"))))) {
            reader.lines().forEach((line) -> entries.add(new DatabaseEntry(Arrays.asList(line.split("\t")))));
            CoatView.printMessage("Omim database successfully loaded", "info");
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
