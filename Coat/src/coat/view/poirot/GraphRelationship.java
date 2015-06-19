package coat.view.poirot;

import java.util.Map;

/**
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class GraphRelationship {
    private final GraphNode source;
    private GraphNode target;
    private final Map<String, Object> properties;

    public GraphRelationship(GraphNode source, GraphNode target, Map<String, Object> properties) {

        this.source = source;
        this.target = target;
        this.properties = properties;
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
}
