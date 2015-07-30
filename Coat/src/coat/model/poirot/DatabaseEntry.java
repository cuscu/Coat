package coat.model.poirot;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class DatabaseEntry {

    private final String[] values;
    private final Map<String, Integer> headers;

    public DatabaseEntry(List<String> fields, Map<String, Integer> headers) {
        this.headers = headers;
        values = new String[fields.size()];
        for (int i = 0; i < values.length; i++) values[i] = fields.get(i);
    }

    public DatabaseEntry(String[] values, Map<String, Integer> headers) {
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
