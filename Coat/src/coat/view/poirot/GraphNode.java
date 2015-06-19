package coat.view.poirot;

import coat.model.poirot.Pearl;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class GraphNode {

    private List<GraphRelationship> relationships = new ArrayList<>();

    private final Pearl pearl;

    private Vector position = new Vector();
    private Vector direction = new Vector();
    private boolean selected;
    private boolean mouseMoving;
    private double speed = 0;

    public GraphNode(Pearl pearl) {
        this.pearl = pearl;
    }

    public Pearl getPearl() {
        return pearl;
    }

    public List<GraphRelationship> getRelationships() {
        return relationships;
    }


    public boolean updateCoordinates(double speed, double width, double height, double margin) {
        if (direction.getX() >  speed) direction.setX(speed);
        if (direction.getY() >  speed) direction.setY(speed);
        if (direction.getX() < -speed) direction.setX(-speed);
        if (direction.getY() < -speed) direction.setY(-speed);
        position.moveX(direction.getX());
        position.moveY(direction.getY());
        if (position.getX() < margin) position.setX(margin);
        if (position.getX() > width - margin) position.setX(width - margin);
        if (position.getY() < margin) position.setY(margin);
        if (position.getY() > height - margin) position.setY(height - margin);
        System.out.print(position + " ");
        direction.set(0, 0);
        return true;
    }

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
}
