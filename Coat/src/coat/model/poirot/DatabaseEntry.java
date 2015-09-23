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

    /**
     * Returns the field in the index position. This method is preferable to <code>getField(String key)</code>, as it is
     * faster.
     *
     * @param index position in the list of values
     * @return the value in the index position
     */
    public String getField(int index) {
        return values[index];
    }

    /**
     * Returns the value associated to the key. Use this method when you don't know the position of the field, or to
     * have a more readable code. This method is slightly slower than <code>getFiled(int index)</code>, as it must find
     * the index in a map.
     *
     * @param key    name of the key
     * @return value associated to the key
     */
    public String getField(String key) {
        return values[headers.get(key)];
    }

    @Override
    public String toString() {
        return Arrays.toString(values);
    }
}
