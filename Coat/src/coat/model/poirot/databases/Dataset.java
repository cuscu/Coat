package coat.model.poirot.databases;

import java.util.*;
import java.util.stream.Collectors;

/**
 * A Dataset stores a list of Instances with the same structure. Each Instance stores a list of Objects, its fields.
 * All Instances in the same Dataset have the same number of fields. Fields can be accessed by its index or by its
 * column name, if columnNames have been set.
 *
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class Dataset {

    private final List<Instance> instances = new ArrayList<>();
    private final List<String> columnNames = new ArrayList<>();
    /**
     * Indexes allow fast access to fields. To access an index, just take it from <code>indexes.get(column)</code>. As
     * indexes are not automatically created, first check if index is available: <code>hasIndex(column)</code>. If not,
     * create it first: <code>createIndex(column)</code>.
     */
    private final Map<Integer, Map<Object, List<Instance>>> indexes = new HashMap<>();
    private final Map<String, Integer> columnPositions = new HashMap<>();

    public void addInstance(Object[] fields) {
        final Instance instance = new Instance(this, fields);
        instances.add(instance);
        addToIndex(instance);
    }

    private void addToIndex(Instance instance) {
        indexes.forEach((position, index) -> {
            final Object key = instance.getField(position);
            index.putIfAbsent(key, new ArrayList<>());
            final List<Instance> instances = index.get(key);
            if (!instances.contains(instance)) instances.add(instance);
        });
    }

    public void createIndex(int position) {
        indexes.putIfAbsent(position, instances.stream().collect(Collectors.groupingBy(instance -> instance.getField(position))));
    }

    public List<Instance> getInstances() {
        return instances;
    }

    /**
     * OPTIONAL. Set the name of the columns. This allows user to access values by its column name.
     *
     * @param columnNames a list containing the name of each column.
     */
    public void setColumnNames(List<String> columnNames) {
        this.columnNames.clear();
        this.columnNames.addAll(columnNames);
        createColumnNamesIndex(columnNames);
    }

    private void createColumnNamesIndex(List<String> columnNames) {
        for (int i = 0; i < columnNames.size(); i++) columnPositions.put(columnNames.get(i), i);
    }

    public List<String> getColumnNames() {
        return columnNames;
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
        System.out.println(columnNames);
        for (Instance instance : instances) {
            for (int i = 0; i < columnNames.size(); i++) System.out.print(instance.getField(i) + " ");
            System.out.println();
        }
    }

    private boolean hasIndex(int column) {
        return indexes.containsKey(column);
    }

}
