package coat.model.poirot;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class StringRelationship {

    private final String source;
    private String target;
    private final Map<String, Object> properties = new HashMap<>();

    public StringRelationship(String source, String target) {
        this.source = source;
        this.target = target;
    }

    public String getTarget() {
        return target;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public String getSource() {
        return source;
    }
}
