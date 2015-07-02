package coat.model.poirot;

import coat.model.vcfreader.Variant;
import javafx.concurrent.Task;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class PoirotAnalysis2 extends Task<PearlDatabase> {


    private final List<String> phenotypes;
    private final List<Variant> variants;
    private final List<String> genes = new ArrayList<>();

    private final PearlDatabase pearlDatabase = new PearlDatabase();
    private final Map<String, List<String>> phenotypeGenes = new HashMap<>();
    private Map<String, List<Variant>> geneMap = new HashMap<>();

    private AtomicInteger round = new AtomicInteger();

    private final static List<String> BLACKLIST = new ArrayList<>();
    public static final Map<String, Double> CONSEQUENCE_SCORE = new HashMap<>();
    public static final Map<String, Double> RELATIONSHIP_SCORE = new HashMap<>();

    static {
        BLACKLIST.add("UBC");
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
    }

    public PoirotAnalysis2(List<Variant> variants, List<String> phenotypes) {
        this.variants = variants;
        this.phenotypes = phenotypes;
    }

    @Override
    protected PearlDatabase call() throws Exception {
        try {
            mapVariantsToGenes();
            phenotypes.forEach(phenotype -> {
                final List<String> realPhenotyes = Omim.getPhenotypes(phenotype);
                realPhenotyes.forEach(name -> phenotypeGenes.put(name, Omim.getRelatedGenes(name)));
            });
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
        for (Variant variant : variants) {
            final String gene = (String) variant.getInfos().get("GNAME");
            if (gene != null) {
                final String GENE = gene.toUpperCase();
                List<Variant> vs = geneMap.get(GENE);
                if (vs == null) {
                    vs = new ArrayList<>();
                    geneMap.put(GENE, vs);
                }
                vs.add(variant);
                if (!genes.contains(GENE)) genes.add(GENE);
            }
        }
    }

    private PearlDatabase secondTry() {
        initializeDatabase();
        expandGraph();
        setWeights();
        cleanDatabase();
        setScores();
        return pearlDatabase;
    }

    private void initializeDatabase() {
        addInitialPhenotypes();
        addInitialGenes();
    }

    private void addInitialGenes() {
        genes.forEach(gene -> {
            final Pearl pearl = pearlDatabase.getOrCreate(gene, "gene");
            final List<Variant> vs = geneMap.get(gene);
            pearl.getProperties().put("variants", vs);
        });
    }

    private void addInitialPhenotypes() {
        phenotypeGenes.keySet().forEach(phenotype -> {
            final Pearl pearl = pearlDatabase.getOrCreate(phenotype, "phenotype");
        });
    }

    private void expandGraph() {
        for (int i = 0; i < 2; i++) {
            round.set(i + 1);
            final List<String> leafGenes = getLeafGenes();
            expandGenes(leafGenes);
        }
    }

    private List<String> getLeafGenes() {
        return pearlDatabase.getPearls("gene").stream().
                filter(Pearl::isLeaf).
                map(Pearl::getName).
                collect(Collectors.toList());
    }

    private void expandGenes(List<String> genes) {
        connectWithLocalGenes(genes);
        connectWithPhenotypes(genes);
        unLeaf(genes);
    }

    private void connectWithLocalGenes(List<String> genes) {
        AtomicInteger counter = new AtomicInteger();
        genes.forEach(geneName -> {
            if (counter.incrementAndGet() % 100 == 0) {
                updateMessage(String.format("Round %d/%d, %d/%d genes", round.get(), 2, counter.get(), genes.size()));
            }
            addBioGridRelationships(geneName);
        });
    }

    private void addBioGridRelationships(String geneName) {
        final List<StringRelationship> relationships = BioGridDatabase.getRelationships(geneName);
        if (relationships != null)
            relationships.stream().filter(relationship -> !BLACKLIST.contains(relationship.getSource()) && !BLACKLIST.contains(relationship.getTarget())).forEach(relationship -> {
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
            source.addRelationship(target, relationship);
            target.addRelationship(source, relationship);
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

    private void connectWithPhenotypes(List<String> genes) {
        for (String phenotype : phenotypeGenes.keySet())
            genes.stream().filter(gene -> phenotypeGenes.get(phenotype).contains(gene)).forEach(gene -> connect(gene, phenotype));
    }

    private void connect(String gene, String phenotype) {
        final Pearl genePearl = pearlDatabase.getPearl(gene, "gene");
        final Pearl phenotypePearl = pearlDatabase.getPearl(phenotype, "phenotype");
        if (phenotypePearl == null || genePearl == null) return;
        PearlRelationship relationship = findRelationship(genePearl, phenotypePearl);
        if (relationship != null) {
            int total = (int) relationship.getProperty("total");
            relationship.setProperty("total", total + 1);
        } else {
            PearlRelationship pearlRelationship = new PearlRelationship(genePearl, phenotypePearl);
            pearlRelationship.getProperties().put("total", 1);
            genePearl.addRelationship(phenotypePearl, pearlRelationship);
            phenotypePearl.addRelationship(genePearl, pearlRelationship);
        }
    }

    private PearlRelationship findRelationship(Pearl source, Pearl target) {
        final List<PearlRelationship> relationships = source.getRelationships().get(target);
        if (relationships != null) {
            for (PearlRelationship relationship : relationships) {
                if (relationship.getTarget().equals(target)) return relationship;
            }
        }
        return null;
    }

    private void unLeaf(List<String> genes) {
        genes.stream().map(gene -> pearlDatabase.getPearl(gene, "gene")).filter(pearl -> pearl != null).forEach(pearl -> pearl.setLeaf(false));
    }

    private void setWeights() {
        pearlDatabase.getPearls("phenotype").forEach(pearl -> {
            pearl.setDistanceToPhenotype(0);
            pearl.getRelationships().keySet().forEach(otherPearl -> otherPearl.setDistanceToPhenotype(1));
        });
        final int[] weight = {2};
        for (int i = 0; i < 3; i++) {
            final List<Pearl> wave = pearlDatabase.getPearls("gene").stream().filter(gene -> gene.getDistanceToPhenotype() > 0).collect(Collectors.toList());
            wave.forEach(pearl -> pearl.getRelationships().forEach((otherPearl, relationships) -> {
                if (otherPearl.getDistanceToPhenotype() < 0) otherPearl.setDistanceToPhenotype(weight[0]);
            }));
            weight[0]++;
        }
    }

    private void cleanDatabase() {
        pearlDatabase.getPearls("gene").stream().filter(pearl -> pearl.getDistanceToPhenotype() < 0).forEach(pearlDatabase::remove);
    }

    private void setScores() {
        pearlDatabase.getPearls("gene").forEach(this::setScore);
    }

    private void setScore(Pearl pearl) {
        double variantScore = getVariantScore(pearl);
        double pathScore = getPathScore(pearl);
        pearl.setScore((variantScore + (1.0 + pathScore) / (pearl.getDistanceToPhenotype() * pearl.getDistanceToPhenotype())));
    }

    private double getVariantScore(Pearl pearl) {
        final List<Variant> variants = (List<Variant>) pearl.getProperties().get("variants");
        return variants != null ? consequenceScore(variants) : 0.0;
    }

    private double consequenceScore(List<Variant> variants) {
        final List<String> consequences = getConsequences(variants);
        return consequences.stream().map(cons -> CONSEQUENCE_SCORE.getOrDefault(cons, 0.0)).max(Double::compare).get();
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


