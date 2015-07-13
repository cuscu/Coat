package coat.model.poirot;

import coat.view.poirot.GraphNode;
import coat.view.poirot.GraphRelationship;
import coat.view.poirot.NodePairKey;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents the Graphic Graph. Contains a list of GraphNodes and a Map of GraphRelationships that can be accessed via
 * NodePairKey, since relationships are not directed. The Graph can be updated with the method
 * <code>setOriginNodes()</code>.
 *
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class Graph {


    private final List<GraphNode> nodes = new ArrayList<>();
    private final Map<NodePairKey, GraphRelationship> relationships = new HashMap<>();


    public synchronized List<GraphNode> getNodes() {
        return nodes;
    }

    public synchronized Map<NodePairKey, GraphRelationship> getRelationships() {
        return relationships;
    }

    public void setOriginNodes(List<Pearl> originNodes) {
        nodes.clear();
        relationships.clear();
        originNodes.forEach(gene -> {
            final List<List<PearlRelationship>> paths = ShortestPath.getShortestPaths(gene);
            addToGraph(paths);
        });
    }

    private void addToGraph(List<List<PearlRelationship>> paths) {
        paths.forEach(path -> path.forEach(relationship -> {
            final GraphNode target = addOrGetNode(relationship.getTarget());
            final GraphNode source = addOrGetNode(relationship.getSource());
            addRelationship(source, target, relationship);
        }));
    }

    private GraphNode addOrGetNode(Pearl node) {
        GraphNode graphNode = getGraphNode(node);
        if (graphNode == null) graphNode = createGraphNode(node);
        return graphNode;
    }

    private GraphNode getGraphNode(Pearl node) {
        for (GraphNode graphNode : nodes) if (graphNode.getPearl().equals(node)) return graphNode;
        return null;
    }

    private GraphNode createGraphNode(Pearl node) {
        final GraphNode graphNode = new GraphNode(node);
        nodes.add(graphNode);
        return graphNode;
    }

    private void addRelationship(GraphNode source, GraphNode target, PearlRelationship relationship) {
        GraphRelationship graphRelationship = getOrCreateGraphRelationship(new NodePairKey(source, target));
        if (!graphRelationship.getRelationships().contains(relationship))
            graphRelationship.getRelationships().add(relationship);
    }

    private GraphRelationship getOrCreateGraphRelationship(NodePairKey key) {
        GraphRelationship graphRelationship = relationships.get(key);
        if (graphRelationship == null) {
            graphRelationship = new GraphRelationship();
            relationships.put(key, graphRelationship);
        }
        return graphRelationship;
    }

}
