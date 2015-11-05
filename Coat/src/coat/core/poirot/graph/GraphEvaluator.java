/******************************************************************************
 * Copyright (C) 2015 UICHUIMI                                                *
 *                                                                            *
 * This program is free software: you can redistribute it and/or modify it    *
 * under the terms of the GNU General Public License as published by the      *
 * Free Software Foundation, either version 3 of the License, or (at your     *
 * option) any later version.                                                 *
 *                                                                            *
 * This program is distributed in the hope that it will be useful, but        *
 * WITHOUT ANY WARRANTY; without even the implied warranty of                 *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                       *
 * See the GNU General Public License for more details.                       *
 *                                                                            *
 * You should have received a copy of the GNU General Public License          *
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.      *
 ******************************************************************************/

package coat.core.poirot.graph;

import coat.core.poirot.Pearl;
import coat.core.poirot.PearlGraph;
import coat.core.poirot.PearlRelationship;
import coat.core.poirot.ShortestPath;
import coat.core.vcf.Variant;
import javafx.concurrent.Task;

import java.util.*;

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
        CONSEQUENCE_SCORE.put("transcript_ablation", 1.0);
        CONSEQUENCE_SCORE.put("splice_acceptor_variant", 1.0);
        CONSEQUENCE_SCORE.put("splice_donor_variant", 1.0);
        CONSEQUENCE_SCORE.put("stop_gained", 1.0);
        CONSEQUENCE_SCORE.put("frameshift_variant", 1.0);
        CONSEQUENCE_SCORE.put("stop_lost", 1.0);
        CONSEQUENCE_SCORE.put("transcript_amplification", 1.0);
        CONSEQUENCE_SCORE.put("inframe_insertion", 0.8);
        CONSEQUENCE_SCORE.put("inframe_deletion", 0.8);
        CONSEQUENCE_SCORE.put("missense_variant", 0.8);
        CONSEQUENCE_SCORE.put("protein_altering_variant", 0.8);
        CONSEQUENCE_SCORE.put("TFBS_ablation", 0.8);
        CONSEQUENCE_SCORE.put("regulatory_region_ablation", 0.8);
        CONSEQUENCE_SCORE.put("splice_region_variant", 0.4);
        CONSEQUENCE_SCORE.put("start_lost", 0.4);
        CONSEQUENCE_SCORE.put("incomplete_terminal_codon_variant", 0.4);
        CONSEQUENCE_SCORE.put("stop_retained_variant", 0.4);
        CONSEQUENCE_SCORE.put("synonymous_variant", 0.4);
        CONSEQUENCE_SCORE.put("coding_sequence_variant", 0.2);
        CONSEQUENCE_SCORE.put("mature_miRNA_variant", 0.2);
        CONSEQUENCE_SCORE.put("5_prime_UTR_variant", 0.2);
        CONSEQUENCE_SCORE.put("3_prime_UTR_variant", 0.2);
        CONSEQUENCE_SCORE.put("non_coding_transcript_exon_variant", 0.2);
        CONSEQUENCE_SCORE.put("intron_variant", 0.2);
        CONSEQUENCE_SCORE.put("NMD_transcript_variant", 0.2);
        CONSEQUENCE_SCORE.put("non_coding_transcript_variant", 0.2);
        CONSEQUENCE_SCORE.put("upstream_gene_variant", 0.2);
        CONSEQUENCE_SCORE.put("downstream_gene_variant", 0.2);
        CONSEQUENCE_SCORE.put("TFBS_amplification", 0.2);
        CONSEQUENCE_SCORE.put("TF_binding_site_variant", 0.2);
        CONSEQUENCE_SCORE.put("regulatory_region_amplification", 0.2);
        CONSEQUENCE_SCORE.put("feature_elongation", 0.2);
        CONSEQUENCE_SCORE.put("regulatory_region_variant", 0.2);
        CONSEQUENCE_SCORE.put("feature_truncation", 0.2);
        CONSEQUENCE_SCORE.put("intergenic_variant", 0.2);

        // type from BioGrid
        RELATIONSHIP_SCORE.put("direct interaction", 1.0);
        RELATIONSHIP_SCORE.put("physical association", 0.4);
        RELATIONSHIP_SCORE.put("additive genetic interaction defined by inequality", 0.4);
        RELATIONSHIP_SCORE.put("suppressive genetic interaction defined by inequality", 0.4);
        RELATIONSHIP_SCORE.put("synthetic genetic interaction defined by inequality", 0.4);
        RELATIONSHIP_SCORE.put("colocalization", 0.2);
        RELATIONSHIP_SCORE.put("association", 0.2);

        // method from mentha
        RELATIONSHIP_SCORE.put("phosphorylation reaction", 0.2);
        RELATIONSHIP_SCORE.put("dephosphorylation reaction", 0.2);
        RELATIONSHIP_SCORE.put("ubiquitination reaction", 0.2);
        RELATIONSHIP_SCORE.put("direct_interaction", 0.2);
        RELATIONSHIP_SCORE.put("cleavage reaction", 0.2);
        RELATIONSHIP_SCORE.put("physical_association", 0.2);
        RELATIONSHIP_SCORE.put("adp ribosylation reaction", 0.2);
        RELATIONSHIP_SCORE.put("enzymatic reaction", 0.2);
        RELATIONSHIP_SCORE.put("protein cleavage", 0.2);
        RELATIONSHIP_SCORE.put("methylation reaction", 0.2);
        RELATIONSHIP_SCORE.put("covalent binding", 0.2);
        RELATIONSHIP_SCORE.put("acetylation reaction", 0.2);
        RELATIONSHIP_SCORE.put("disulfide bond", 0.2);
        RELATIONSHIP_SCORE.put("protein_cleavage", 0.2);
        RELATIONSHIP_SCORE.put("Association", 0.2);
        RELATIONSHIP_SCORE.put("deubiquitination reaction", 0.2);
        RELATIONSHIP_SCORE.put("neddylation reaction", 0.2);
        RELATIONSHIP_SCORE.put("enzymatic_reaction", 0.2);
        RELATIONSHIP_SCORE.put("deacetylation reaction", 0.2);
        RELATIONSHIP_SCORE.put("hydroxylation reaction", 0.2);
        RELATIONSHIP_SCORE.put("gtpase reaction", 0.2);
        RELATIONSHIP_SCORE.put("oxidoreductase activity electron transfer reaction", 0.2);
        RELATIONSHIP_SCORE.put("palmitoylation reaction", 0.2);
        RELATIONSHIP_SCORE.put("rna cleavage", 0.2);
        RELATIONSHIP_SCORE.put("demethylation reaction", 0.2);
        RELATIONSHIP_SCORE.put("sumoylation reaction", 0.2);
        RELATIONSHIP_SCORE.put("phosphotransfer reaction", 0.2);
        RELATIONSHIP_SCORE.put("oxidoreductase activity electron transfer assay", 0.2);
        RELATIONSHIP_SCORE.put("cleavage_reaction", 0.2);
        RELATIONSHIP_SCORE.put("proline isomerization  reaction", 0.2);
        RELATIONSHIP_SCORE.put("transglutamination_reaction", 0.2);
        RELATIONSHIP_SCORE.put("isomerase reaction", 0.2);
        RELATIONSHIP_SCORE.put("genetic inequality", 0.2);
        RELATIONSHIP_SCORE.put("deneddylation reaction", 0.2);
        RELATIONSHIP_SCORE.put("glycosylation reaction", 0.2);
        RELATIONSHIP_SCORE.put("dna strand elongation", 0.2);

        RELATIONSHIP_SCORE.put("linkage", 0.2);
        RELATIONSHIP_SCORE.put("mutation", 0.4);
        RELATIONSHIP_SCORE.put("deletion or duplication", 0.6);

        // Confidence in Omim
        RELATIONSHIP_SCORE.put("Confirmed", 1.0);
        RELATIONSHIP_SCORE.put("Provisional", 0.6);
        RELATIONSHIP_SCORE.put("Inconsistent", 0.4);
        RELATIONSHIP_SCORE.put("Limbo", 0.2);

        // OMIM
        RELATIONSHIP_SCORE.put("General", 0.4);
        RELATIONSHIP_SCORE.put("Isoform_specific", 0.8);

    }

    private final PearlGraph database;
    private final List<String> phenotypes;
    private int processed;
    private int total;

    public GraphEvaluator(PearlGraph database, List<String> phenotypes) {
        this.database = database;
        this.phenotypes = phenotypes;
    }

    @Override
    protected Void call() throws Exception {
        try {
            clear();
            activatePhenotypes();
            setDistances();
            setUpScores();
            updateMessage("Completed");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void clear() {
        database.getPearls(Pearl.Type.GENE).parallelStream().forEach(pearl -> {
            pearl.setDistanceToPhenotype(-1);
            pearl.setScore(0.0);
        });
        database.getPearls(Pearl.Type.DISEASE).parallelStream().forEach(pearl -> {
            pearl.setDistanceToPhenotype(0);
            pearl.setScore(0.0);
        });
        database.getPearls(Pearl.Type.EXPRESSION).parallelStream().forEach(pearl -> {
            pearl.setDistanceToPhenotype(0);
            pearl.setScore(0.0);
        });
        total = database.numberOfPearls(Pearl.Type.GENE);
        processed = 0;
    }

    private void activatePhenotypes() {
        database.getPearls(Pearl.Type.EXPRESSION).stream().forEach(pearl -> pearl.setActive(phenotypes.contains(pearl.getName())));
        database.getPearls(Pearl.Type.DISEASE).stream().forEach(pearl -> pearl.setActive(phenotypes.contains(pearl.getName())));
    }

    private void setDistances() {
        final List<Pearl> phenotypePearls = new ArrayList<>();
        database.getPearls(Pearl.Type.DISEASE).forEach(phenotypePearls::add);
        database.getPearls(Pearl.Type.EXPRESSION).forEach(phenotypePearls::add);
        phenotypePearls.stream()
                .filter(Pearl::isActive)
                .forEach(pearl -> setDistance(pearl, 0));
    }

    private static void setDistance(Pearl pearl, int distance) {
        pearl.setDistanceToPhenotype(distance);
        if (distance > MAX_DISTANCE) return;
        pearl.getRelationships().keySet().stream()
                .filter(neighbour -> neighbour.getType() == Pearl.Type.GENE)
                .filter(neighbour -> neighbour.getDistanceToPhenotype() == -1 || neighbour.getDistanceToPhenotype() > distance + 1)
                .forEach(neighbour -> setDistance(neighbour, distance + 1));
    }

    private void setUpScores() {
        final List<Pearl> phenotypePearls = new ArrayList<>();
        database.getPearls(Pearl.Type.DISEASE).forEach(phenotypePearls::add);
        database.getPearls(Pearl.Type.EXPRESSION).forEach(phenotypePearls::add);
        phenotypePearls.stream().forEach(this::score);
    }

    /**
     * Computes the score of a single pearl.
     * <p>
     * score = max(v_i) + (max(p_j) / d^2)
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
//    private void score(Pearl pearl) {
//        updateProgress(pearl);
//        final double variantScore = getVariantScore(pearl);
//        final double pathScore = getPathScore(pearl);
//        pearl.setScore(variantScore + pathScore / (pearl.getDistanceToPhenotype() * pearl.getDistanceToPhenotype()));
//    }
    private void score(Pearl pearl) {
        if (pearl.getScore() > 0) return;
        updateProgress(pearl);
        final double vScore = getVariantScore(pearl);
        final double maxNScore = getMaxNeighbourScore(pearl);
        if (pearl.getType() == Pearl.Type.GENE)
            pearl.setScore((0.8 * vScore + 0.2 * maxNScore) / pearl.getDistanceToPhenotype());
        else pearl.setScore(maxNScore);
    }

    private double getMaxNeighbourScore(Pearl pearl) {
        return pearl.getRelationships().entrySet().stream()
                .filter(entry -> entry.getKey().getDistanceToPhenotype() > pearl.getDistanceToPhenotype())
                .mapToDouble(this::getNeighbourScore).max().orElse(0.0);
    }

    private double getNeighbourScore(Map.Entry<Pearl, List<PearlRelationship>> entry) {
        score(entry.getKey());
        final double maxRelationshipScore = getMaxRelationshipScore(entry);
        return entry.getKey().getScore() * maxRelationshipScore;
    }

    private double getMaxRelationshipScore(Map.Entry<Pearl, List<PearlRelationship>> entry) {
        return entry.getValue().stream()
                .mapToDouble(this::getRelationshipScore)
                .max().orElse(0.0);
    }

    private void updateProgress(Pearl pearl) {
        if (++processed % 100 == 0) {
            updateMessage(String.format("%d/%d %s", processed, total, pearl.getName()));
            updateProgress(processed, total);
        }
    }

    private double getVariantScore(Pearl pearl) {
        final List<Variant> variants = (List<Variant>) pearl.getProperties().get("variants");
        return variants == null ? 0.0 : getMaxVariantScore(variants);
    }

    private double getMaxVariantScore(List<Variant> variants) {
        return variants.stream().mapToDouble(variant -> 0.5 * getConsequenceScore(variant) + 0.5 * getSiftScore(variant)).max().orElse(0.0);
    }

    private double getSiftScore(Variant variant) {
        final String sifTs = variant.getInfo("SIFTs");
        if (sifTs == null) return 0;
        try {
            return 1 - Double.valueOf(sifTs);
        } catch (NumberFormatException ignored) {
        }
        return 0;
    }

    private double getRelationshipScore(PearlRelationship relationship) {
        final String type = getRelationshipType(relationship);
        if (type != null) return RELATIONSHIP_SCORE.getOrDefault(type, 0.0);
        return 0;
    }

    private double getConsequenceScore(Variant variant) {
        final String cons = variant.getInfo("CONS");
        if (cons != null) {
            return Arrays.stream(cons.split(","))
                    .mapToDouble(consequence -> CONSEQUENCE_SCORE.getOrDefault(consequence, 0.0))
                    .max().orElse(0.0);
        }
        return 0;
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
        if (relationship.getProperties().containsKey("type"))
            return (String) relationship.getProperties().get("type");
        if (relationship.getProperties().containsKey("method"))
            return (String) relationship.getProperties().get("method");
        if (relationship.getProperties().containsKey("confidence"))
            return (String) relationship.getProperties().get("confidence");
        if (relationship.getProperties().containsKey("status"))
            return (String) relationship.getProperties().get("status");
        return null;
    }

}
