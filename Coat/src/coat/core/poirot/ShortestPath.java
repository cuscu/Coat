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
package coat.core.poirot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Finds the shortest paths from the given Pearl to all active phenotypes, if reachable.
 *
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class ShortestPath {

    /**
     * Finds the shortest paths from the given Pearl to all active phenotypes, if reachable.
     *
     * @param pearl should be "gene"
     * @return a list with all the minimum paths to active phenotypes
     */
    public static List<List<PearlRelationship>> getPaths(Pearl pearl) {
        if (pearl == null) return new ArrayList<>();
        final int distance = pearl.getDistanceToPhenotype();
        final List<List<PearlRelationship>> paths = new ArrayList<>();
        pearl.getRelationships().forEach((other, relationships) -> {
            if (other.getType().equals("phenotype") && other.isActive()) paths.addAll(createSubPaths(relationships));
            else if (other.getType().equals("gene") && other.getDistanceToPhenotype() < distance) {
                final List<List<PearlRelationship>> subPaths = getPaths(other);
                subPaths.forEach(subPath -> relationships.forEach(pearlRelationship -> {
                    final List<PearlRelationship> path = new ArrayList<>(subPath);
                    path.add(0, pearlRelationship);
                    paths.add(path);
                }));
            }
        });
        return paths;
    }

    private static List<List<PearlRelationship>> createSubPaths(List<PearlRelationship> relationships) {
        return relationships.stream()
                .map(pearlRelationship -> Arrays.asList(pearlRelationship))
                .collect(Collectors.toList());
    }


}
