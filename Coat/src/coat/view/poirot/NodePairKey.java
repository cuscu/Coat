package coat.view.poirot;

/**
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class NodePairKey implements Comparable<NodePairKey> {

    private final String key;
    private final GraphNode source;
    private final GraphNode target;

    public NodePairKey(GraphNode source, GraphNode target) {
        this.source = source;
        this.target = target;
        if (source.getPearl().getGeneSymbol().compareTo(target.getPearl().getGeneSymbol()) < 0)
            key = source.getPearl().getGeneSymbol() + target.getPearl().getGeneSymbol();
        else key = target.getPearl().getGeneSymbol() + source.getPearl().getGeneSymbol();
    }

    public GraphNode getSource() {
        return source;
    }

    public GraphNode getTarget() {
        return target;
    }

    public String getKey() {
        return key;
    }


    @Override
    public int hashCode() {
        return key.hashCode();
    }

    @Override
    public int compareTo(NodePairKey other) {
        return key.compareTo(other.key);
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && obj.getClass() == NodePairKey.class && key.equals(((NodePairKey) obj).key);
    }
}
