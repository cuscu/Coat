package coat.model.poirot;

import coat.model.vcfreader.Variant;
import javafx.concurrent.Task;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Given a PearlDatabase, set the score value for each gene pearl, according to the consequences of its variant, their
 * distance to the nearest active phenotype and the type of relationships between them and the nearest phenotype.
 * <p>
 * The score of a single Pearl is:
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
 * <p>
 * CONSEQUENCE_SCORE and RELATIONSHIP_SCORE are Maps that contain each possible value in the relationship or
 * consequence as keys and a number from 1.0 to 5.0 as values.
 *
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class GraphEvaluator extends Task<Void> {

    private static final int MAX_DISTANCE = 20;
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

    private final PearlDatabase database;
    private final List<String> phenotypes;

    public GraphEvaluator(PearlDatabase database, List<String> phenotypes) {
        this.database = database;
        this.phenotypes = phenotypes;
    }

    @Override
    protected Void call() throws Exception {
        try {
            clear();
            activatePhenotypes();
            setDistances();
            database.getPearls("gene").forEach(this::score);
            System.err.println("done");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void activatePhenotypes() {
        database.getPearls("phenotype").stream().forEach(pearl -> pearl.setActive(phenotypes.contains(pearl.getName())));
    }

    private void setDistances() {
        database.getPearls("phenotype")
                .stream()
                .filter(Pearl::isActive)
                .forEach(pearl -> expand(pearl, 0));
    }

    private static void expand(Pearl pearl, int distance) {
        pearl.setDistanceToPhenotype(distance);
        if (distance >  MAX_DISTANCE) return;
        pearl.getRelationships().keySet().stream()
                .filter(neighbour -> neighbour.getType().equals("gene"))
                .filter(neighbour -> neighbour.getDistanceToPhenotype() == -1 || neighbour.getDistanceToPhenotype() > distance + 1)
                .forEach(neighbour -> expand(neighbour, distance + 1));
    }

    private void clear() {
        database.getPearls("gene").parallelStream().forEach(pearl -> {
            pearl.setScore(0.0);
            pearl.setDistanceToPhenotype(-1);
        });
        database.getPearls("phenotype").forEach(pearl -> pearl.setDistanceToPhenotype(0));
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
     * @param pearl the pearl to score
     */
    private void score(Pearl pearl) {
        final double variantScore = getVariantScore(pearl);
        final double pathScore = getPathScore(pearl);
        pearl.setScore((variantScore + (pathScore) / (pearl.getDistanceToPhenotype() * pearl.getDistanceToPhenotype())));
    }

    private double getVariantScore(Pearl pearl) {
        final List<Variant> variants = (List<Variant>) pearl.getProperties().get("variants");
        return variants == null ? 0.0 : consequenceScore(variants);
    }

    private double consequenceScore(List<Variant> variants) {
        return variants.stream()
                .map(variant -> (String) variant.getInfos().get("CONS"))
                .filter(cons -> cons != null)
                .flatMap(cons -> Arrays.stream(cons.split(", ")))
                .mapToDouble(consequence -> CONSEQUENCE_SCORE.getOrDefault(consequence, 0.0))
                .max().orElse(0.0);
    }

    private double getPathScore(Pearl pearl) {
        return ShortestPath.getPaths(pearl).stream()
                .map(this::computePathScore)
                .max(Double::compare).orElse(0.0);
    }

    private double computePathScore(List<PearlRelationship> path) {
        return path.stream()
                .map(this::getRelationshipType)
                .mapToDouble(type -> RELATIONSHIP_SCORE.getOrDefault(type, 0.0))
                .sum();
    }

    private String getRelationshipType(PearlRelationship relationship) {
        final String type = (String) relationship.getProperties().get("type");
        return type != null ? type : (String) relationship.getProperties().get("method");
    }

}
