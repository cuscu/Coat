package coat.model.poirot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class Pearl {
    private boolean leaf = true;
    private String type;
    private String name;
    private int weight = -1;
    private List<PearlRelationship> outRelationships = new ArrayList<>();
    private List<PearlRelationship> inRelationships = new ArrayList<>();
    private Map<String, Object> properties = new HashMap<>();

    public Pearl(String name, String type) {
        this.name = name;
        this.type = type;
        this.name = name;
    }

    public boolean isLeaf() {
        return leaf;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public void setLeaf(boolean leaf) {
        this.leaf = leaf;
    }

    @Override
    public String toString() {
        return String.format("[%s] %d, %s", type, weight, name);
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }


    public List<PearlRelationship> getOutRelationships() {
        return outRelationships;
    }

    public PearlRelationship createRelationshipTo(Pearl target) {
        final PearlRelationship relationship = new PearlRelationship(this, target);
        this.outRelationships.add(relationship);
        target.inRelationships.add(relationship);
        return relationship;
    }

    public List<PearlRelationship> getInRelationships() {
        return inRelationships;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }
}
