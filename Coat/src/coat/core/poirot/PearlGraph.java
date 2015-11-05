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
public class PearlGraph {

    private Map<Pearl.Type, Map<String, Pearl>> typeMap = new HashMap<>();

    /**
     * Creates a new empty PearlDatabase
     */
    public PearlGraph() {

    }

    /**
     * Gets the number of Pearl of the given type in the PearlDatabase
     *
     * @param type type to count
     * @return number of pearls of the given type, 0 if the type is not in the PearlDatabase
     */
    public int numberOfPearls(Pearl.Type type) {
        return typeMap.getOrDefault(type, Collections.emptyMap()).size();
    }

    /**
     * Returns true if there is a Pearl in the database with the specified name and type.
     *
     * @param name name of the Pearl
     * @param type type of the Pearl
     * @return true if a Pearl with the same name and type exists
     */
    public boolean contains(Pearl.Type type, String name) {
        final Map<String, Pearl> map = typeMap.get(type);
        return map != null && map.containsKey(name);
    }


    /**
     * Gets a specific Pearl by giving its type and name. If the Pearl is not in the PearlDatabase returns null.
     *
     * @param name name of the pearl
     * @param type type of the pearl
     * @return the Pearl or null
     */
    public Pearl getPearl(Pearl.Type type, String name) {
        final Map<String, Pearl> map = typeMap.get(type);
        return map == null ? null : map.get(name);
    }

    /**
     * Gets a Pearl from the PearlDatabase by giving its type and name. If the Pearl does not exist, a new one is
     * created and returned.
     *
     * @param name name of the pearl
     * @param type type of the pearl
     * @return the Pearl with the given name and type
     */
    public synchronized Pearl getOrCreate(Pearl.Type type, String name) {
        typeMap.putIfAbsent(type, new HashMap<>());
        final Map<String, Pearl> map = typeMap.get(type);
        map.putIfAbsent(name, new Pearl(type, name));
        return map.get(name);
    }


    /**
     * Get a list of Pearls with the type of the argument.
     *
     * @param type type of the Pearls
     * @return the list of Pearls, or an empty list
     */
    public synchronized List<Pearl> getPearls(Pearl.Type type) {
        final Map<String, Pearl> pearls = typeMap.get(type);
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
        if (typeMap.containsKey(pearl.getType()))
            typeMap.get(pearl.getType()).remove(pearl.getName());
    }

    /**
     * Add this Pearl to the PearlDatabase.
     *
     * @param pearl the pearl
     */
    public void add(Pearl pearl) {
        typeMap.putIfAbsent(pearl.getType(), new HashMap<>());
        typeMap.get(pearl.getType()).put(pearl.getName(), pearl);
    }
}
