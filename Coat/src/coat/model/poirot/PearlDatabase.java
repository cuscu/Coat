package coat.model.poirot;

import javafx.collections.ObservableList;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class PearlDatabase {

    private HashMap<String, HashMap<String, Pearl>> types = new HashMap<>();

    private final List<String> blackList = Arrays.asList("UBC");

    public int nodes() {
        final int[] count = {0};
        types.forEach((type, typeMap) -> count[0] += typeMap.size());
        return count[0];
    }

    public int nodes(String type) {
        return types.get(type).size();
    }

    public boolean contains(String name, String type) {
        final HashMap<String, Pearl> typeMap = types.get(type);
        return typeMap != null && typeMap.containsKey(name);
    }

    public String toJson() {
        final String[] nodes = {"\"nodes\":["};
        final String[] edges = {"\"edges\":["};
        types.forEach((type, typeMap) -> typeMap.forEach((name, pearl) -> {
            final int cluster = pearl.getWeight() + 1;
            nodes[0] += "{\"id\": \"" + name + "\", \"type\":\"" + type + "\", \"cluster\": " + cluster + "},\n";
            pearl.getOutRelationships().forEach(pearlRelationship -> {
                final String target = pearlRelationship.getTarget().getName();
                final String caption = String.valueOf(pearlRelationship.getProperty("count"));
                edges[0] += "{\"source\":\"" + name + "\", \"target\": \"" + target + "\", \"caption\": \"" + caption + "\"},\n";
            });
        }));
        nodes[0] = nodes[0].substring(0, nodes[0].length() - 2);
        edges[0] = edges[0].substring(0, edges[0].length() - 2);
        return "{\n" + nodes[0] + "],\n" + edges[0] + "\n]}";

    }

    @Override
    public String toString() {
        return String.format("Nodes: %d, edges: %d", nodes(), edges());
    }

    private int edges() {
        final int[] edges = {0};
        types.forEach((type, typeMap) -> typeMap.forEach((s, pearl) -> edges[0] += pearl.getOutRelationships().size()));
        return edges[0];
    }

    public Pearl getPearl(String name, String type) {
        final HashMap<String, Pearl> typeMap = types.get(type);
        return typeMap == null ? null : typeMap.get(name);
    }

    public Pearl getOrCreate(String name, String type) {
        if (blackList.contains(name)) return null;
        HashMap<String, Pearl> typeMap = types.get(type);
        Pearl pearl;
        if (typeMap != null) {
            pearl = typeMap.get(name);
            if (pearl == null) {
                pearl = new Pearl(name, type);
                typeMap.put(name, pearl);
            }
            return pearl;
        } else {
            typeMap = new HashMap<>();
            types.put(type, typeMap);
            pearl = new Pearl(name, type);
            typeMap.put(name, pearl);
            return pearl;
        }
    }

    public List<Pearl> getPearls(String type) {
        final HashMap<String, Pearl> pearls = types.get(type);
        return (pearls == null) ? null : pearls.entrySet().stream().map(Map.Entry::getValue).collect(Collectors.toList());
    }

    public void remove(Pearl pearl) {
        final HashMap<String, Pearl> typeMap = types.get(pearl.getType());
        if (typeMap != null) {
            for (PearlRelationship relationship : pearl.getInRelationships())
                relationship.getSource().getOutRelationships().remove(relationship);
            for (PearlRelationship relationship : pearl.getOutRelationships())
                relationship.getTarget().getInRelationships().remove(relationship);
            pearl.getOutRelationships().clear();
            pearl.getInRelationships().clear();
            typeMap.remove(pearl.getName());
        }
    }

    public void subgraph(File graphFile, ObservableList<Pearl> genes) {
        List<Pearl> pearlNodes = new ArrayList<>();
        List<PearlRelationship> relationshipEdges = new ArrayList<>();

        genes.forEach(gene -> {
            List<List<PearlRelationship>> paths = getShortestPaths(gene);
            paths.forEach(path -> path.forEach(relationship -> {
                if (!pearlNodes.contains(relationship.getSource())) pearlNodes.add(relationship.getSource());
                if (!pearlNodes.contains(relationship.getTarget())) pearlNodes.add(relationship.getTarget());
                if (!relationshipEdges.contains(relationship)) relationshipEdges.add(relationship);
            }));
        });
        final String[] nodes = {"\"nodes\":["};
        pearlNodes.forEach(pearl -> nodes[0] += String.format("{\"id\": \"%s\",\"type\": \"%s\", \"cluster\": %d},",
                pearl.getName(), pearl.getType(), pearl.getWeight() + 1));
        final String[] edges = {"\"edges\":["};
        relationshipEdges.forEach(relationship -> edges[0] += String.format("{\"source\": \"%s\", \"target\": \"%s\", \"interactions\": %d, \"caption\": \"%s\"},",
                relationship.getSource().getName(), relationship.getTarget().getName(), (int) relationship.getProperty("count"), getCaption(relationship)));
        nodes[0] = nodes[0].substring(0, nodes[0].length() - 1);
        edges[0] = edges[0].substring(0, edges[0].length() - 1);
        final String json = "{\n" + nodes[0] + "],\n" + edges[0] + "\n]}";
        save(graphFile, json);
    }

    private String getCaption(PearlRelationship relationship) {
        Map<String, Integer> map = new HashMap<>();
        List<String> types = (List<String>) relationship.getProperty("types");
        return types == null ? "" : getCounts(map, types);
    }

    private String getCounts(Map<String, Integer> map, List<String> types) {
        types.forEach(s -> {
            int count = map.getOrDefault(s, 0);
            map.put(s, count + 1);
        });
        final String[] ret = {"["};
        map.forEach((s, integer) -> ret[0] += integer + " " + s +",");
        ret[0] = ret[0].substring(0, ret[0].length() - 1) + "]";
        return ret[0];
    }

    private List<List<PearlRelationship>> getShortestPaths(Pearl pearl) {
        return pearl.getType().equals("phenotype") ? new ArrayList<>(Arrays.asList(new ArrayList<>())) : getSubPaths(pearl);
    }

    private List<List<PearlRelationship>> getSubPaths(Pearl pearl) {
        List<List<PearlRelationship>> paths = new ArrayList<>();
        int min = getMinWeight(pearl);
        genearateOutSubPaths(pearl, paths, min);
        genearateInSubPaths(pearl, paths, min);
        return paths;
    }

    private int getMinWeight(Pearl gene) {
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

    private void genearateOutSubPaths(Pearl pearl, List<List<PearlRelationship>> paths, int min) {
        pearl.getOutRelationships().stream().
                filter(relationship -> relationship.getTarget().getWeight() == min).
                forEach(relationship -> addOutSubPaths(paths, relationship));
    }

    private void genearateInSubPaths(Pearl pearl, List<List<PearlRelationship>> paths, int min) {
        pearl.getInRelationships().stream().
                filter(relationship -> relationship.getSource().getWeight() == min).
                forEach(relationship -> addInSubPaths(paths, relationship));
    }

    private void addOutSubPaths(List<List<PearlRelationship>> paths, PearlRelationship relationship) {
        List<List<PearlRelationship>> subPaths = getShortestPaths(relationship.getTarget());
        subPaths.forEach(path -> {
            path.add(0, relationship);
            paths.add(path);
        });
    }

    private void addInSubPaths(List<List<PearlRelationship>> paths, PearlRelationship relationship) {
        List<List<PearlRelationship>> subPaths = getShortestPaths(relationship.getSource());
        subPaths.forEach(path -> {
            path.add(0, relationship);
            paths.add(path);
        });
    }

    private void save(File graphFile, String json) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(graphFile))) {
            writer.write(json);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
