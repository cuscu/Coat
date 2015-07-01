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
    private int distanceToPhenotype = -1;
    private Map<String, Object> properties = new HashMap<>();

    private Map<Pearl, List<PearlRelationship>> relationships = new HashMap<>();
    private Double score;

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
        return String.format("[%s] %d, %s", type, distanceToPhenotype, name);
    }

    public int getDistanceToPhenotype() {
        return distanceToPhenotype;
    }

    public void setDistanceToPhenotype(int distanceToPhenotype) {
        this.distanceToPhenotype = distanceToPhenotype;
    }


//    public List<PearlRelationship> getOutRelationships() {
//        return outRelationships;
//    }

//    public PearlRelationship createRelationshipTo(Pearl target) {
//        final PearlRelationship relationship = new PearlRelationship(this, target);
//        this.outRelationships.add(relationship);
//        target.inRelationships.add(relationship);
//        return relationship;
//    }

//    public List<PearlRelationship> getInRelationships() {
//        return inRelationships;
//    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public Map<Pearl, List<PearlRelationship>> getRelationships() {
        return relationships;
    }

    public PearlRelationship createRelationshipTo(Pearl target) {
        final PearlRelationship relationship = new PearlRelationship(this, target);
        List<PearlRelationship> relationshipsTo = this.relationships.get(target);
        if (relationshipsTo == null) {
            relationshipsTo = new ArrayList<>();
            relationships.put(target, relationshipsTo);
        }
        relationshipsTo.add(relationship);
        return relationship;
    }

    public void addRelationship(Pearl pearl, PearlRelationship relationship) {
        List<PearlRelationship> rs = this.relationships.get(pearl);
        if (rs == null) {
            rs = new ArrayList<>();
            relationships.put(pearl, rs);
        }
        if (!rs.contains(relationship)) rs.add(relationship);
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public double getScore() {
        return score;
    }
}
