package coat.model.poirot;

import coat.model.vcf.Variant;
import javafx.concurrent.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    static {
        BLACKLIST.add("UBC");
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
            String gene = (String) variant.getInfos().get("GNAME");
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
//        source.getOutRelationships().add(relationship);
//        target.getInRelationships().add(relationship);
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
            pearl.setWeight(0);
            pearl.getRelationships().keySet().forEach(otherPearl -> otherPearl.setWeight(1));
//            pearl.getInRelationships().stream().map(PearlRelationship::getSource).forEach(source -> source.setWeight(1));
        });
        final int[] weight = {2};
        for (int i = 0; i < 3; i++) {
            final List<Pearl> wave = pearlDatabase.getPearls("gene").stream().filter(gene -> gene.getWeight() > 0).collect(Collectors.toList());
            wave.forEach(pearl -> {
                pearl.getRelationships().forEach((otherPearl, relationships) -> {
                    if (otherPearl.getWeight() < 0) otherPearl.setWeight(weight[0]);
                });
//                pearl.getInRelationships().stream().map(PearlRelationship::getSource).filter(source -> source.getWeight() < 0).forEach(source -> source.setWeight(weight[0]));
//                pearl.getOutRelationships().stream().map(PearlRelationship::getTarget).filter(target -> target.getWeight() < 0).forEach(target -> target.setWeight(weight[0]));
            });
            weight[0]++;
        }
    }

    private void cleanDatabase() {
        System.out.println("Cleaning");
//        printDatabase();
        pearlDatabase.getPearls("gene").stream().filter(pearl -> pearl.getWeight() < 0).forEach(pearlDatabase::remove);
    }

}


