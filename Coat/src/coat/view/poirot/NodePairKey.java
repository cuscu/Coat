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
        if (source.getPearl().getName().compareTo(target.getPearl().getName()) < 0)
            key = source.getPearl().getName() + target.getPearl().getName();
        else key = target.getPearl().getName() + source.getPearl().getName();
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
