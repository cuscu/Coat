package coat.view.poirot;

import coat.Coat;
import coat.model.poirot.Graph;
import coat.model.poirot.Pearl;
import coat.model.poirot.PoirotAnalysis;
import coat.model.vcfreader.Variant;
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
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class GraphView extends Canvas {

    public static final int RELATIONSHIP_RADIUS = 10;
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
    private final static long GLOWING_TIME = 2000;
    private final static long HALF_GLOWING_TIME = GLOWING_TIME / 2;
    private final static double OPACITY_FACTOR = 0.8 / HALF_GLOWING_TIME;

    private final static double MAX_RADIUS = 40;

    private final static double MIN_RADIUS = 15;

    private final Random random = new Random();
    private GraphNode movingNode;

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

    public Property<Pearl> getSelectedPearlProperty() {
        return selectedPearl;
    }

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

    private void startMove(MouseEvent event) {
        lastMousePosition = new Vector(event.getX(), event.getY());
        for (GraphNode node : graph.getNodes())
            if (node.getPosition().distance(lastMousePosition) < radiusProperty.getValue()) {
                movingNode = node;
                node.setMouseMoving(true);
                break;
            }
    }

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

    private void endMove(MouseEvent event) {
        if (movingNode != null) {
            movingNode.setMouseMoving(false);
            movingNode = null;
        }
    }

    private void zoom(ScrollEvent event) {
        double scale = event.getDeltaY() > 0 ? 1.25 : 0.8;
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

    private void mouseMoving(MouseEvent event) {
        final Vector mousePosition = new Vector(event.getX(), event.getY());
        synchronized (graph.getNodes()) {
            graph.getNodes().forEach(node -> node.setMouseOver(node.getPosition().distance(mousePosition) < radiusProperty.get()));
        }
        synchronized (graph.getRelationships()) {
            graph.getRelationships().forEach((nodePairKey, graphRelationship) -> graphRelationship.setMouseOver(graphRelationship.getPosition().distance(mousePosition) < RELATIONSHIP_RADIUS));
        }
    }

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

    public void setCandidates(List<Pearl> originGenes) {
        graph.setOriginNodes(originGenes);
        maxWeight = graph.getNodes().stream().map(GraphNode::getPearl).map(Pearl::getDistanceToPhenotype).max(Integer::compare).get();
        radiusProperty.setValue(0.4 * Math.sqrt(0.25 * effectiveWidth * effectiveHeight / graph.getNodes().size()));
        HierarchyDistribution.distribute(graph, margin, effectiveWidth, effectiveHeight, maxWeight);
        startTime = System.currentTimeMillis();
        startDrawer();
    }

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
        }, 0, 200);
    }

    private void iterate() {
        if (Coat.getStage() != null && !Coat.getStage().isShowing()) timer.cancel();
        interactNodes();
        updateNodePositions();
        Platform.runLater(this::paint);
    }

    private void interactNodes() {
        synchronized (graph.getNodes()) {
            graph.getNodes().stream().filter(graphNode -> !graphNode.isMouseMoving()).forEach(this::avoidCollisions);
        }
    }

    private void avoidCollisions(GraphNode graphNode) {
        final double security = diameter + 0.5 * nodeDistance;
        graph.getNodes().stream().filter(node -> !node.equals(graphNode)).forEach(node -> {
            final double dist = node.distance(graphNode);
            if (dist < security) {
                moveRandomly(graphNode); // Moving randomly avoids strict horizontal or vertical movements
                final Vector vector = new Vector(graphNode.getPosition(), node.getPosition());
                vector.scale((security - dist) / dist);
                node.push(vector);
            }
        });
    }

    private void moveRandomly(GraphNode node) {
        node.push(new Vector(random.nextDouble() - 0.5, random.nextDouble() - 0.5));
    }

    private void updateNodePositions() {
        graph.getNodes().forEach(node -> {
            limitSpeed(node);
            move(node);
            stop(node);
        });
    }

    private void limitSpeed(GraphNode node) {
        if (node.getDirection().getX() > maxSpeed) node.getDirection().setX(maxSpeed);
        if (node.getDirection().getY() > maxSpeed) node.getDirection().setY(maxSpeed);
        if (node.getDirection().getX() < -maxSpeed) node.getDirection().setX(-maxSpeed);
        if (node.getDirection().getY() < -maxSpeed) node.getDirection().setY(-maxSpeed);
    }

    private void move(GraphNode node) {
        node.getPosition().add(node.getDirection());
    }

    private void stop(GraphNode node) {
        node.getDirection().set(0, 0);
    }

    private void paint() {
        screen.clearRect(0, 0, getWidth(), getHeight());
        paintRelationships();
        paintNodes();
    }

    private void paintRelationships() {
        graph.getRelationships().forEach((nodePairKey, graphRelationship) -> {
            final int size = graphRelationship.getRelationships().size();
            final Vector center = getCenter(nodePairKey);
            graphRelationship.getPosition().set(center.getX(), center.getY());
            Color baseColor = getRelationshipColor(graphRelationship);
            drawRelationshipLine(nodePairKey, size, baseColor);
            drawRelationshipCircle(graphRelationship, baseColor);
            drawSelection(graphRelationship);
            writeText(size + "", center);
        });
    }

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
                map(pearlRelationship -> (String) pearlRelationship.getProperties().getOrDefault("type", null)).
                map(type -> PoirotAnalysis.RELATIONSHIP_SCORE.getOrDefault(type, 0.0)).
                max(Double::compare).get();
        Color baseColor = Color.BLACK.interpolate(Color.GREEN, score * 0.2);
        if (graphRelationship.isMouseOver()) baseColor = baseColor.interpolate(Color.WHITE, 0.5);
        return baseColor;
    }

    private void drawSelection(GraphRelationship graphRelationship) {
        if (graphRelationship.isSelected()) {
            screen.setLineWidth(4);
            screen.setStroke(new Color(1, 1, 0, getSelectionOpacity()));
            screen.strokeOval(graphRelationship.getPosition().getX() - RELATIONSHIP_RADIUS, graphRelationship.getPosition().getY() - RELATIONSHIP_RADIUS, RELATIONSHIP_RADIUS * 2, RELATIONSHIP_RADIUS * 2);
        }
    }

    private void writeText(String text, Vector position) {
        screen.setFill(Color.WHITE);
        screen.fillText(text, position.getX(), position.getY());
    }

    private void paintNodes() {
        synchronized (graph.getNodes()) {
            graph.getNodes().forEach(graphNode -> {
                drawCircle(graphNode);
                writeNodeText(graphNode);
            });
        }
    }

    private void drawCircle(GraphNode graphNode) {
        drawRelationshipCircle(graphNode);
        screen.setLineWidth(4);
        drawConsequences(graphNode);
        drawSelectionCircle(graphNode);
    }

    private void drawRelationshipCircle(GraphNode graphNode) {
        setBackgroundColor(graphNode);
        screen.fillOval(graphNode.getPosition().getX() - radiusProperty.get(), graphNode.getPosition().getY() - radiusProperty.get(), diameter, diameter);
    }

    private void setBackgroundColor(GraphNode graphNode) {
        Color color;
        switch (graphNode.getPearl().getType()) {
            case "phenotype":
                color = Color.ORANGE;
                break;
            case "gene":
                color = new Color(1.0 - graphNode.getPearl().getDistanceToPhenotype() / maxWeight, 0, 0, 1);
                break;
            default:
                color = Color.GRAY;
        }
        screen.setFill(graphNode.isMouseOver() ? color.interpolate(Color.WHITE, 0.5) : color);
    }

    private void drawConsequences(GraphNode graphNode) {
        final List<Variant> variants = (List<Variant>) graphNode.getPearl().getProperties().get("variants");
        if (variants != null) {
            final List<String> consequences = getConsequences(variants);
            final double VARIANT_RADIUS = radiusProperty.get() * 0.15;
            final double VARIANT_DIAMETER = 2 * VARIANT_RADIUS;
            for (int i = 0; i < consequences.size(); i++) {
                final double angle = 6.28318 * i / consequences.size() + 1.570795; // radians
                final double x = graphNode.getPosition().getX() + Math.cos(angle) * (radiusProperty.get() - VARIANT_DIAMETER) - VARIANT_RADIUS;
                final double y = graphNode.getPosition().getY() - Math.sin(angle) * (radiusProperty.get() - VARIANT_DIAMETER) - VARIANT_RADIUS;
                final double score = PoirotAnalysis.CONSEQUENCE_SCORE.getOrDefault(consequences.get(i), 0.0);
                Color color = Color.WHITE.interpolate(Color.RED, score * 0.2);
                screen.setFill(color);
                screen.fillOval(x, y, VARIANT_DIAMETER, VARIANT_DIAMETER);
            }
        }

    }

    private void drawSelectionCircle(GraphNode graphNode) {
        if (graphNode.isSelected()) {
            screen.setStroke(new Color(1, 1, 0, getSelectionOpacity()));
            screen.strokeOval(graphNode.getPosition().getX() - radiusProperty.get(), graphNode.getPosition().getY() - radiusProperty.get(), diameter, diameter);
        }
    }

    private List<String> getConsequences(List<Variant> variants) {
        List<String> consequences = new ArrayList<>();
        variants.forEach(variant -> {
            final String cons = (String) variant.getInfos().get("CONS");
            if (cons != null) Collections.addAll(consequences, cons.split(","));
        });
        return consequences;
    }

    private double getSelectionOpacity() {
        final long period = (System.currentTimeMillis() - startTime) % GLOWING_TIME;
        final long step = Math.abs(HALF_GLOWING_TIME - period);
        return step * OPACITY_FACTOR + 0.2;
    }

    private void writeNodeText(GraphNode graphNode) {
        String name;
        if (graphNode.getPearl().getType().equals("phenotype")) {
            screen.setFill(Color.BLACK);
            name = simplifyName(graphNode);
        } else {
            screen.setFill(Color.WHITE);
            name = graphNode.getPearl().getName();
        }
        screen.fillText(name, graphNode.getPosition().getX(), graphNode.getPosition().getY());
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
}
