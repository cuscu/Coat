package coat.model.poirot;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class PearlRelationship {
    private final Pearl source;
    private Pearl target;
    private Map<String, Object> properties = new HashMap<>();

    public PearlRelationship(Pearl source, Pearl target) {
        this.source = source;
        this.target = target;
    }

    public Pearl getTarget() {
        return target;
    }

    public Pearl getSource() {
        return source;
    }

    public Pearl getOtherNode(Pearl pearl){
        if (source.equals(pearl)) return target;
        else if (target.equals(pearl)) return source;
        else return null;
    }

    public Object getProperty(String name) {
        return properties.get(name);
    }

    public void setProperty(String name, Object value) {
        properties.put(name, value);
    }

    @Override
    public String toString() {
        return source.getName() + "->" + target.getName();
    }
}
