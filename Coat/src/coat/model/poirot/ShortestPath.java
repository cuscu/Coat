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
        List<List<PearlRelationship>> paths = new ArrayList<>();
        int min = getMinWeight(pearl);
        genearateOutSubPaths(pearl, paths, min);
        genearateInSubPaths(pearl, paths, min);
        return paths;
    }

    private static int getMinWeight(Pearl gene) {
        final int outMin = gene.getOutRelationships().isEmpty() ? Integer.MAX_VALUE :
                gene.getOutRelationships().stream().
                        map(PearlRelationship::getTarget).
                        map(Pearl::getWeight).
                        min(Integer::compare).get();
        final int inMin = gene.getInRelationships().isEmpty() ? Integer.MAX_VALUE :
                gene.getInRelationships().stream().
                        map(PearlRelationship::getSource).
                        map(Pearl::getWeight).
                        min(Integer::compare).get();
        return Math.min(inMin, outMin);
    }

    private static void genearateOutSubPaths(Pearl pearl, List<List<PearlRelationship>> paths, int min) {
        pearl.getOutRelationships().stream().
                filter(relationship -> relationship.getTarget().getWeight() == min).
                forEach(relationship -> addOutSubPaths(paths, relationship));
    }

    private static void genearateInSubPaths(Pearl pearl, List<List<PearlRelationship>> paths, int min) {
        pearl.getInRelationships().stream().
                filter(relationship -> relationship.getSource().getWeight() == min).
                forEach(relationship -> addInSubPaths(paths, relationship));
    }

    private static void addOutSubPaths(List<List<PearlRelationship>> paths, PearlRelationship relationship) {
        List<List<PearlRelationship>> subPaths = getShortestPaths(relationship.getTarget());
        subPaths.forEach(path -> {
            path.add(0, relationship);
            paths.add(path);
        });
    }

    private static void addInSubPaths(List<List<PearlRelationship>> paths, PearlRelationship relationship) {
        List<List<PearlRelationship>> subPaths = getShortestPaths(relationship.getSource());
        subPaths.forEach(path -> {
            path.add(0, relationship);
            paths.add(path);
        });
    }
}
