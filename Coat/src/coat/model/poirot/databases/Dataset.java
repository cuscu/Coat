package coat.model.poirot.databases;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class Dataset {

    private final List<Instance> instances;
    private final List<String> headers;
    private final Map<Integer, Map<Object, List<Instance>>> indexes = new HashMap<>();
    private final Map<String, Integer> columnPositions = new HashMap<>();

    public Dataset(List<String> headers) {
        this.instances = new ArrayList<>();
        this.headers = headers;
        for (int i = 0; i < headers.size(); i++) columnPositions.put(headers.get(i), i);
    }

    public void addInstance(Object[] fields) {
        final Instance instance = new Instance(this, fields);
        instances.add(instance);
        addToIndex(instance);
    }

    private void addToIndex(Instance instance) {
        indexes.forEach((position, index) -> {
            Object key = instance.getField(position);
            List<Instance> instances = index.get(key);
            if (instances == null){
                instances = new ArrayList<>();
                index.put(key, instances);
            }
            instances.add(instance);
        });
    }

    public void createIndex(int position) {
        indexes.putIfAbsent(position, instances.stream().collect(Collectors.groupingBy(instance -> instance.getField(position))));
    }

    public List<Instance> getInstances() {
        return instances;
    }

    /**
     * Get all the instances that have the value of the column position equals to id. This method can only be used if
     * an index has been created before with <code>createIndex(position)</code>
     *
     * @param value    the value to query
     * @param position the column
     * @return a list of instances that matches value in the position column
     */
    public List<Instance> getInstances(Object value, int position) {
        return indexes.get(position).getOrDefault(value, Collections.emptyList());
    }

    public int getPositionOf(String column) {
        return columnPositions.getOrDefault(column, -1);
    }

    public void printValue() {
        System.out.println(headers);
        for (Instance instance : instances) {
            for (int i = 0; i < headers.size(); i++) System.out.print(instance.getField(i) + " ");
            System.out.println();
        }
    }

}
