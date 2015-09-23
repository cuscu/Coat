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
        if (pearl == null)return new ArrayList<>();
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

    public static List<List<PearlRelationship>> getShortestPaths(Pearl pearl) {
        return pearl.getType().equals("phenotype") ? new ArrayList<>(Arrays.asList(new ArrayList<>())) : getSubPaths(pearl);
    }

    private static List<List<PearlRelationship>> getSubPaths(Pearl pearl) {
        final List<List<PearlRelationship>> paths = new ArrayList<>();
        int min = getMinWeight(pearl);
        pearl.getRelationships().keySet().stream().filter(neighbour -> neighbour.getDistanceToPhenotype() == min).forEach(neighbour -> {
            final List<List<PearlRelationship>> subPaths = getShortestPaths(neighbour);
            for (List<PearlRelationship> path : subPaths) {
                for (PearlRelationship relationship : pearl.getRelationships().get(neighbour)) {
                    List<PearlRelationship> newPath = new ArrayList<>(path);
                    newPath.add(0, relationship);
                    paths.add(newPath);
                }
            }
        });
        return paths;
    }

    private static int getMinWeight(Pearl gene) {
        return gene.getRelationships().keySet().stream().map(Pearl::getDistanceToPhenotype).min(Integer::compare).get();
    }


}
