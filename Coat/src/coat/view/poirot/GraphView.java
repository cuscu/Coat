package coat.view.poirot;

import coat.Coat;
import coat.model.poirot.Pearl;
import coat.model.poirot.PearlRelationship;
import coat.model.poirot.ShortestPath;
import coat.model.vcf.Variant;
import javafx.application.Platform;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.ArcType;
import javafx.scene.text.FontSmoothingType;
import javafx.scene.text.TextAlignment;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class GraphView extends Canvas {

    private final GraphicsContext screen;
    private final List<GraphNode> nodes = new ArrayList<>();
    private final AtomicBoolean updating = new AtomicBoolean(false);

    private final ObservableList<Pearl> sourceNodes = FXCollections.observableArrayList();
    private final Property<Pearl> selectedPearl = new SimpleObjectProperty<>();

    private Timer timer;
    private double diameter;
    private double radius;
    private double margin;
    private int maxTotal;
    private double maxWeight;
    private double effectiveWidth;
    private double effectiveHeight;
    private double maxSpeed;
    private long startTime;
    private double nodeDistance;
    private double levelHeight;
    private Vector lastMousePosition = new Vector();

    private final long GLOWING_TIME = 2000;
    private final long HALF_GLOWING_TIME = GLOWING_TIME / 2;
    private final double OPACITY_FACTOR = 0.8 / HALF_GLOWING_TIME;

    private final static Map<String, Paint> BIO_COLORS = new HashMap<>();
    private final static Map<String, Paint> CONS_COLORS = new HashMap<>();

    static {
        BIO_COLORS.put("protein_coding", Color.LIMEGREEN);
        BIO_COLORS.put("processed_transcript", Color.CYAN);
        BIO_COLORS.put("retained_intron", Color.BLUE);
        BIO_COLORS.put("nonsense_mediated_decay", Color.DARKBLUE);
        BIO_COLORS.put("lincRNA", Color.MAGENTA);
        CONS_COLORS.put("intron_variant", Color.DARKBLUE);
        CONS_COLORS.put("downstream_gene_variant", Color.LIMEGREEN);
        CONS_COLORS.put("upstream_gene_variant", Color.LIMEGREEN);
        CONS_COLORS.put("non_coding_trasncript_variant", Color.CYAN);
        CONS_COLORS.put("NMD_transcript_variant", Color.CYAN);
        CONS_COLORS.put("non_coding_transcript_exon_variant", Color.CYAN);
        CONS_COLORS.put("regulatory_region_variant", Color.CYAN);

    }

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
    }

    public Property<Pearl> getSelectedPearlProperty() {
        return selectedPearl;
    }

    private void setMouseEvents() {
        setOnMouseClicked(this::clicked);
        setOnMousePressed(this::startMove);
        setOnMouseReleased(this::endMove);
        setOnMouseDragged(this::mouseDragging);
        setOnScroll(this::zoom);
    }

    private void clicked(MouseEvent event) {
        if (movingNode == null) {
            final Vector click = new Vector(event.getX(), event.getY());
            boolean selected = false;
            for (GraphNode node : nodes) {
                node.setSelected(click.distance(node.getPosition()) < radius);
                if (node.isSelected()) {
                    selectedPearl.setValue(node.getPearl());
                    selected = true;
                }
            }
            if (!selected) {
                lastMousePosition.set(event.getX(), event.getY());
                selectedPearl.setValue(null);
            }
        }
    }

    private void startMove(MouseEvent event) {
        lastMousePosition = new Vector(event.getX(), event.getY());
        for (GraphNode node : nodes)
            if (node.getPosition().distance(lastMousePosition) < radius) {
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
        setRadius(radius * scale);
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

    private void setRadius(double radius) {
        this.radius = radius;
        diameter = 2 * radius;
        maxSpeed = 0.25 * radius;
        margin = 1.1 * radius;
        nodeDistance = 2 * diameter;
        levelHeight = 2 * diameter;
        // Nodes will share half of the area
        effectiveWidth = getWidth() - 2 * margin;
        effectiveHeight = getHeight() - 2 * margin;
    }

    public void setCandidates(List<Pearl> originGenes) {
        updating.set(true);
        this.sourceNodes.setAll(originGenes);
        findNodes();
        initialDistribution();
        startTime = System.currentTimeMillis();
        startDrawer();
        updating.set(false);
    }

    private void findNodes() {
        nodes.clear();
        sourceNodes.forEach(gene -> {
            final List<List<PearlRelationship>> paths = ShortestPath.getShortestPaths(gene);
            createGraphicGraph(paths);
        });
    }

    private void createGraphicGraph(List<List<PearlRelationship>> paths) {
        maxTotal = 0;
        maxWeight = 0;
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
        if (graphNode.getPearl().getWeight() > maxWeight) maxWeight = graphNode.getPearl().getWeight();
        return graphNode;
    }

    private void addRelationship(GraphNode source, GraphNode target, PearlRelationship relationship) {
        if (!relationshipExists(source, target)) {
            final GraphRelationship graphRelationship = new GraphRelationship(source, target, relationship.getProperties());
            int total = (int) relationship.getProperty("total");
            if (total > maxTotal) maxTotal = total;
            source.getRelationships().add(graphRelationship);
            target.getRelationships().add(graphRelationship);
        }
    }

    private boolean relationshipExists(GraphNode source, GraphNode target) {
        for (GraphRelationship graphRelationship : source.getRelationships())
            if (graphRelationship.getTarget().equals(target)) return true;
        return false;
    }

    private void initialDistribution() {
        setRadius(0.5 * Math.sqrt(0.25 * effectiveWidth * effectiveHeight / nodes.size()));
        nodes.forEach(node -> node.getPosition().set(
                margin + effectiveWidth * random.nextDouble(),
                margin + effectiveHeight * (node.getPearl().getWeight()) / maxWeight));
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
                if (!node.isMouseMoving()) {
                    avoidCollisions(node);
                    if ((System.currentTimeMillis() - startTime < 30000)) {
                        closeRelated(node);
                        moveRandomly(node);
                        floatInAir(node);
                    }
                }
            });
        }
    }

    private void moveRandomly(GraphNode node) {
        node.push(new Vector(random.nextDouble() - 0.5, random.nextDouble() - 0.5));
    }

    private void avoidCollisions(GraphNode graphNode) {
        nodes.stream().filter(node -> !node.equals(graphNode)).forEach(node -> {
            final double dist = node.distance(graphNode);
            if (dist < diameter) {
                final Vector vector = new Vector(graphNode.getPosition(), node.getPosition());
                vector.scale((diameter - dist) / dist);
                node.push(vector);
            }
        });
    }

    private void closeRelated(GraphNode node) {
        node.getRelationships().stream().filter(relationship -> relationship.getSource().equals(node)).forEach(relationship -> {
            final GraphNode target = relationship.getTarget();
//            final double dist = node.distance(target);
            final double dist = Math.abs(node.getPosition().getX() - target.getPosition().getX());
            final int t = (int) relationship.getProperties().get("total");
            final Vector v = new Vector(target.getPosition().getX() - node.getPosition().getX(), 0.0);
            v.scale(t * (dist - nodeDistance) / dist);
            node.push(v);
        });
    }

    private void floatInAir(GraphNode graphNode) {
        double prefHeight = margin + effectiveHeight * (graphNode.getPearl().getWeight()) / maxWeight;
        double height = graphNode.getPosition().getY();
        if (height - prefHeight > levelHeight || prefHeight - height > levelHeight) {
            final Vector vector = new Vector(graphNode.getPosition(), new Vector(graphNode.getPosition().getX(), prefHeight));
            graphNode.push(vector);
        }

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
        if (node.getPosition().getY() > effectiveHeight + margin) node.getPosition().setY(effectiveHeight + margin);
    }

    private void stop(GraphNode node) {
        node.getDirection().set(0, 0);
    }

    private void paint() {
        screen.clearRect(0, 0, getWidth(), getHeight());
        paintRelationships();
        paintNodes();
    }

    private void paintNodes() {
        synchronized (nodes) {
            nodes.forEach(graphNode -> {
                drawCircle(graphNode);
                writeText(graphNode);
            });
        }
    }

    private void paintRelationships() {
        nodes.forEach(graphNode -> graphNode.getRelationships().stream().filter(relationship -> relationship.getSource().equals(graphNode)).forEach(relationship -> {
            final int total = (int) relationship.getProperties().get("total");
            screen.setLineWidth(total);
            drawLine(graphNode.getPosition(), relationship.getTarget().getPosition());
            Vector center = new Vector(
                    (graphNode.getPosition().getX() + relationship.getTarget().getPosition().getX()) * 0.5,
                    (graphNode.getPosition().getY() + relationship.getTarget().getPosition().getY()) * 0.5
            );
            drawBox(center);
            writeText((int) relationship.getProperties().get("total") + "", center);
        }));
    }

    private void drawLine(Vector source, Vector target) {
        screen.setStroke(Color.BLACK);
        screen.strokeLine(source.getX(), source.getY(), target.getX(), target.getY());
    }

    private void drawBox(Vector position) {
        screen.setFill(Color.GRAY);
        screen.fillRoundRect(position.getX() - 6, position.getY() - 6, 12, 12, 1, 1);
    }

    private void writeText(String text, Vector position) {
        screen.setFill(Color.BLACK);
        screen.fillText(text, position.getX(), position.getY());
    }

    private void drawCircle(GraphNode graphNode) {
        drawBackgroundCircle(graphNode);
        screen.setLineWidth(4);
        drawSelectionCircle(graphNode);
        drawBiotypeCircle(graphNode);
        drawConsequenceCircle(graphNode);
    }

    private void drawBackgroundCircle(GraphNode graphNode) {
        setColor(graphNode);
        screen.fillOval(graphNode.getPosition().getX() - radius, graphNode.getPosition().getY() - radius, diameter, diameter);
    }

    private void setColor(GraphNode graphNode) {
        switch (graphNode.getPearl().getType()) {
            case "phenotype":
                screen.setFill(Color.ORANGE);
                break;
            case "gene":
                screen.setFill(new Color(1.0 - graphNode.getPearl().getWeight() / maxWeight, 0, 0, 1));
                break;
        }
    }

    private void drawSelectionCircle(GraphNode graphNode) {
        if (graphNode.isSelected()) {
            screen.setStroke(new Color(1, 1, 0, getSelectionOpacity()));
            screen.strokeOval(graphNode.getPosition().getX() - radius, graphNode.getPosition().getY() - radius, diameter, diameter);
        }
    }

    private void drawBiotypeCircle(GraphNode graphNode) {
        final List<Variant> variants = (List<Variant>) graphNode.getPearl().getProperties().get("variants");
        if (variants != null && !variants.isEmpty()) {
            final List<String> biotypes = getBiotypes(variants);
            final List<String> uniques = biotypes.stream().distinct().collect(Collectors.toList());
            Collections.sort(uniques, (o1, o2) -> Long.compare(count(biotypes, o1), count(biotypes, o2)));
            final int total = biotypes.size();
            double x = graphNode.getPosition().getX() - radius + 4;
            double y = graphNode.getPosition().getY() - radius + 4;
            double width = diameter - 8;
            double height = diameter - 8;
            double startAngle = 90;
            for (String value : uniques) {
                final long count = count(biotypes, value);
                double angle = (double) count / total * 360;
                screen.setStroke(BIO_COLORS.getOrDefault(value, Color.GRAY));
                screen.strokeArc(x, y, width, height, startAngle, angle, ArcType.OPEN);
                startAngle += angle;
            }
        }
    }

    private long count(List<String> biotypes, String value) {
        return biotypes.stream().filter(s -> s.equals(value)).count();
    }

    private List<String> getBiotypes(List<Variant> variants) {
        return variants.stream().
                map(variant -> (String) variant.getInfos().get("BIO")).
                filter(biotype -> biotype != null).collect(Collectors.toList());
    }

    private void drawConsequenceCircle(GraphNode graphNode) {
        final List<Variant> variants = (List<Variant>) graphNode.getPearl().getProperties().get("variants");
        if (variants != null && !variants.isEmpty()) {
            final List<String> consequences = getConsequences(variants);
            final List<String> uniques = consequences.stream().distinct().collect(Collectors.toList());
            Collections.sort(uniques, (o1, o2) -> Long.compare(count(consequences, o1), count(consequences, o2)));
            final int total = consequences.size();
            double x = graphNode.getPosition().getX() - radius + 8;
            double y = graphNode.getPosition().getY() - radius + 8;
            double width = diameter - 16;
            double height = diameter - 16;
            double startAngle = 90;
            for (String value : uniques) {
                final long count = count(consequences, value);
                double angle = (double) count / total * 360;
                screen.setStroke(CONS_COLORS.getOrDefault(value, Color.GRAY));
                screen.strokeArc(x, y, width, height, startAngle, angle, ArcType.OPEN);
                startAngle += angle;
            }
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
        long period = (System.currentTimeMillis() - startTime) % GLOWING_TIME;
        long step = Math.abs(HALF_GLOWING_TIME - period);
        return step * OPACITY_FACTOR + 0.2;
    }

    private void writeText(GraphNode graphNode) {
        screen.setFill(Color.WHITE);
        screen.fillText(graphNode.getPearl().getName(), graphNode.getPosition().getX(), graphNode.getPosition().getY());
    }
}
