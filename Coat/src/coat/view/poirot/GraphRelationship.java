package coat.view.poirot;

import coat.model.poirot.PearlRelationship;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class GraphRelationship {

    private List<PearlRelationship> relationships = new ArrayList<>();
    private Vector position = new Vector();
    private boolean mouseOver;
    private boolean selected;


    public List<PearlRelationship> getRelationships() {
        return relationships;
    }

    public Vector getPosition() {
        return position;
    }

    public void setMouseOver(boolean mouseOver) {
        this.mouseOver = mouseOver;
    }

    public boolean isMouseOver() {
        return mouseOver;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isSelected() {
        return selected;
    }
}
