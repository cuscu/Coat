package coat.model.poirot;

import coat.model.vcfreader.Variant;
import javafx.concurrent.Task;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * This class is a Task (a long task) that analyzes a VCF file and a list of phenotype key words and returns a
 * PearlDatabase with the gene-gene-phenotype graph associated to the variants.
 * TODO: Separate in a new class the punctuation system.
 *
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class PoirotAnalysis extends Task<PearlDatabase> {


    private final List<String> phenotypeKeyWords;
    private final List<Variant> variants;

    private Map<String, List<Variant>> geneMap = new HashMap<>();
    private final PearlDatabase pearlDatabase = new PearlDatabase();

    private AtomicInteger round = new AtomicInteger();

    private final static List<String> GENE_BLACKLIST = new ArrayList<>();
    public static final Map<String, Double> CONSEQUENCE_SCORE = new HashMap<>();
    public static final Map<String, Double> RELATIONSHIP_SCORE = new HashMap<>();

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

    static {
        GENE_BLACKLIST.add("UBC");
        CONSEQUENCE_SCORE.put("transcript_ablation", 5.0);
        CONSEQUENCE_SCORE.put("splice_acceptor_variant", 5.0);
        CONSEQUENCE_SCORE.put("splice_donor_variant", 5.0);
        CONSEQUENCE_SCORE.put("stop_gained", 5.0);
        CONSEQUENCE_SCORE.put("frameshift_variant", 5.0);
        CONSEQUENCE_SCORE.put("stop_lost", 5.0);
        CONSEQUENCE_SCORE.put("transcript_amplification", 5.0);
        CONSEQUENCE_SCORE.put("inframe_insertion", 4.0);
        CONSEQUENCE_SCORE.put("inframe_deletion", 4.0);
        CONSEQUENCE_SCORE.put("missense_variant", 4.0);
        CONSEQUENCE_SCORE.put("protein_altering_variant", 4.0);
        CONSEQUENCE_SCORE.put("TFBS_ablation", 4.0);
        CONSEQUENCE_SCORE.put("regulatory_region_ablation", 4.0);
        CONSEQUENCE_SCORE.put("splice_region_variant", 2.0);
        CONSEQUENCE_SCORE.put("start_lost", 2.0);
        CONSEQUENCE_SCORE.put("incomplete_terminal_codon_variant", 2.0);
        CONSEQUENCE_SCORE.put("stop_retained_variant", 2.0);
        CONSEQUENCE_SCORE.put("synonymous_variant", 2.0);
        CONSEQUENCE_SCORE.put("coding_sequence_variant", 1.0);
        CONSEQUENCE_SCORE.put("mature_miRNA_variant", 1.0);
        CONSEQUENCE_SCORE.put("5_prime_UTR_variant", 1.0);
        CONSEQUENCE_SCORE.put("3_prime_UTR_variant", 1.0);
        CONSEQUENCE_SCORE.put("non_coding_transcript_exon_variant", 1.0);
        CONSEQUENCE_SCORE.put("intron_variant", 1.0);
        CONSEQUENCE_SCORE.put("NMD_transcript_variant", 1.0);
        CONSEQUENCE_SCORE.put("non_coding_transcript_variant", 1.0);
        CONSEQUENCE_SCORE.put("upstream_gene_variant", 1.0);
        CONSEQUENCE_SCORE.put("downstream_gene_variant", 1.0);
        CONSEQUENCE_SCORE.put("TFBS_amplification", 1.0);
        CONSEQUENCE_SCORE.put("TF_binding_site_variant", 1.0);
        CONSEQUENCE_SCORE.put("regulatory_region_amplification", 1.0);
        CONSEQUENCE_SCORE.put("feature_elongation", 1.0);
        CONSEQUENCE_SCORE.put("regulatory_region_variant", 1.0);
        CONSEQUENCE_SCORE.put("feature_truncation", 1.0);
        CONSEQUENCE_SCORE.put("intergenic_variant", 1.0);

        RELATIONSHIP_SCORE.put("direct interaction", 5.0);
        RELATIONSHIP_SCORE.put("physical association", 2.0);
        RELATIONSHIP_SCORE.put("additive genetic interaction defined by inequality", 2.0);
        RELATIONSHIP_SCORE.put("suppressive genetic interaction defined by inequality", 2.0);
        RELATIONSHIP_SCORE.put("synthetic genetic interaction defined by inequality", 2.0);
        RELATIONSHIP_SCORE.put("colocalization", 1.0);
        RELATIONSHIP_SCORE.put("association", 1.0);

        RELATIONSHIP_SCORE.put("phosphorylation reaction", 1.0);
        RELATIONSHIP_SCORE.put("dephosphorylation reaction", 1.0);
        RELATIONSHIP_SCORE.put("ubiquitination reaction", 1.0);
        RELATIONSHIP_SCORE.put("direct_interaction", 1.0);
        RELATIONSHIP_SCORE.put("cleavage reaction", 1.0);
        RELATIONSHIP_SCORE.put("physical_association", 1.0);
        RELATIONSHIP_SCORE.put("adp ribosylation reaction", 1.0);
        RELATIONSHIP_SCORE.put("enzymatic reaction", 1.0);
        RELATIONSHIP_SCORE.put("protein cleavage", 1.0);
        RELATIONSHIP_SCORE.put("methylation reaction", 1.0);
        RELATIONSHIP_SCORE.put("covalent binding", 1.0);
        RELATIONSHIP_SCORE.put("acetylation reaction", 1.0);
        RELATIONSHIP_SCORE.put("disulfide bond", 1.0);
        RELATIONSHIP_SCORE.put("protein_cleavage", 1.0);
        RELATIONSHIP_SCORE.put("Association", 1.0);
        RELATIONSHIP_SCORE.put("deubiquitination reaction", 1.0);
        RELATIONSHIP_SCORE.put("neddylation reaction", 1.0);
        RELATIONSHIP_SCORE.put("enzymatic_reaction", 1.0);
        RELATIONSHIP_SCORE.put("deacetylation reaction", 1.0);
        RELATIONSHIP_SCORE.put("hydroxylation reaction", 1.0);
        RELATIONSHIP_SCORE.put("gtpase reaction", 1.0);
        RELATIONSHIP_SCORE.put("oxidoreductase activity electron transfer reaction", 1.0);
        RELATIONSHIP_SCORE.put("palmitoylation reaction", 1.0);
        RELATIONSHIP_SCORE.put("rna cleavage", 1.0);
        RELATIONSHIP_SCORE.put("demethylation reaction", 1.0);
        RELATIONSHIP_SCORE.put("sumoylation reaction", 1.0);
        RELATIONSHIP_SCORE.put("phosphotransfer reaction", 1.0);
        RELATIONSHIP_SCORE.put("oxidoreductase activity electron transfer assay", 1.0);
        RELATIONSHIP_SCORE.put("cleavage_reaction", 1.0);
        RELATIONSHIP_SCORE.put("proline isomerization  reaction", 1.0);
        RELATIONSHIP_SCORE.put("transglutamination_reaction", 1.0);
        RELATIONSHIP_SCORE.put("isomerase reaction", 1.0);
        RELATIONSHIP_SCORE.put("genetic inequality", 1.0);
        RELATIONSHIP_SCORE.put("deneddylation reaction", 1.0);
        RELATIONSHIP_SCORE.put("glycosylation reaction", 1.0);
        RELATIONSHIP_SCORE.put("dna strand elongation", 1.0);
    }

    /**
     * Creates a new PoirotAnalysis task, ready to be inserted in a Thread, or launch with <code>Paltform</code>
     *
     * @param variants
     * @param phenotypes
     */
    public PoirotAnalysis(List<Variant> variants, List<String> phenotypes) {
        this.variants = variants;
        this.phenotypeKeyWords = phenotypes;
    }

    @Override
    protected PearlDatabase call() throws Exception {
        try {
            mapVariantsToGenes();
            return secondTry();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void printDatabase() {
        pearlDatabase.getPearls("gene").forEach((pearl) -> {
            System.out.println(pearl);
            pearl.getRelationships().forEach((otherPearl, pearlRelationships) ->
                    pearlRelationships.stream().filter(relationship -> relationship.getSource().equals(pearl)).
                            forEach(System.out::println));
        });
    }

    private void mapVariantsToGenes() {
        updateMessage("Reading variants");
        int total = variants.size();
        int count = 0;
        for (Variant variant : variants) {
            final String gene = (String) variant.getInfos().get("GNAME");
            if (gene != null) {
                String GENE = getStandardName(gene);
                if (GENE == null) GENE = gene.toUpperCase();
                List<Variant> vs = geneMap.get(GENE);
                if (vs == null) {
                    vs = new ArrayList<>();
                    geneMap.put(GENE, vs);
                }
                vs.add(variant);
            }
            if (++count % 1000 == 0) updateMessage(String.format("Reading variants %d/%d", count, total));
        }
    }

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

    private PearlDatabase secondTry() {
        addInitialGenes();
        expandGraph();
        setDistances();
        cleanDatabase();
        setScores();
        return pearlDatabase;
    }

    private void addInitialGenes() {
        geneMap.keySet().forEach(gene -> {
            final Pearl pearl = pearlDatabase.getOrCreate(gene, "gene");
            final List<Variant> vs = geneMap.get(gene);
            pearl.getProperties().put("variants", vs);
        });
    }

    private void expandGraph() {
        AtomicInteger count = new AtomicInteger();
        for (int i = 0; i < 2; i++) {
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

    private void connectToOmimPhenotypes(Pearl pearl) {
        omimDatabase.getUnmodifiableEntries().stream().
                filter(omimEntry -> omimEntry.getField(0).equals(pearl.getName())).
                map(omimEntry -> omimEntry.getField(3)).
                filter(disorders -> !disorders.equals(".")).
                forEach(disorders ->
                        Arrays.stream(disorders.split(";")).forEach(disorder -> linkGeneToDisorder(pearl, disorder)));
    }

    private void connectToHPRDExpressions(Pearl pearl) {
        hprdExpressionDatabase.getUnmodifiableEntries().stream().
                filter(hprdEntry -> hprdEntry.getField(2).equals(pearl.getName())).
                forEach(hprdEntry -> linkGeneToExpression(pearl, hprdEntry));
    }

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
            relationshipTo.setProperty("method", disorderFields[2]);
        }
    }

    private void linkGeneToExpression(Pearl pearl, DatabaseEntry expression) {
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

    private void addRelationships(List<StringRelationship> relationships) {
        if (relationships != null)
            relationships.stream().filter(relationship -> !GENE_BLACKLIST.contains(relationship.getSource()) && !GENE_BLACKLIST.contains(relationship.getTarget())).forEach(relationship -> {
                final Pearl source = pearlDatabase.getOrCreate(relationship.getSource(), "gene");
                final Pearl target = pearlDatabase.getOrCreate(relationship.getTarget(), "gene");
                updateRelationship(relationship, source, target);
            });
    }

    private void updateRelationship(StringRelationship myRelationship, Pearl source, Pearl target) {
        String id = (String) myRelationship.getProperties().get("id");
        if (!relationshipExists(source, target, id)) {
            PearlRelationship relationship = new PearlRelationship(source, target);
            cloneProperties(myRelationship, relationship);
        }
    }

    private boolean relationshipExists(Pearl source, Pearl target, String id) {
        final List<PearlRelationship> sourceToTarget = source.getRelationships().get(target);
        if (sourceToTarget != null)
            for (PearlRelationship relationship : sourceToTarget)
                if (relationship.getProperties().get("id").equals(id)) return true;
        return false;
    }

    private void cloneProperties(StringRelationship myRelationship, PearlRelationship relationship) {
        myRelationship.getProperties().keySet().forEach(key -> relationship.getProperties().put(key, myRelationship.getProperties().get(key)));
    }

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

    private void cleanDatabase() {
        pearlDatabase.getPearls("gene").stream().filter(pearl -> pearl.getDistanceToPhenotype() < 0).forEach(pearlDatabase::remove);
    }

    private void setScores() {
        final AtomicInteger count = new AtomicInteger();
        pearlDatabase.getPearls("gene").parallelStream().forEach((pearl) -> {
            if (count.incrementAndGet() % 100 == 0)
                updateMessage(String.format("Scoring gene %d/%d", count.get(), pearlDatabase.pearls("gene")));
            setScore(pearl);
        });
    }

    private void setScore(Pearl pearl) {
        final double variantScore = getVariantScore(pearl);
        final double pathScore = getPathScore(pearl);
        pearl.setScore((variantScore + (1.0 + pathScore) / (pearl.getDistanceToPhenotype() * pearl.getDistanceToPhenotype())));
    }

    private double getVariantScore(Pearl pearl) {
        final List<Variant> variants = (List<Variant>) pearl.getProperties().get("variants");
        return variants != null ? consequenceScore(variants) : 0.0;
    }

    private double consequenceScore(List<Variant> variants) {
        try {
            final List<String> consequences = getConsequences(variants);
            return consequences.stream().map(cons -> CONSEQUENCE_SCORE.getOrDefault(cons, 0.0)).max(Double::compare).get();
        } catch (NoSuchElementException ex) {
            return 0.0;
        }
    }

    private List<String> getConsequences(List<Variant> variants) {
        final List<String> consequences = new ArrayList<>();
        for (Variant variant : variants) {
            final String cons = (String) variant.getInfos().get("CONS");
            if (cons != null) Collections.addAll(consequences, cons.split(", "));
        }
        return consequences;
    }

    private double getPathScore(Pearl pearl) {
        final List<List<PearlRelationship>> paths = ShortestPath.getShortestPaths(pearl);
        return paths.stream().map(this::computePathScore).max(Double::compare).get();
    }

    private double computePathScore(List<PearlRelationship> path) {
        return path.stream().
                map(relationship -> (String) relationship.getProperties().get("type")).
                filter(type -> type != null).
                map(type -> RELATIONSHIP_SCORE.getOrDefault(type, 0.0)).
                reduce(0.0, Double::sum);
    }

}


