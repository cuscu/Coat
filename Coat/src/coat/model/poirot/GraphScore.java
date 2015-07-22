package coat.model.poirot;

import coat.model.vcfreader.Variant;
import javafx.concurrent.Task;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class GraphScore extends Task<Integer> {

    private PearlDatabase pearlDatabase;

    public static final Map<String, Double> CONSEQUENCE_SCORE = new HashMap<>();
    public static final Map<String, Double> RELATIONSHIP_SCORE = new HashMap<>();

    static {
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

        RELATIONSHIP_SCORE.put("linkage", 1.0);
        RELATIONSHIP_SCORE.put("mutation", 2.0);
        RELATIONSHIP_SCORE.put("deletion or duplication", 3.0);
    }

    /**
     * Creates a new GraphScore.
     *
     * @param pearlDatabase pearlDatabase to score
     */
    public GraphScore(PearlDatabase pearlDatabase) {
        this.pearlDatabase = pearlDatabase;
    }

    /**
     * Starts the scoring.
     *
     * @return always 0
     * @throws Exception
     */
    @Override
    protected Integer call() throws Exception {
        final AtomicInteger count = new AtomicInteger();
        pearlDatabase.getPearls("gene").parallelStream().forEach((pearl) -> {
            if (count.incrementAndGet() % 100 == 0)
                updateMessage(String.format("Scoring gene %d/%d", count.get(), pearlDatabase.pearls("gene")));
            setScore(pearl);
        });
        return 0;
    }

    /**
     * Computes the score of a single pearl.
     * <p>
     * score = max(v_i) + (1 + max(p_j) / d^2)
     * <p>
     * v_i = CONSEQUENCE_SCORE(variant(i).get("CONS"))
     * <p>
     * p_j = paths(j) = sum(r_k)
     * <p>
     * r_k = RELATIONSHIP_SCORE(relationships(k).type)
     * <p>
     * d = distance to phenotype
     *
     * @param pearl
     */
    private void setScore(Pearl pearl) {
        final double variantScore = getVariantScore(pearl);
        final double pathScore = getPathScore(pearl);
        pearl.setScore((variantScore + (pathScore) / (pearl.getDistanceToPhenotype() * pearl.getDistanceToPhenotype())));
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
        double score = 0.0;
        for (PearlRelationship relationship : path) {
            String type = (String) relationship.getProperties().get("type");
            if (type == null) type = (String) relationship.getProperties().get("method");
            if (type != null) score += RELATIONSHIP_SCORE.getOrDefault(type, 0.0);
        }
        return score;
//        return path.stream().
//                map(relationship -> (String) relationship.getProperties().get("type")).
//                filter(type -> type != null).
//                map(type -> RELATIONSHIP_SCORE.getOrDefault(type, 0.0)).
//                reduce(0.0, Double::sum);
    }
}
