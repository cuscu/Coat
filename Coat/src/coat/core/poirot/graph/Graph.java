/******************************************************************************
 * Copyright (C) 2015 UICHUIMI                                                *
 *                                                                            *
 * This program is free software: you can redistribute it and/or modify it    *
 * under the terms of the GNU General Public License as published by the      *
 * Free Software Foundation, either version 3 of the License, or (at your     *
 * option) any later version.                                                 *
 *                                                                            *
 * This program is distributed in the hope that it will be useful, but        *
 * WITHOUT ANY WARRANTY; without even the implied warranty of                 *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                       *
 * See the GNU General Public License for more details.                       *
 *                                                                            *
 * You should have received a copy of the GNU General Public License          *
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.      *
 ******************************************************************************/

package coat.core.poirot.graph;

import coat.core.poirot.Pearl;
import coat.core.poirot.PearlRelationship;
import coat.core.poirot.ShortestPath;
import coat.view.poirot.GraphNode;
import coat.view.poirot.GraphRelationship;
import coat.view.poirot.NodePairKey;

import java.util.*;

/**
 * Represents the Graphical Graph. Contains a list of GraphNodes and a Map of GraphRelationships that can be accessed
 * via NodePairKey, since relationships are not directed. The Graph can be updated with the method
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

    /**
     * Updates the graph by settings its origin genes, id est, the genes shown in the list of affected genes, but they
     * can be any Pearl. Graph will create a subgraph by using the PearlRelationships of the Pearls.
     *
     * @param originNodes the initial list of genes
     */
    public void setOriginNodes(List<Pearl> originNodes) {
        clearGraph();
        createGraph(originNodes);
    }

    public void clearGraph() {
        nodes.clear();
        relationships.clear();
    }

    private void createGraph(List<Pearl> originNodes) {
        originNodes.stream()
                .map(ShortestPath::getPaths)
                .flatMap(Collection::stream) // List<List<PearlRelationship>>
                .flatMap(Collection::stream) // List<PearlRelationship>
                .forEach(this::createGraphRelationship);
    }

    private void createGraphRelationship(PearlRelationship relationship) {
        final GraphNode target = addOrGetNode(relationship.getTarget());
        final GraphNode source = addOrGetNode(relationship.getSource());
        addRelationship(source, target, relationship);
    }

    private GraphNode addOrGetNode(Pearl node) {
        final GraphNode graphNode = getGraphNode(node);
        return graphNode == null ? createGraphNode(node) : graphNode;
    }

    private GraphNode getGraphNode(Pearl node) {
        final Optional<GraphNode> first = nodes.stream().filter(graphNode -> graphNode.getPearl().equals(node)).findFirst();
        return first.isPresent() ? first.get() : null;
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
