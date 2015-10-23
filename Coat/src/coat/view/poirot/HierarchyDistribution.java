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

package coat.view.poirot;

import coat.core.poirot.graph.Graph;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
class HierarchyDistribution {

    private static Graph graph;
    private static double margin;
    private static double effectiveWidth;
    private static double effectiveHeight;
    private static double maxWeight;

    public static synchronized void distribute(Graph graph, double margin, double effectiveWidth, double effectiveHeight, double maxWeight) {
        HierarchyDistribution.graph = graph;
        HierarchyDistribution.margin = margin;
        HierarchyDistribution.effectiveWidth = effectiveWidth;
        HierarchyDistribution.effectiveHeight = effectiveHeight;
        HierarchyDistribution.maxWeight = maxWeight;
        hierarchyDistribution();
    }

    private static void hierarchyDistribution() {
        final List<GraphNode> rootNodes = graph.getNodes().stream()
                .filter(graphNode -> graphNode.getPearl().getDistanceToPhenotype() == 0).collect(Collectors.toList());
        final double y = margin;
        final double nodeWidth = effectiveWidth / rootNodes.size();
        for (int i = 0; i < rootNodes.size(); i++) {
            final double x = margin + i * nodeWidth + 0.5 * nodeWidth;
            rootNodes.get(i).getPosition().set(x, y);
        }
        setSubHierarchy(rootNodes, 1);

    }

    private static void setSubHierarchy(List<GraphNode> parentNodes, int weight) {
        if (weight > maxWeight) return;
        final List<GraphNode> subNodes = graph.getNodes().stream().
                filter(graphNode -> graphNode.getPearl().getDistanceToPhenotype() == weight).collect(Collectors.toList());
        final double y = margin + weight / maxWeight * effectiveHeight;
        final double nodeWidth = effectiveWidth / subNodes.size();
        int j = 0;
        final List<GraphNode> orderNodes = new ArrayList<>();
        for (GraphNode parentNode : parentNodes) {
            final List<GraphNode> children = extractNodeChildren(subNodes, parentNode);
            for (GraphNode child : children) {
                if (!orderNodes.contains(child)) {
                    double x = margin + j * nodeWidth + 0.5 * nodeWidth;
                    child.getPosition().set(x, y);
                    orderNodes.add(child);
                    j++;
                }
            }
        }
        setSubHierarchy(orderNodes, weight + 1);
    }

    private static List<GraphNode> extractNodeChildren(List<GraphNode> subNodes, GraphNode parentNode) {
        return subNodes.stream()
                .filter(graphNode -> graph.getRelationships().containsKey(new NodePairKey(parentNode, graphNode))).collect(Collectors.toList());
    }
}
