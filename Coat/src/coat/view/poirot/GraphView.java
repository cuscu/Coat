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

import coat.Coat;
import coat.core.poirot.Pearl;
import coat.core.poirot.PearlRelationship;
import coat.core.poirot.graph.Graph;
import coat.core.poirot.graph.GraphEvaluator;
import coat.core.vcf.Variant;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.FontSmoothingType;
import javafx.scene.text.TextAlignment;

import java.util.*;

/**
 * This is the graph view. Input is a list of Pearls, output are selectedPearl and selectedRelationship. Pearls (logical
 * graph) are stored on a <code>Graph</code> object.
 *
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
class GraphView extends Canvas {

    private static final int RELATIONSHIP_RADIUS = 10;
    private final GraphicsContext screen;

    private final Graph graph = new Graph();

    private final Property<Pearl> selectedPearl = new SimpleObjectProperty<>();

    private final Property<GraphRelationship> selectedRelationship = new SimpleObjectProperty<>();
    private Timer timer;


    private double diameter;
    private final DoubleProperty radiusProperty = new SimpleDoubleProperty();
    private double margin;
    private double maxWeight;
    private double effectiveWidth;
    private double effectiveHeight;
    private double maxSpeed;
    private double nodeDistance;

    private long startTime;
    private Vector lastMousePosition = new Vector();
    private final static long GLOWING_TIME = 1000;
    private final static long HALF_GLOWING_TIME = GLOWING_TIME / 2;
    private final static double OPACITY_FACTOR = 0.8 / HALF_GLOWING_TIME;

    private final static double MAX_RADIUS = 40;
    private final static double MIN_RADIUS = 10;

    private final Random random = new Random();
    private GraphNode movingNode;

    /**
     * Creates a Canvas that renders a GraphView.
     */
    public GraphView() {
        screen = getGraphicsContext2D();
        widthProperty().addListener((observable, oldValue, newValue) -> effectiveWidth = getWidth() - 2 * margin);
        heightProperty().addListener((observable, oldValue, newValue) -> effectiveHeight = getHeight() - 2 * margin);
        setMouseEvents();
        screen.setTextAlign(TextAlignment.CENTER);
        screen.setTextBaseline(VPos.CENTER);
        screen.setFontSmoothingType(FontSmoothingType.GRAY);
        setWidth(400);
        setHeight(400);
        Platform.setImplicitExit(true);
        radiusProperty.addListener((observable, oldValue, newRadius) -> setRadius(newRadius.doubleValue()));

    }

    /**
     * Get the current selected Pearl property. It is possible to add listener to this property.
     *
     * @return the selected pearl property
     */
    public Property<Pearl> getSelectedPearlProperty() {
        return selectedPearl;
    }

    /**
     * Get the current selected Relationship property. You can add listeners.
     *
     * @return the current selected relationship property
     */
    public Property<GraphRelationship> getSelectedRelationship() {
        return selectedRelationship;
    }

    private void setMouseEvents() {
        setOnMouseClicked(this::clicked);
        setOnMousePressed(this::startMove);
        setOnMouseReleased(this::endMove);
        setOnMouseDragged(this::mouseDragging);
        setOnMouseMoved(this::mouseMoving);
        setOnScroll(this::zoom);
    }

    /**
     * Mouse has been pressed and released
     *
     * @param event the mouse event
     */
    private void clicked(MouseEvent event) {
        if (movingNode == null) {
            final Vector click = new Vector(event.getX(), event.getY());
            selectNode(click);
            selectRelationship(click);
            if (selectedRelationship.getValue() == null && selectedPearl.getValue() == null)
                lastMousePosition.set(event.getX(), event.getY());
        }
    }

    private void selectRelationship(Vector clickPosition) {
        selectedRelationship.setValue(null);
        for (Map.Entry<NodePairKey, GraphRelationship> entry : graph.getRelationships().entrySet()) {
            entry.getValue().setSelected(clickPosition.distance(entry.getValue().getPosition()) < RELATIONSHIP_RADIUS);
            if (entry.getValue().isSelected()) selectedRelationship.setValue(entry.getValue());
        }
    }

    private void selectNode(Vector clickPosition) {
        selectedPearl.setValue(null);
        for (GraphNode node : graph.getNodes()) {
            node.setSelected(clickPosition.distance(node.getPosition()) < radiusProperty.getValue());
            if (node.isSelected()) selectedPearl.setValue(node.getPearl());
        }
    }

    /**
     * Mouse has been pressed. If a node is under mouse, it starts moving.
     *
     * @param event the mouse event
     */
    private void startMove(MouseEvent event) {
        lastMousePosition = new Vector(event.getX(), event.getY());
        for (GraphNode node : graph.getNodes())
            if (node.getPosition().distance(lastMousePosition) < radiusProperty.getValue()) {
                movingNode = node;
                node.setMouseMoving(true);
                break;
            }
    }

    /**
     * If a node has been previously pressed, and the mouse is still pressed, the node must go with the mouse.
     *
     * @param event mouse event
     */
    private void mouseDragging(MouseEvent event) {
        if (movingNode != null) movingNode.getPosition().set(event.getX(), event.getY());
        else {
            final Vector mousePosition = new Vector(event.getX(), event.getY());
            final Vector direction = new Vector(lastMousePosition, mousePosition);
            synchronized (graph.getNodes()) {
                for (GraphNode node : graph.getNodes()) node.getPosition().add(direction);
            }
            lastMousePosition = mousePosition;
        }
    }

    /**
     * The mouse has been released. If there were a node moving, it is released.
     *
     * @param event the mouse event
     */
    private void endMove(MouseEvent event) {
        if (movingNode != null) {
            movingNode.setMouseMoving(false);
            movingNode = null;
        }
    }

    /**
     * Whe the user scrolls the mouse, the graph is zoomed (x1.25 or x0.8).
     *
     * @param event the mouse event
     */
    private void zoom(ScrollEvent event) {
        final double scale = event.getDeltaY() > 0 ? 1.25 : 0.8;
        radiusProperty.setValue(radiusProperty.get() * scale);
        final Vector mouse = new Vector(event.getX(), event.getY());
        synchronized (graph.getNodes()) {
            graph.getNodes().forEach(graphNode -> {
                graphNode.getPosition().substract(mouse);
                graphNode.getPosition().scale(scale);
                graphNode.getPosition().add(mouse);
            });
        }
    }

    /**
     * When the mouse is freely moved, nodes and relationships under it  will light.
     *
     * @param event the mouse event
     */
    private void mouseMoving(MouseEvent event) {
        final Vector mousePosition = new Vector(event.getX(), event.getY());
        setMouseOverNodes(mousePosition);
        setMouseOverRelationships(mousePosition);
    }

    private void setMouseOverNodes(Vector mousePosition) {
        synchronized (graph.getNodes()) {
            graph.getNodes().forEach(node -> node.setMouseOver(node.getPosition().distance(mousePosition) < radiusProperty.get()));
        }
    }

    private void setMouseOverRelationships(Vector mousePosition) {
        synchronized (graph.getRelationships()) {
            graph.getRelationships().forEach((nodePairKey, graphRelationship) -> graphRelationship.setMouseOver(graphRelationship.getPosition().distance(mousePosition) < RELATIONSHIP_RADIUS));
        }
    }

    /**
     * Changes the size of the node by setting the radius. This will also affect the distance between nodes and the size
     * of margins. radius will never be less than MIN_RADIUS or more than MAX_RADIUS. In those cases, radius is cropped.
     *
     * @param radius the new radius
     */
    private void setRadius(double radius) {
        if (radius > MAX_RADIUS) this.radiusProperty.setValue(MAX_RADIUS);
        else if (radius < MIN_RADIUS) this.radiusProperty.setValue(MIN_RADIUS);
        else {
            diameter = 2 * radius;
            maxSpeed = 0.25 * radius;
            margin = 1.1 * radius;
            nodeDistance = radius;
            // Nodes will share half of the area
            effectiveWidth = getWidth() - 2 * margin;
            effectiveHeight = getHeight() - 2 * margin;
        }
    }

    /**
     * Set the list of root genes. <code>Graph</code> will call ShortestPath to get the paths to the phenotypes. Then
     * <code>GraphView</code> will apply a hierarchy distribution to the resulting nodes.
     *
     * @param rootGenes the list of root genes
     */
    public void setRootGenes(List<Pearl> rootGenes) {
        graph.setOriginNodes(rootGenes);
        maxWeight = graph.getNodes().stream().map(GraphNode::getPearl).map(Pearl::getDistanceToPhenotype).max(Integer::compare).orElse(1);
        radiusProperty.setValue(0.4 * Math.sqrt(0.25 * effectiveWidth * effectiveHeight / graph.getNodes().size()));
        HierarchyDistribution.distribute(graph, margin, effectiveWidth, effectiveHeight, maxWeight);
        startTime = System.currentTimeMillis();
        startDrawer();
    }

    /**
     * Initialize the timer that repaints the GraphView.
     */
    private void startDrawer() {
        if (timer != null) timer.cancel();
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    iterate();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 0, 50);
    }

    private void iterate() {
        if (Coat.getStage() != null && !Coat.getStage().isShowing()) timer.cancel();
        calculateNewPositions();
        updateNodePositions();
        Platform.runLater(this::paint);
    }

    private void calculateNewPositions() {
        synchronized (graph.getNodes()) {
            graph.getNodes().stream().filter(graphNode -> !graphNode.isMouseMoving()).forEach(this::avoidCollisions);
        }
    }

    private void avoidCollisions(GraphNode graphNode) {
        final double minDistance = diameter + 0.5 * nodeDistance;
        graph.getNodes().stream()
                .filter(node -> !node.equals(graphNode))
                .forEach(node -> avoidCollisionWith(graphNode, minDistance, node));
    }

    private void avoidCollisionWith(GraphNode graphNode, double minDistance, GraphNode node) {
        final double dist = node.distance(graphNode);
        if (dist < minDistance) {
            moveRandomly(graphNode);
            separate(graphNode, minDistance, node, dist);
        }
    }

    private void separate(GraphNode graphNode, double minDistance, GraphNode node, double dist) {
        final Vector vector = new Vector(graphNode.getPosition(), node.getPosition());
        vector.scale((minDistance - dist) / dist);
        node.push(vector);
    }

    /**
     * Applies a minimum random move to a node in both axis in the range [-0.5 , 0.5]. This random move allows nodes to
     * behave more natural and avoids strict horizontal or vertical movements.
     *
     * @param node the node to move slightly
     */
    private void moveRandomly(GraphNode node) {
        node.push(new Vector(random.nextDouble() - 0.5, random.nextDouble() - 0.5));
    }

    /**
     * Takes the movement of every node, limits to the maximum speed and updates its position to the target position.
     */
    private void updateNodePositions() {
        graph.getNodes().forEach(node -> {
            limitSpeed(node);
            move(node);
        });
    }

    /**
     * Limits movement to a maximum value.
     *
     * @param node node to limit
     */
    private void limitSpeed(GraphNode node) {
        if (node.getDirection().getX() > maxSpeed) node.getDirection().setX(maxSpeed);
        if (node.getDirection().getY() > maxSpeed) node.getDirection().setY(maxSpeed);
        if (node.getDirection().getX() < -maxSpeed) node.getDirection().setX(-maxSpeed);
        if (node.getDirection().getY() < -maxSpeed) node.getDirection().setY(-maxSpeed);
    }

    /**
     * Updates the position of the node.
     *
     * @param node node to move
     */
    private void move(GraphNode node) {
        node.getPosition().add(node.getDirection());
        node.getDirection().set(0, 0);
    }

    /**
     * Clears screen and paint all nodes and relationships.
     */
    private void paint() {
        screen.clearRect(0, 0, getWidth(), getHeight());
        paintRelationships();
        paintNodes();
    }

    /**
     * Paints all the relationships.
     */
    private void paintRelationships() {
        graph.getRelationships().forEach((nodePairKey, graphRelationship) -> {
            final int size = graphRelationship.getRelationships().size();
            final Vector center = getCenter(nodePairKey);
            graphRelationship.getPosition().set(center.getX(), center.getY());
            final Color baseColor = getRelationshipColor(graphRelationship);
            drawRelationshipLine(nodePairKey, size, baseColor);
            drawRelationshipCircle(graphRelationship, baseColor);
            drawRelationshipSelectionCircle(graphRelationship);
            writeText(size + "", center);
        });
    }

    /**
     * Calculates the centre of a relationship.
     *
     * @param nodePairKey nodes of the relationship
     * @return the centre
     */
    private Vector getCenter(NodePairKey nodePairKey) {
        return new Vector(
                (nodePairKey.getSource().getPosition().getX() + nodePairKey.getTarget().getPosition().getX()) * 0.5,
                (nodePairKey.getSource().getPosition().getY() + nodePairKey.getTarget().getPosition().getY()) * 0.5);
    }

    private void drawRelationshipLine(NodePairKey nodePairKey, int size, Color color) {
        screen.setStroke(color);
        screen.setLineWidth(size);
        screen.strokeLine(nodePairKey.getSource().getPosition().getX(), nodePairKey.getSource().getPosition().getY(),
                nodePairKey.getTarget().getPosition().getX(), nodePairKey.getTarget().getPosition().getY());
    }

    private void drawRelationshipCircle(GraphRelationship graphRelationship, Color color) {
        screen.setFill(color);
        screen.fillOval(graphRelationship.getPosition().getX() - RELATIONSHIP_RADIUS, graphRelationship.getPosition().getY() - RELATIONSHIP_RADIUS, RELATIONSHIP_RADIUS * 2, RELATIONSHIP_RADIUS * 2);
    }

    private Color getRelationshipColor(GraphRelationship graphRelationship) {
        final double score = graphRelationship.getRelationships().stream().
                mapToDouble(this::getRelationshipScore).max().getAsDouble();
        Color baseColor = Color.BLACK.interpolate(Color.DODGERBLUE, score);
        if (graphRelationship.isMouseOver()) baseColor = baseColor.interpolate(Color.WHITE, 0.5);
        return baseColor;
    }

    private Double getRelationshipScore(PearlRelationship relationship) {
        String type = (String) relationship.getProperties().get("type");
        if (type == null) type = (String) relationship.getProperties().get("method");
        return GraphEvaluator.RELATIONSHIP_SCORE.getOrDefault(type, 0.0);
    }

    /**
     * If the relationship is selected, draws a circle around them.
     *
     * @param graphRelationship relationship
     */
    private void drawRelationshipSelectionCircle(GraphRelationship graphRelationship) {
        if (graphRelationship.isSelected()) {
            screen.setLineWidth(4);
            screen.setStroke(new Color(1, 1, 0, getSelectionOpacity()));
            screen.strokeOval(
                    graphRelationship.getPosition().getX() - RELATIONSHIP_RADIUS,
                    graphRelationship.getPosition().getY() - RELATIONSHIP_RADIUS,
                    RELATIONSHIP_RADIUS * 2, RELATIONSHIP_RADIUS * 2);
        }
    }

    /**
     * Writes a white text in this position
     *
     * @param text     the text
     * @param position the  position
     */
    private void writeText(String text, Vector position) {
        screen.setFill(Color.WHITE);
        screen.fillText(text, position.getX(), position.getY());
    }

    /**
     * Paints all the nodes in the graph.
     */
    private void paintNodes() {
        synchronized (graph.getNodes()) {
            graph.getNodes().forEach(graphNode -> {
                drawCircle(graphNode);
                writeNodeText(graphNode);
            });
        }
    }

    private void drawCircle(GraphNode graphNode) {
        drawSelectionCircle(graphNode);
        drawNodeCircle(graphNode);
        drawConsequences(graphNode);
    }

    private void drawNodeCircle(GraphNode graphNode) {
        Color color = getFillColor(graphNode);
        if (graphNode.isMouseOver()) color = Color.WHITE.interpolate(color, 0.5);
        screen.setFill(color);
        screen.fillOval(graphNode.getPosition().getX() - radiusProperty.get(), graphNode.getPosition().getY() - radiusProperty.get(), diameter, diameter);
    }

    /**
     * Calculates the fill color of a node
     *
     * @param graphNode the node to paint
     */
    private Color getFillColor(GraphNode graphNode) {
        switch (graphNode.getPearl().getType()) {
            case DISEASE:
                return Color.ORANGE;
            case EXPRESSION:
                return Color.DARKORANGE;
            case GENE:
                return new Color(1.0 - graphNode.getPearl().getDistanceToPhenotype() / maxWeight, 0, 0, 1);
            default:
                return Color.GRAY;
        }
    }

    /**
     * Paints the points in the nodes that indicate the consequences of the gene.
     *
     * @param graphNode the node
     */
    private void drawConsequences(GraphNode graphNode) {
        final List<Variant> variants = (List<Variant>) graphNode.getPearl().getProperties().get("variants");
        if (variants != null) {
            final List<String> consequences = getConsequences(variants);
            final double VARIANT_RADIUS = radiusProperty.get() * 0.15;
            final double VARIANT_DIAMETER = 2 * VARIANT_RADIUS;
            for (int i = 0; i < consequences.size(); i++) {
                final double angle = 6.28318 * i / consequences.size() + 1.570795; // must be radians
                final double x = graphNode.getPosition().getX() + Math.cos(angle) * (radiusProperty.get() - VARIANT_DIAMETER) - VARIANT_RADIUS;
                final double y = graphNode.getPosition().getY() - Math.sin(angle) * (radiusProperty.get() - VARIANT_DIAMETER) - VARIANT_RADIUS;
                final double score = GraphEvaluator.CONSEQUENCE_SCORE.getOrDefault(consequences.get(i), 0.0);
                Color color = Color.WHITE.interpolate(Color.RED, score);
                screen.setFill(color);
                screen.fillOval(x, y, VARIANT_DIAMETER, VARIANT_DIAMETER);
            }
        }
    }

    /**
     * If the node is selected, paints a yellow circle around it.
     *
     * @param graphNode the node
     */
    private void drawSelectionCircle(GraphNode graphNode) {
        if (graphNode.isSelected()) {
            screen.setStroke(new Color(1, 1, 0, getSelectionOpacity()));
            screen.setLineWidth(8);
            screen.strokeOval(graphNode.getPosition().getX() - radiusProperty.get(), graphNode.getPosition().getY() - radiusProperty.get(), diameter, diameter);
        }
    }

    /**
     * Maps a list of variants to a list of consequences.
     *
     * @param variants the lis t of variants
     * @return the list of consequences
     */
    private List<String> getConsequences(List<Variant> variants) {
        List<String> consequences = new ArrayList<>();
        variants.forEach(variant -> {
            final String cons = variant.getInfo("CONS");
            if (cons != null) Collections.addAll(consequences, cons.split(","));
        });
        return consequences;
    }

    /**
     * Gets the opacity of the selection based on System clock. This method allows the selection to glow.
     *
     * @return the current opacity factor
     */
    private double getSelectionOpacity() {
        final long period = (System.currentTimeMillis() - startTime) % GLOWING_TIME;
        final long step = Math.abs(HALF_GLOWING_TIME - period);
        return step * OPACITY_FACTOR + 0.2;
    }

    private void writeNodeText(GraphNode graphNode) {
        String name;
        if (graphNode.getPearl().getType() == Pearl.Type.DISEASE || graphNode.getPearl().getType() == Pearl.Type.EXPRESSION) {
            screen.setFill(Color.BLACK);
            name = simplifyName(graphNode);
        } else {
            screen.setFill(Color.WHITE);
            name = graphNode.getPearl().getName();
        }
        screen.fillText(name, graphNode.getPosition().getX(), graphNode.getPosition().getY());
        screen.fillText(String.format("%.3f",graphNode.getPearl().getScore()), graphNode.getPosition().getX(), graphNode.getPosition().getY() + 12);
    }

    private String simplifyName(GraphNode graphNode) {
        String name = graphNode.getPearl().getName();
        String[] words = name.split(" ");
        return words[0].replaceAll("\\p{Punct}", "");
    }

    private void printGraphSize() {
        System.out.println(String.format("Nodes: %d, edges: %d", graph.getNodes().size(), graph.getRelationships().size()));
        graph.getRelationships().forEach((nodePairKey, graphRelationship) -> System.out.println(nodePairKey.getKey() + " " + graphRelationship.getRelationships().size()));
    }

    public void clear() {
        graph.clearGraph();
    }
}
