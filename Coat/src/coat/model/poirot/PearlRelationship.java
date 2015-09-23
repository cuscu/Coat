package coat.model.poirot;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class PearlRelationship {

    private final Pearl source;
    private final Pearl target;
    private final Map<String, Object> properties = new HashMap<>();

    public PearlRelationship(Pearl source, Pearl target) {
        this.source = source;
        this.target = target;
        source.addRelationship(target, this);
        target.addRelationship(source, this);
    }

    public Pearl getTarget() {
        return target;
    }

    public Pearl getSource() {
        return source;
    }


    @Override
    public String toString() {
        return source.getName() + "->" + target.getName();
    }

    public Map<String, Object> getProperties() {
        return properties;
    }
}
