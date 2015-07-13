package coat.model.poirot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class ShortestPath {

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
