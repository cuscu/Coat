package coat.model.poirot;

import coat.model.poirot.databases.*;
import coat.model.vcfreader.Variant;
import javafx.concurrent.Task;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * This class is a Task (a long task) that analyzes a VCF file and a list of phenotype key words and returns a
 * PearlDatabase with the gene-gene-phenotype graph associated to the variants.
 *
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class PoirotAnalysis extends Task<PearlDatabase> {


    private final List<String> phenotypeKeyWords;
    private final List<Variant> variants;

    private final Map<String, List<Variant>> geneMap = new HashMap<>();
    private final PearlDatabase pearlDatabase = new PearlDatabase();

    private final AtomicInteger round = new AtomicInteger();
    private final AtomicInteger genesCount = new AtomicInteger();
    private final AtomicInteger variantsCount = new AtomicInteger();

    private long numberOfLeafGenes;

    private final static List<String> GENE_BLACKLIST = Arrays.asList("UBC");
    private final static List<String> OMIM_METHOD = Arrays.asList("", "association", "linkage", "mutation", "deletion or duplication");

    /**
     * Creates a new PoirotAnalysis task, ready to be inserted in a Thread, or launched with <code>Paltform</code>
     *
     * @param variants   the list of variants
     * @param phenotypes this list of keywords
     */
    public PoirotAnalysis(List<Variant> variants, List<String> phenotypes) {
        this.variants = variants;
        this.phenotypeKeyWords = phenotypes;
    }

    @Override
    protected PearlDatabase call() throws Exception {
        try {
            mapVariantsToGenes();
            return runAnalysis();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Initialize the list of genes, taking them from the GNAME info field. Each gene name points to a list of variants.
     */
    private void mapVariantsToGenes() {
        updateMessage("Reading variants");
        variants.forEach(this::mapVariant);
    }

    private void mapVariant(Variant variant) {
        final String gene = (String) variant.getInfos().get("GNAME");
        if (gene != null) {
            String GENE = HGNCDatabase.getStandardSymbol(gene);
            if (GENE == null) GENE = gene.toUpperCase();
            List<Variant> vs = getVariants(GENE);
            vs.add(variant);
        }
        if (variantsCount.incrementAndGet() % 1000 == 0)
            updateMessage(String.format("Reading variants %d/%d", variantsCount.get(), variants.size()));
    }

    /**
     * Gets the list of variants related to this gene in the geneMap.
     *
     * @param GENE name of the gene
     * @return list of variants
     */
    private List<Variant> getVariants(String GENE) {
        List<Variant> vs = geneMap.get(GENE);
        if (vs == null) {
            vs = new ArrayList<>();
            geneMap.put(GENE, vs);
        }
        return vs;
    }

    /**
     * Executes the PoirotAnalysis
     *
     * @return a PearlDatabase with the resulting graph
     */
    private PearlDatabase runAnalysis() {
        addInitialGenes();
        expandGraph();
        setDistances();
        cleanDatabase();
        setScores();
        return pearlDatabase;
    }

    /**
     * Creates the initial wave of genes with the found affected genes in the variant list.
     */
    private void addInitialGenes() {
        geneMap.keySet().forEach(gene -> {
            final Pearl pearl = pearlDatabase.getOrCreate(gene, "gene");
            final List<Variant> vs = geneMap.get(gene);
            pearl.getProperties().put("variants", vs);
        });
    }

    /**
     * Performs 2 iterations of querying each node and finding its related genes and phenotypes.
     */
    private void expandGraph() {
        for (int i = 0; i < 2; i++) {
            round.set(i + 1);
            numberOfLeafGenes = pearlDatabase.getPearls("gene").stream().filter(Pearl::isLeaf).count();
            pearlDatabase.getPearls("gene").stream().
                    filter(Pearl::isLeaf).
                    forEach(this::expand);
        }
    }

    private void expand(Pearl pearl) {
        connectToOmimDisorders(pearl);
        connectToHPRDExpressions(pearl);
        addRelationships(BioGridDatabase.getRelationships(pearl.getGeneSymbol()));
        addRelationships(MenthaDatabase.getRelationships(pearl.getGeneSymbol()));
        addRelationships(HPRDDatabase.getRelationships(pearl.getGeneSymbol()));
        pearl.setLeaf(false);
        if (genesCount.incrementAndGet() % 100 == 0)
            updateMessage(String.format("Round %d of %d, %d/%d genes processed", round.get(), 2, genesCount.get(), numberOfLeafGenes));
    }

    /**
     * Locate the related Omim phenotypes and creates a relationship to them when needed.
     *
     * @param pearl the gene pearl
     */
    private void connectToOmimDisorders(Pearl pearl) {
        OmimDatabase.getEntries(pearl.getGeneSymbol()).stream()
                .map(omimEntry -> omimEntry.getField(3))
                .filter(disorders -> !disorders.equals("."))
                .flatMap(disorders -> Arrays.stream(disorders.split(";")))
                .forEach(disorder -> linkGeneToOmimDisorder(pearl, disorder));
    }

    /**
     * Connect gene to Omim disorder
     *
     * @param pearl    the gene pearl
     * @param disorder the line of the disorder
     */
    private void linkGeneToOmimDisorder(Pearl pearl, String disorder) {
        if (matchesKeywords(disorder)) {
            final String[] disorderFields = disorder.split("\\|");
            final String id = disorderFields[0] + "," + disorderFields[1];
            final Pearl phenotype = pearlDatabase.getOrCreate(id, "phenotype");
            phenotype.getProperties().put("name", disorderFields[0]);
            phenotype.getProperties().put("mimNumber", disorderFields[1]);
            final PearlRelationship relationshipTo = pearl.createRelationshipTo(phenotype);
            relationshipTo.setProperty("method", OMIM_METHOD.get(Integer.valueOf(disorderFields[2])));
        }
    }

    private boolean matchesKeywords(String disorder) {
        return phenotypeKeyWords.parallelStream().anyMatch(keyword -> disorder.toLowerCase().contains(keyword.toLowerCase()));
    }

    /**
     * Locate the hprd expressions associated to the gene, and create the relationships.
     *
     * @param pearl the gene pearl
     */
    private void connectToHPRDExpressions(Pearl pearl) {
        HPRDExpressionDatabase.getEntries(pearl.getGeneSymbol()).forEach(hprdEntry -> linkGeneToHPRDExpression(pearl, hprdEntry));
    }

    /**
     * Connect one gene with one expression.
     *
     * @param pearl      gene pearl
     * @param expression the HPRD expression
     */
    private void linkGeneToHPRDExpression(Pearl pearl, DatabaseEntry expression) {
        if (matchesKeywords(expression.getField(3))) {
            final Pearl phenotype = pearlDatabase.getOrCreate(expression.getField(3), "phenotype");
            phenotype.getProperties().put("name", expression.getField(3));
            final PearlRelationship relationshipTo = pearl.createRelationshipTo(phenotype);
            relationshipTo.setProperty("status", expression.getField(4));
        }
    }

    /**
     * Translate all StringRelationships to PearlRelationships. If source or target Pearl does not exist, create it.
     *
     * @param relationships list of StringRelationship
     */
    private void addRelationships(List<StringRelationship> relationships) {
        if (relationships != null)
            relationships.stream().
                    filter(relationship -> !isInBlacklist(relationship.getSource())).
                    filter(relationship -> !isInBlacklist(relationship.getTarget())).
                    forEach(relationship -> {
                        final Pearl source = pearlDatabase.getOrCreate(relationship.getSource(), "gene");
                        final Pearl target = pearlDatabase.getOrCreate(relationship.getTarget(), "gene");
                        updateRelationship(relationship, source, target);
                    });
    }

    private boolean isInBlacklist(String symbol) {
        return GENE_BLACKLIST.contains(symbol);
    }

    /**
     * If relationship does not exist, create it.
     *
     * @param myRelationship relationship as StringRelationship
     * @param source         source pearl
     * @param target         target pearl
     */
    private void updateRelationship(StringRelationship myRelationship, Pearl source, Pearl target) {
        String id = (String) myRelationship.getProperties().get("id");
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

    /**
     * Calculates the distance of each node to the nearest phenotype.
     */
    private void setDistances() {
        pearlDatabase.getPearls("phenotype").forEach(pearl -> {
            pearl.setDistanceToPhenotype(0);
            pearl.getRelationships().keySet().forEach(otherPearl -> otherPearl.setDistanceToPhenotype(1));
        });
        final int[] distance = {2};
        for (int i = 0; i < 3; i++) {
            final List<Pearl> wave = pearlDatabase.getPearls("gene").stream().filter(gene -> gene.getDistanceToPhenotype() > 0).collect(Collectors.toList());
            wave.forEach(pearl -> pearl.getRelationships().forEach((otherPearl, relationships) -> {
                if (otherPearl.getDistanceToPhenotype() < 0) otherPearl.setDistanceToPhenotype(distance[0]);
            }));
            distance[0]++;
        }
    }

    /**
     * Removes all nodes in the database that have negative distance to phenotype.
     */
    private void cleanDatabase() {
        pearlDatabase.getPearls("gene").stream().filter(pearl -> pearl.getDistanceToPhenotype() < 0).forEach(pearlDatabase::remove);
    }

    /**
     *
     */
    private void setScores() {
        final GraphScore graphScore = new GraphScore(pearlDatabase);
        graphScore.messageProperty().addListener((observable, oldValue, newValue) -> updateMessage(newValue));
        graphScore.run();
    }
}


