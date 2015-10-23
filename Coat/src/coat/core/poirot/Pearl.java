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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Single unit of the PearlDatabase. Each Pearl is characterized by its name and type. A Pearl can contain any number of
 * relationships with any other Pearl.
 *
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class Pearl {

    private String type;
    private String name;
    private Map<String, Object> properties = new HashMap<>();

    private Map<Pearl, List<PearlRelationship>> relationships = new HashMap<>();

    private int distanceToPhenotype = -1;
    private Double score;
    private boolean active;

    public Pearl(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        return String.format("[%s] %d, %s", type, distanceToPhenotype, name);
    }

    public int getDistanceToPhenotype() {
        return distanceToPhenotype;
    }

    public void setDistanceToPhenotype(int distanceToPhenotype) {
        this.distanceToPhenotype = distanceToPhenotype;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public Map<Pearl, List<PearlRelationship>> getRelationships() {
        return relationships;
    }

    public PearlRelationship createRelationshipTo(Pearl target) {
        final PearlRelationship relationship = new PearlRelationship(this, target);
        addRelationship(target, relationship);
        target.addRelationship(this, relationship);
        return relationship;
    }

    private void addRelationship(Pearl pearl, PearlRelationship relationship) {
        relationships.putIfAbsent(pearl, new ArrayList<>());
        final List<PearlRelationship> rs = this.relationships.get(pearl);
        if (!rs.contains(relationship)) rs.add(relationship);
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public double getScore() {
        return score;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isActive() {
        return active;
    }
}
