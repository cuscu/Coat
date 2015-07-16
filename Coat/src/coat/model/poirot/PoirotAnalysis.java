package coat.model.poirot;

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

    private Map<String, List<Variant>> geneMap = new HashMap<>();
    private final PearlDatabase pearlDatabase = new PearlDatabase();

    private AtomicInteger round = new AtomicInteger();

    private final static List<String> GENE_BLACKLIST = Arrays.asList("UBC");
    private final static List<String> OMIM_METHOD = Arrays.asList("", "association", "linkage", "mutation", "deletion or duplication");

    /**
     * Omim diseases database (gene-phenotype)
     */
    private OmimDatabase omimDatabase = new OmimDatabase();
    /**
     * HGNC genes database (gene standard names)
     */
    private HGNCDatabase hgncDatabase = new HGNCDatabase();
    /**
     * HPRD expression database (gene-phenotype)
     */
    private HPRDExpressionDatabase hprdExpressionDatabase = new HPRDExpressionDatabase();

    /**
     * Creates a new PoirotAnalysis task, ready to be inserted in a Thread, or launch with <code>Paltform</code>
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
        int total = variants.size();
        AtomicInteger count = new AtomicInteger();
        variants.forEach(variant -> {
            final String gene = (String) variant.getInfos().get("GNAME");
            if (gene != null) {
                String GENE = getStandardName(gene);
                if (GENE == null) GENE = gene.toUpperCase();
                List<Variant> vs = getVariants(GENE);
                vs.add(variant);
            }
            if (count.incrementAndGet() % 1000 == 0)
                updateMessage(String.format("Reading variants %d/%d", count.get(), total));
        });
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
     * Tries to get the HGNC standard name for the gene. If the name is already the standard name, returns the same name.
     * If it is an old or synonym name, returns the standard associated name from the HGNC database.
     *
     * @param gene gene name
     * @return standard gene name
     */
    private String getStandardName(String gene) {
        final String lowerCasedGene = gene.toLowerCase();
        for (DatabaseEntry hgncEntry : hgncDatabase.getUnmodifiableEntries()) {
            if (hgncEntry.getField(1).equalsIgnoreCase(gene)
                    || hgncEntry.getField(3).toLowerCase().contains(lowerCasedGene)
                    || hgncEntry.getField(4).toLowerCase().contains(lowerCasedGene))
                return hgncEntry.getField(1);
        }
        return null;
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
            AtomicInteger count = new AtomicInteger();
            round.set(i + 1);
            final long total = pearlDatabase.getPearls("gene").stream().filter(Pearl::isLeaf).count();
            pearlDatabase.getPearls("gene").stream().
                    filter(Pearl::isLeaf).
                    forEach(pearl -> {
                        connectToOmimPhenotypes(pearl);
                        connectToHPRDExpressions(pearl);
                        addRelationships(BioGridDatabase.getRelationships(pearl.getName()));
                        addRelationships(MenthaDatabase.getRelationships(pearl.getName()));
//                        addRelationships(HPRDDatabase.getRelationships(pearl.getName()));
                        pearl.setLeaf(false);
                        if (count.incrementAndGet() % 100 == 0)
                            updateMessage(String.format("Round %d of %d, %d/%d genes processed", round.get(), 2, count.get(), total));
                    });
        }
    }

    /**
     * Locate the related Omim phenotypes and creates a relationship to them when needed.
     *
     * @param pearl the gene pearl
     */
    private void connectToOmimPhenotypes(Pearl pearl) {
        omimDatabase.getUnmodifiableEntries().stream().
                filter(omimEntry -> omimEntry.getField(0).equals(pearl.getName())).
                map(omimEntry -> omimEntry.getField(3)).
                filter(disorders -> !disorders.equals(".")).
                forEach(disorders ->
                        Arrays.stream(disorders.split(";")).forEach(disorder -> linkGeneToDisorder(pearl, disorder)));
    }

    /**
     * Connect gene to Omim disorder
     *
     * @param pearl    the gene pearl
     * @param disorder the line of the disorder
     */
    private void linkGeneToDisorder(Pearl pearl, String disorder) {
        boolean isValid = false;
        for (String p : phenotypeKeyWords) if (disorder.toLowerCase().contains(p.toLowerCase())) isValid = true;
        if (isValid) {
            final String[] disorderFields = disorder.split("\\|");
            final String id = disorderFields[0] + "," + disorderFields[1];
            final Pearl phenotype = pearlDatabase.getOrCreate(id, "phenotype");
            phenotype.getProperties().put("name", disorderFields[0]);
            phenotype.getProperties().put("mimNumber", disorderFields[1]);
            final PearlRelationship relationshipTo = pearl.createRelationshipTo(phenotype);
            relationshipTo.setProperty("method", OMIM_METHOD.get(Integer.valueOf(disorderFields[2])));
        }
    }

    /**
     * Locate the hprd expressions associated to the gene, and create the relationships.
     *
     * @param pearl the gene pearl
     */
    private void connectToHPRDExpressions(Pearl pearl) {
        hprdExpressionDatabase.getUnmodifiableEntries().stream().
                filter(hprdEntry -> hprdEntry.getField(2).equals(pearl.getName())).
                forEach(hprdEntry -> linkGeneToHPRDExpression(pearl, hprdEntry));
    }

    /**
     * Connect one gene with one expression.
     *
     * @param pearl      gene pearl
     * @param expression the HPRD expression
     */
    private void linkGeneToHPRDExpression(Pearl pearl, DatabaseEntry expression) {
        boolean isValid = false;
        for (String p : phenotypeKeyWords)
            if (expression.getField(3).toLowerCase().contains(p.toLowerCase())) isValid = true;
        if (isValid) {
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
            relationships.stream().filter(relationship -> !GENE_BLACKLIST.contains(relationship.getSource()) && !GENE_BLACKLIST.contains(relationship.getTarget())).forEach(relationship -> {
                final Pearl source = pearlDatabase.getOrCreate(relationship.getSource(), "gene");
                final Pearl target = pearlDatabase.getOrCreate(relationship.getTarget(), "gene");
                updateRelationship(relationship, source, target);
            });
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
            cloneProperties(myRelationship, relationship);
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
        final List<PearlRelationship> sourceToTarget = source.getRelationships().get(target);
        if (sourceToTarget != null)
            for (PearlRelationship relationship : sourceToTarget)
                if (relationship.getProperties().get("id").equals(id)) return true;
        return false;
    }

    /**
     * Copy the properties from a StringRelationship to a PearlRelationship
     *
     * @param myRelationship the StringRelationship
     * @param relationship   the PearlRelationship
     */
    private void cloneProperties(StringRelationship myRelationship, PearlRelationship relationship) {
        myRelationship.getProperties().keySet().forEach(key -> relationship.getProperties().put(key, myRelationship.getProperties().get(key)));
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
        GraphScore graphScore = new GraphScore(pearlDatabase);
        graphScore.messageProperty().addListener((observable, oldValue, newValue) -> updateMessage(newValue));
        graphScore.run();
    }
}


