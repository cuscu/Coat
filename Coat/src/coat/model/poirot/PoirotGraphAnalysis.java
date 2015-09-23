package coat.model.poirot;

import coat.model.poirot.databases.*;
import coat.model.vcfreader.Variant;
import javafx.concurrent.Task;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Task (a long task) that analyzes a VCF file and a list of phenotype key words and returns a PearlDatabase with the
 * gene-gene-phenotype graph associated to the variants.
 *
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class PoirotGraphAnalysis extends Task<PearlDatabase> {

    public static final int CYCLES = 2;
    private Dataset omimDataset;
    private Dataset hprdExpressionDataset;

    private final List<Variant> variants;
    private final PearlDatabase database = new PearlDatabase();

    private final List<Pearl> leafGenes = new ArrayList<>();
    private int pearlsCount;
    private int round;
    private int numberOfPearlsToExpand;
    private final static List<String> GENE_BLACKLIST = Arrays.asList("UBC");

    /**
     * Creates a new PoirotAnalysis task, ready to be inserted in a Thread, or launched with <code>Paltform</code>
     *
     * @param variants the list of variants
     */
    public PoirotGraphAnalysis(List<Variant> variants) {
        this.variants = variants;
    }

    @Override
    protected PearlDatabase call() throws Exception {
        loadOmimDataset();
        loadHPRDExpressionDataset();
        addInitialGenes();
        expandGraph();
        return database;
    }

    private void loadHPRDExpressionDataset() {
        final HPRDExpressionDatasetLoader loader = new HPRDExpressionDatasetLoader();
        loader.run();
        try {
            hprdExpressionDataset = loader.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    private void loadOmimDataset() {
        final OmimDatasetLoader loader = new OmimDatasetLoader();
        loader.run();
        try {
            omimDataset = loader.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    private void addInitialGenes() {
        variants.forEach(variant -> {
            final String gene = (String) variant.getInfos().get("GNAME");
            if (gene != null) addVariantGeneToDatabase(variant, gene);
        });
        leafGenes.addAll(database.getPearls("gene"));
    }

    private void addVariantGeneToDatabase(Variant variant, String gene) {
        final String GENE = HGNCDatabase.getStandardSymbol(gene);
        final Pearl genePearl = database.getOrCreate(GENE, "gene");
        genePearl.getProperties().putIfAbsent("variants", new ArrayList<>());
        final List<Variant> geneVariants = (List<Variant>) genePearl.getProperties().get("variants");
        geneVariants.add(variant);
    }

    private void expandGraph() {
        for (int i = 0; i < CYCLES; i++) {
            final List<Pearl> pearlsToExpand = new ArrayList<>(leafGenes);
            leafGenes.clear();
            resetProgress(i, pearlsToExpand.size());
            pearlsToExpand.forEach(this::expand);
        }
    }

    private void resetProgress(int i, int total) {
        pearlsCount = 0;
        round = i;
        numberOfPearlsToExpand = total;
    }

    private void expand(Pearl pearl) {
        printProgress();
        connect(pearl);
    }

    private void printProgress() {
        if (++pearlsCount % 100 == 0)
            updateMessage(String.format("Round %d of %d, %d/%d genes processed", round, 2, pearlsCount, numberOfPearlsToExpand));

    }

    private void connect(Pearl pearl) {
        connectToOmimDisorders(pearl);
        connectToHPRDExpressions(pearl);
        addRelationships(BioGridDatabase.getRelationships(pearl.getName()));
        addRelationships(MenthaDatabase.getRelationships(pearl.getName()));
        addRelationships(HPRDDatabase.getRelationships(pearl.getName()));
    }

    private void connectToOmimDisorders(Pearl pearl) {
        omimDataset.getInstances(pearl.getName(), 0).forEach(instance -> {
            final Pearl phenotype = getOmimPhenotypePearl(instance);
            final PearlRelationship relationship = pearl.createRelationshipTo(phenotype);
            final String confidence = (String) instance.getField(3);
            relationship.getProperties().put("confidence", confidence);
        });
    }

    private Pearl getOmimPhenotypePearl(Instance instance) {
        final String name = (String) instance.getField(4);
        return database.contains(name, "phenotype")
                ? database.getPearl(name, "phenotype")
                : createOmimPhenotype(instance);
    }

    private Pearl createOmimPhenotype(Instance instance) {
        final Integer phenotype_mappingKey = (Integer) instance.getField(6);
        final String phenotype_name = (String) instance.getField(4);
        final Integer mimNumber = (Integer) instance.getField(5);
        final Pearl phenotype = database.getOrCreate(phenotype_name, "phenotype");
        phenotype.getProperties().put("name", phenotype_name);
        phenotype.getProperties().put("mappingKey", phenotype_mappingKey);
        phenotype.getProperties().put("mimNumber", mimNumber);
        return phenotype;
    }

    private void connectToHPRDExpressions(Pearl pearl) {
        hprdExpressionDataset.getInstances(pearl.getName(), 2).forEach(instance -> {
            final Pearl phenotype = getHPRDExpressionPearl(instance);
            final PearlRelationship relationship = pearl.createRelationshipTo(phenotype);
            relationship.getProperties().put("hprd_id", instance.getField(0));
            relationship.getProperties().put("status", instance.getField(4));

        });
    }

    private Pearl getHPRDExpressionPearl(Instance instance) {
        final String expression = (String) instance.getField(3);
        return database.getOrCreate(expression, "phenotype");
    }

    private void addRelationships(List<StringRelationship> relationships) {
        if (relationships != null)
            relationships.stream().
                    filter(relationship -> notInBlacklist(relationship.getSource())).
                    filter(relationship -> notInBlacklist(relationship.getTarget())).
                    forEach(relationship -> {
                        final Pearl source = database.getOrCreate(relationship.getSource(), "gene");
                        final Pearl target = database.getOrCreate(relationship.getTarget(), "gene");
                        updateRelationship(relationship, source, target);
                    });
    }

    private boolean notInBlacklist(String symbol) {
        return !GENE_BLACKLIST.contains(symbol);
    }

    /**
     * If relationship does not exist, create it.
     *
     * @param myRelationship relationship as StringRelationship
     * @param source         source pearl
     * @param target         target pearl
     */
    private void updateRelationship(StringRelationship myRelationship, Pearl source, Pearl target) {
        final String id = (String) myRelationship.getProperties().get("id");
        if (!relationshipExists(source, target, id)) {
            PearlRelationship relationship = new PearlRelationship(source, target);
            relationship.getProperties().putAll(myRelationship.getProperties());
        }
    }

    /**
     * Checks by id if the relationship already exists.
     *
     * @param source source node
     * @param target target node
     * @param id     id of the relationship
     * @return true if relationshipExists
     */
    private boolean relationshipExists(Pearl source, Pearl target, String id) {
        final List<PearlRelationship> relationships = source.getRelationships().getOrDefault(target, Collections.emptyList());
        return relationships.stream().anyMatch(relationship -> relationship.getProperties().get("id").equals(id));
    }
}
