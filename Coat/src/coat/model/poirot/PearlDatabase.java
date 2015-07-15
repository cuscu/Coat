package coat.model.poirot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class PearlDatabase {

    private HashMap<String, HashMap<String, Pearl>> types = new HashMap<>();

    public int pearls() {
        final int[] count = {0};
        types.forEach((type, typeMap) -> count[0] += typeMap.size());
        return count[0];
    }

    public int pearls(String type) {
        return types.get(type).size();
    }

    public boolean contains(String name, String type) {
        final HashMap<String, Pearl> typeMap = types.get(type);
        return typeMap != null && typeMap.containsKey(name);
    }

    public Pearl getPearl(String name, String type) {
        final HashMap<String, Pearl> typeMap = types.get(type);
        return typeMap == null ? null : typeMap.get(name);
    }

    public synchronized Pearl getOrCreate(String name, String type) {
        HashMap<String, Pearl> typeMap = types.get(type);
        if (typeMap != null) {
            Pearl pearl = typeMap.get(name);
            if (pearl == null) {
                pearl = new Pearl(name, type);
                typeMap.put(name, pearl);
            }
            return pearl;
        } else {
            typeMap = new HashMap<>();
            types.put(type, typeMap);
            Pearl pearl = new Pearl(name, type);
            typeMap.put(name, pearl);
            return pearl;
        }
    }

    public synchronized List<Pearl> getPearls(String type) {
        final HashMap<String, Pearl> pearls = types.get(type);
        return (pearls == null) ? null : pearls.entrySet().stream().map(Map.Entry::getValue).collect(Collectors.toList());
    }

    public void remove(Pearl pearl) {
        pearl.getRelationships().keySet().stream().filter(key -> !key.equals(pearl)).forEach(key -> key.getRelationships().remove(pearl));
        pearl.getRelationships().clear();
        final HashMap<String, Pearl> typeMap = types.get(pearl.getType());
        if (typeMap != null) typeMap.remove(pearl.getName());
    }

    public void add(Pearl pearl) {
        HashMap<String, Pearl> typeMap = types.get(pearl.getType());
        if (typeMap == null) {
            typeMap = new HashMap<>();
            types.put(pearl.getType(), typeMap);
        }
        typeMap.put(pearl.getName(), pearl);
    }
}
