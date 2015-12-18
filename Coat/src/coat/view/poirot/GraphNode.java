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

package coat.view.poirot;

import coat.core.poirot.Pearl;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;

/**
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class GraphNode implements Selectable{

//    private List<GraphRelationship> relationships = new ArrayList<>();

    private final Pearl pearl;

    private Vector position = new Vector();
    private Vector direction = new Vector();
    private Property<Boolean> selected = new SimpleObjectProperty<>(false);
    private boolean mouseMoving;
    private boolean mouseOver;

    public GraphNode(Pearl pearl) {
        this.pearl = pearl;
    }

    public Pearl getPearl() {
        return pearl;
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
        this.selected.setValue(selected);
    }

    @Override
    public Property<Boolean> selectedProperty() {
        return selected;
    }

    public boolean isSelected() {
        return selected.getValue();
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
