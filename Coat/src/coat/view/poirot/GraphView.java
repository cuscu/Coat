package coat.view.poirot;

import coat.Coat;
import coat.model.poirot.Pearl;
import coat.model.poirot.PearlRelationship;
import coat.model.poirot.PoirotAnalysis2;
import coat.model.poirot.ShortestPath;
import coat.model.vcf.Variant;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.FontSmoothingType;
import javafx.scene.text.TextAlignment;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class GraphView extends Canvas {

    public static final int RELATIONSHIP_RADIUS = 10;
    private final GraphicsContext screen;
    private final List<GraphNode> nodes = new ArrayList<>();
    private final AtomicBoolean updating = new AtomicBoolean(false);

    private final ObservableList<Pearl> sourceNodes = FXCollections.observableArrayList();
    private final Property<Pearl> selectedPearl = new SimpleObjectProperty<>();
    private final Property<GraphRelationship> selectedRelationship = new SimpleObjectProperty<>();

    private Timer timer;
    private double diameter;


    private final DoubleProperty radiusProperty = new SimpleDoubleProperty();
    private double margin;
    private int maxTotal;
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

    private final Map<NodePairKey, GraphRelationship> relationships = new HashMap<>();
//    private final static Map<String, Paint> BIO_COLORS = new HashMap<>();
//
//    static {
//        BIO_COLORS.put("protein_coding", Color.LIMEGREEN);
//        BIO_COLORS.put("processed_transcript", Color.CYAN);
//        BIO_COLORS.put("retained_intron", Color.BLUE);
//        BIO_COLORS.put("nonsense_mediated_decay", Color.DARKBLUE);
//        BIO_COLORS.put("lincRNA", Color.MAGENTA);
//    }

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
        for (Map.Entry<NodePairKey, GraphRelationship> entry : relationships.entrySet()) {
            entry.getValue().setSelected(clickPosition.distance(entry.getValue().getPosition()) < RELATIONSHIP_RADIUS);
            if (entry.getValue().isSelected()) selectedRelationship.setValue(entry.getValue());
        }
    }

    private void selectNode(Vector clickPosition) {
        selectedPearl.setValue(null);
        for (GraphNode node : nodes) {
            node.setSelected(clickPosition.distance(node.getPosition()) < radiusProperty.getValue());
            if (node.isSelected()) selectedPearl.setValue(node.getPearl());
        }
    }

    private void startMove(MouseEvent event) {
        lastMousePosition = new Vector(event.getX(), event.getY());
        for (GraphNode node : nodes)
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
            synchronized (nodes) {
                for (GraphNode node : nodes) node.getPosition().add(direction);
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
        updating.set(true);
        double scale = event.getDeltaY() > 0 ? 1.25 : 0.8;
        radiusProperty.setValue(radiusProperty.get() * scale);
        final Vector mouse = new Vector(event.getX(), event.getY());
        synchronized (nodes) {
            nodes.forEach(graphNode -> {
                graphNode.getPosition().substract(mouse);
                graphNode.getPosition().scale(scale);
                graphNode.getPosition().add(mouse);
            });
        }
        updating.set(false);
    }

    private void mouseMoving(MouseEvent event) {
        final Vector mousePosition = new Vector(event.getX(), event.getY());
        synchronized (nodes) {
            nodes.forEach(node -> node.setMouseOver(node.getPosition().distance(mousePosition) < radiusProperty.get()));
        }
        synchronized (relationships) {
            relationships.forEach((nodePairKey, graphRelationship) -> graphRelationship.setMouseOver(graphRelationship.getPosition().distance(mousePosition) < RELATIONSHIP_RADIUS));
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
        updating.set(true);
        this.sourceNodes.setAll(originGenes);
        findNodes();
        radiusProperty.setValue(0.4 * Math.sqrt(0.25 * effectiveWidth * effectiveHeight / nodes.size()));
        initialDistribution();
        startTime = System.currentTimeMillis();
        startDrawer();
        updating.set(false);
    }

    private void findNodes() {
        nodes.clear();
        relationships.clear();
        maxTotal = 0;
        maxWeight = 0;
        sourceNodes.forEach(gene -> {
            final List<List<PearlRelationship>> paths = ShortestPath.getShortestPaths(gene);
            addToGraph(paths);
        });
    }

    private void addToGraph(List<List<PearlRelationship>> paths) {
        paths.forEach(path -> path.forEach(relationship -> {
            final GraphNode target = addOrGetNode(relationship.getTarget());
            final GraphNode source = addOrGetNode(relationship.getSource());
            addRelationship(source, target, relationship);
        }));
    }

    private GraphNode addOrGetNode(Pearl node) {
        GraphNode graphNode = getGraphNode(node);
        if (graphNode == null) graphNode = createGraphNode(node);
        return graphNode;
    }

    private GraphNode getGraphNode(Pearl node) {
        for (GraphNode graphNode : nodes) if (graphNode.getPearl().equals(node)) return graphNode;
        return null;
    }

    private GraphNode createGraphNode(Pearl node) {
        final GraphNode graphNode = new GraphNode(node);
        nodes.add(graphNode);
        if (graphNode.getPearl().getDistanceToPhenotype() > maxWeight)
            maxWeight = graphNode.getPearl().getDistanceToPhenotype();
        return graphNode;
    }

    private void addRelationship(GraphNode source, GraphNode target, PearlRelationship relationship) {
        NodePairKey key = new NodePairKey(source, target);
        GraphRelationship graphRelationship = relationships.get(key);
        if (graphRelationship == null) {
            graphRelationship = new GraphRelationship();
            relationships.put(key, graphRelationship);
        }
        if (!graphRelationship.getRelationships().contains(relationship))
            graphRelationship.getRelationships().add(relationship);
    }

    private void initialDistribution() {
        hierarchyDistribution();
//        printGraphSize();
    }

    private void hierarchyDistribution() {
        final List<GraphNode> rootNodes = nodes.stream().
                filter(graphNode -> graphNode.getPearl().getDistanceToPhenotype() == 0).collect(Collectors.toList());
        final double y = margin;
        final double nodeWidth = effectiveWidth / rootNodes.size();
        for (int i = 0; i < rootNodes.size(); i++) {
            final double x = margin + i * nodeWidth + 0.5 * nodeWidth;
            rootNodes.get(i).getPosition().set(x, y);
        }
        setSubHierarchy(rootNodes, 1);

    }

    private void setSubHierarchy(List<GraphNode> parentNodes, int weight) {
        if (weight > maxWeight) return;
        final List<GraphNode> subNodes = nodes.stream().
                filter(graphNode -> graphNode.getPearl().getDistanceToPhenotype() == weight).collect(Collectors.toList());
        final double y = margin + weight / maxWeight * effectiveHeight;
        final double nodeWidth = effectiveWidth / subNodes.size();
        int j = 0;
        final List<GraphNode> orderNodes = new ArrayList<>();
        for (GraphNode parentNode : parentNodes) {
            final List<GraphNode> children = extractNodeChildren(subNodes, parentNode);
            for (GraphNode child : children) {
                if (!orderNodes.contains(child)) {
                    double x = margin + j * nodeWidth + 0.5 * nodeWidth;
                    child.getPosition().set(x, y);
                    orderNodes.add(child);
                    j++;
                }
            }
        }
        setSubHierarchy(orderNodes, weight + 1);
    }

    private List<GraphNode> extractNodeChildren(List<GraphNode> subNodes, GraphNode parentNode) {
        return subNodes.stream().filter(graphNode -> relationships.containsKey(new NodePairKey(parentNode, graphNode))).collect(Collectors.toList());
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
        if (updating.get()) return;
        if (Coat.getStage() != null && !Coat.getStage().isShowing()) timer.cancel();
        interactNodes();
        updateNodePositions();
        Platform.runLater(this::paint);
    }

    private void interactNodes() {
        synchronized (nodes) {
            nodes.forEach(node -> {
                if (!node.isMouseMoving()) avoidCollisions(node);
            });
        }
    }

    private void avoidCollisions(GraphNode graphNode) {
        final double security = diameter + 0.5 * nodeDistance;
        nodes.stream().filter(node -> !node.equals(graphNode)).forEach(node -> {
            final double dist = node.distance(graphNode);
            if (dist < security) {
                moveRandomly(graphNode);
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
        nodes.forEach(node -> {
            limitSpeed(node);
            move(node);
            stayInSafeArea(node);
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

    private void stayInSafeArea(GraphNode node) {
        if (node.getPosition().getX() < margin) node.getPosition().setX(margin);
        if (node.getPosition().getX() > margin + effectiveWidth) node.getPosition().setX(margin + effectiveWidth);
        if (node.getPosition().getY() < margin) node.getPosition().setY(margin);
        if (node.getPosition().getY() > margin + effectiveHeight) node.getPosition().setY(margin + effectiveHeight);
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
        relationships.forEach((nodePairKey, graphRelationship) -> {
            final int size = graphRelationship.getRelationships().size();
            final Vector center = getCenter(nodePairKey);
            graphRelationship.getPosition().set(center.getX(), center.getY());
            drawRelationshipLine(nodePairKey, size);
            drawRelationshipCircle(graphRelationship);
            drawSelection(graphRelationship);
            writeText(size + "", center);

        });
    }

    private Vector getCenter(NodePairKey nodePairKey) {
        return new Vector(
                (nodePairKey.getSource().getPosition().getX() + nodePairKey.getTarget().getPosition().getX()) * 0.5,
                (nodePairKey.getSource().getPosition().getY() + nodePairKey.getTarget().getPosition().getY()) * 0.5);
    }

    private void drawRelationshipLine(NodePairKey nodePairKey, int size) {
        screen.setStroke(Color.BLACK);
        screen.setLineWidth(size);
        screen.strokeLine(nodePairKey.getSource().getPosition().getX(), nodePairKey.getSource().getPosition().getY(),
                nodePairKey.getTarget().getPosition().getX(), nodePairKey.getTarget().getPosition().getY());
    }

    private void drawSelection(GraphRelationship graphRelationship) {
        if (graphRelationship.isSelected()) {
            screen.setLineWidth(4);
            screen.setStroke(new Color(1, 1, 0, getSelectionOpacity()));
            screen.strokeOval(graphRelationship.getPosition().getX() - RELATIONSHIP_RADIUS, graphRelationship.getPosition().getY() - RELATIONSHIP_RADIUS, RELATIONSHIP_RADIUS * 2, RELATIONSHIP_RADIUS * 2);
        }
    }

    private void drawRelationshipCircle(GraphRelationship graphRelationship) {
        final double score = graphRelationship.getRelationships().stream().
                map(pearlRelationship -> (String) pearlRelationship.getProperties().getOrDefault("type", null)).
                map(type -> PoirotAnalysis2.RELATIONSHIP_SCORE.getOrDefault(type, 0.0)).
                max(Double::compare).get();
        Color baseColor = Color.BLACK.interpolate(Color.GREEN, score * 0.2);
        if (graphRelationship.isMouseOver()) baseColor = baseColor.interpolate(Color.WHITE, 0.5);
        screen.setFill(baseColor);
        screen.fillOval(graphRelationship.getPosition().getX() - RELATIONSHIP_RADIUS, graphRelationship.getPosition().getY() - RELATIONSHIP_RADIUS, RELATIONSHIP_RADIUS * 2, RELATIONSHIP_RADIUS * 2);
    }

    private void writeText(String text, Vector position) {
        screen.setFill(Color.WHITE);
        screen.fillText(text, position.getX(), position.getY());
    }

    private void paintNodes() {
        synchronized (nodes) {
            nodes.forEach(graphNode -> {
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
                final double score = PoirotAnalysis2.CONSEQUENCE_SCORE.getOrDefault(consequences.get(i), 0.0);
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
        String [] words = name.split(" ");
        return words[0];
//        final int nextQuote = name.indexOf(",");
//        if (nextQuote > 0) name = name.substring(0, nextQuote).replace("{", "");
//        return name;
    }

    private void printGraphSize() {
        System.out.println(String.format("Nodes: %d, edges: %d", nodes.size(), relationships.size()));
        relationships.forEach((nodePairKey, graphRelationship) -> System.out.println(nodePairKey.getKey() + " " + graphRelationship.getRelationships().size()));
    }
}
