package coat.view.poirot;

import coat.model.poirot.PearlRelationship;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class GraphRelationship {
    private final GraphNode source;
    private final GraphNode target;
    private final Map<String, Object> properties;
    private List<PearlRelationship> relationships = new ArrayList<>();

    public GraphRelationship(GraphNode source, GraphNode target) {
        this.source = source;
        this.target = target;
        this.properties = new HashMap<>();
    }

    public GraphNode getTarget() {
        return target;
    }

    public GraphNode getOtherNode(GraphNode node) {
        return node.equals(target) ? source : target;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public GraphNode getSource() {
        return source;
    }

    public List<PearlRelationship> getRelationships() {
        return relationships;
    }
}
