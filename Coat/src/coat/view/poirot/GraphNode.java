package coat.view.poirot;

import coat.model.poirot.Pearl;

/**
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class GraphNode {

//    private List<GraphRelationship> relationships = new ArrayList<>();

    private final Pearl pearl;

    private Vector position = new Vector();
    private Vector direction = new Vector();
    private boolean selected;
    private boolean mouseMoving;
    private double speed = 0;
    private boolean mouseOver;

    public GraphNode(Pearl pearl) {
        this.pearl = pearl;
    }

    public Pearl getPearl() {
        return pearl;
    }

//    public List<GraphRelationship> getRelationships() {
//        return relationships;
//    }

    public double distance(GraphNode node) {
        return position.distance(node.position);
    }

    public void push(Vector vector) {
        direction.moveX(vector.getX());
        direction.moveY(vector.getY());
    }

    public Vector getPosition() {
        return position;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setMouseMoving(boolean mouseMoving) {
        this.mouseMoving = mouseMoving;
    }

    public boolean isMouseMoving() {
        return mouseMoving;
    }

    public Vector getDirection() {
        return direction;
    }

    public void setMouseOver(boolean mouseOver) {
        this.mouseOver = mouseOver;
    }

    public boolean isMouseOver() {
        return mouseOver;
    }
}
