package coat.model.poirot;

import java.util.Arrays;
import java.util.Map;

/**
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class DatabaseEntry {

    private final String[] values;
    private final Map<String, Integer> headers;

    public DatabaseEntry(Map<String, Integer> headers, String[] values) {
        this.values = values;
        this.headers = headers;
    }

    public String getField(int index) {
        return values[index];
    }

    public String getField(String key) {
        return values[headers.get(key)];
    }

    @Override
    public String toString() {
        return Arrays.toString(values);
    }
}
