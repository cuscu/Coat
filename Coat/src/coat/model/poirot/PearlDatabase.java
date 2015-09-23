package coat.model.poirot;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Graph based database. Pearls are connected by PearlRelationship. Any two Pearls can be connected. Pearls are
 * classified by type. One Pearl has one type.
 *
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class PearlDatabase {

    private HashMap<String, HashMap<String, Pearl>> types = new HashMap<>();

    /**
     * Creates a new empty PearlDatabase
     */
    public PearlDatabase() {

    }

    /**
     * Gets the number of Pearl of the given type in the PearlDatabase
     *
     * @param type type to count
     * @return number of pearls of the given type, 0 if the type is not in the PearlDatabase
     */
    public int numberOfPearls(String type) {
        return types.containsKey(type) ? types.get(type).size() : 0;
    }

    /**
     * Returs true if there is a Pearl in the database with the specified name and type.
     *
     * @param name name of the Pearl
     * @param type type of the Pearl
     * @return true if a Pearl with the same name and type exists
     */
    public boolean contains(String name, String type) {
        final HashMap<String, Pearl> typeMap = types.get(type);
        return typeMap != null && typeMap.containsKey(name);
    }

    /**
     * Gets a specific Pearl by giving its type and name. If the Pearl is not in the PearlDatabase returns null.
     *
     * @param name name of the pearl
     * @param type type of the pearl
     * @return the Pearl or null
     */
    public Pearl getPearl(String name, String type) {
        final HashMap<String, Pearl> typeMap = types.get(type);
        return typeMap == null ? null : typeMap.get(name);
    }

    /**
     * Gets a Pearl from the PearlDatabase by giving its type and name. If the Pearl does not exist, a new one is
     * created and returned.
     *
     * @param name name of the pearl
     * @param type type of the pearl
     * @return the Pearl with the given name and type
     */
    public synchronized Pearl getOrCreate(String name, String type) {
        final HashMap<String, Pearl> typeMap = getTypeMap(type);
        typeMap.putIfAbsent(name, new Pearl(name, type));
        return typeMap.get(name);
    }

    /**
     * Gets the map of the given type
     *
     * @param type the type
     * @return a map
     */
    private HashMap<String, Pearl> getTypeMap(String type) {
        types.putIfAbsent(type, new HashMap<>());
        return types.get(type);
    }

    /**
     * Get a list of Pearls with the type of the argument.
     *
     * @param type type of the Pearls
     * @return the list of Pearls, or an empty list
     */
    public synchronized List<Pearl> getPearls(String type) {
        final HashMap<String, Pearl> pearls = types.get(type);
        return (pearls == null) ? Collections.emptyList() : pearls.entrySet().stream().map(Map.Entry::getValue).collect(Collectors.toList());
    }

    /**
     * Delete this Pearl and its relationships from the PearlDatabase.
     *
     * @param pearl the Pearl to remove
     */
    public void remove(Pearl pearl) {
        removeInputRelationships(pearl);
        removeOutputRelationships(pearl);
        removePearl(pearl);
    }

    private void removeInputRelationships(Pearl pearl) {
        pearl.getRelationships().keySet().stream()
                .filter(key -> !key.equals(pearl))
                .forEach(key -> key.getRelationships().remove(pearl));
    }

    private void removeOutputRelationships(Pearl pearl) {
        pearl.getRelationships().clear();
    }

    private void removePearl(Pearl pearl) {
        getTypeMap(pearl.getType()).remove(pearl.getName());
    }

    /**
     * Add this Pearl to the PearlDatabase.
     *
     * @param pearl the pearl
     */
    public void add(Pearl pearl) {
        getTypeMap(pearl.getType()).put(pearl.getName(), pearl);
    }
}
