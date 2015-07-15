package coat.model.poirot;

import java.util.List;

/**
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class DatabaseEntry {

    private final List<String> fields;

    public DatabaseEntry(List<String> fields) {
        this.fields = fields;
    }

    public String getField(int index) {
        return fields.get(index);
    }
}
