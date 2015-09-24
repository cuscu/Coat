package coat.model.poirot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
            if (other.getType().equals("phenotype") && other.isActive()) {
                for (PearlRelationship relationship : relationships) {
                    final List<PearlRelationship> path = Arrays.asList(relationship);
                    paths.add(path);
                }
            } else if (other.getType().equals("gene") && other.getDistanceToPhenotype() < distance) {
                final List<List<PearlRelationship>> subPaths = getPaths(other);
                for (List<PearlRelationship> subPath : subPaths) {
                    for (PearlRelationship relationship : relationships) {
                        final List<PearlRelationship> path = new ArrayList<>(subPath);
                        path.add(0, relationship);
                        paths.add(path);
                    }
                }
            }
        });
        return paths;
    }


}
