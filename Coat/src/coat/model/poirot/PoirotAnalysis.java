package coat.model.poirot;

import javafx.concurrent.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class PoirotAnalysis extends Task<PearlDatabase> {
    private final List<String> genes;
    private final List<String> phenotypes;
    private final PearlDatabase pearlDatabase = new PearlDatabase();
    private final Map<String, List<String>> phenotypeGenes = new HashMap<>();

    public PoirotAnalysis(List<String> genes, List<String> phenotypes) {
        this.genes = genes;
        this.phenotypes = phenotypes;
    }

    @Override
    protected PearlDatabase call() throws Exception {
        upperCase();
        phenotypes.forEach(phenotype -> phenotypeGenes.put(phenotype, Omim.getRelatedGenes(phenotype)));
        return secondTry();
    }

    private void upperCase() {
        for (int i = 0; i < genes.size(); i++) genes.set(i, genes.get(i).toUpperCase());
        for (int i = 0; i < phenotypes.size(); i++) phenotypes.set(i, phenotypes.get(i).toUpperCase());
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
        genes.forEach(gene -> pearlDatabase.getOrCreate(gene, "gene"));
    }

    private void addInitialPhenotypes() {
        phenotypes.forEach(phenotype -> pearlDatabase.getOrCreate(phenotype, "phenotype"));
    }

    private void expandGraph() {
        for (int i = 0; i < 2; i++) {
            final List<String> leafGenes = getLeafGenes();
            updateMessage(String.format("Round %d: %d nodes to expand", i, leafGenes.size()));
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
        // List is split in fragments of 50, cause BioGrid gives MySQL error when too much genes
        // (how much? I don't know)
        connectWithGenes(genes);
        connectWithPhenotypes(genes);
        unLeaf(genes);
    }

    private void connectWithGenes(List<String> genes) {
        for (int j = 0; j < genes.size(); j += 50) {
            updateMessage(j + " nodes processed");
            int to = j + 50;
            if (to > genes.size()) to = genes.size();
            final List<String> interactions = BioGrid.getInteractions(genes.subList(j, to));
            if (interactions != null) expandInteractions(interactions);
        }
    }

    private void expandInteractions(List<String> interactions) {
        interactions.forEach(line -> addInteraction(line.split("\t")));
    }

    private void addInteraction(String[] row) {
        final String from = row[7].toUpperCase();
        final String to = row[8].toUpperCase();
        final String type = row[12];
        final Pearl source = pearlDatabase.getOrCreate(from, "gene");
        if (source == null) return;
        final Pearl target = pearlDatabase.getOrCreate(to, "gene");
        if (target == null) return;
        PearlRelationship relationship = findRelationship(source, target);
        if (relationship != null) {
            final int count = (int) relationship.getProperty("count");
            relationship.setProperty("count", count + 1);
            final List<String> types = (List<String>) relationship.getProperty("types");
            types.add(type);
        } else {
            relationship = source.createRelationshipTo(target);
            relationship.setProperty("count", 1);
            List<String> types = new ArrayList<>();
            types.add(type);
            relationship.setProperty("types", types);
        }
    }

    private void connectWithPhenotypes(List<String> genes) {
        for (String phenotype : phenotypes)
            for (String gene : genes)
                if (phenotypeGenes.get(phenotype).contains(gene))
                    connect(gene, phenotype);
    }

    private void connect(String gene, String phenotype) {
        final Pearl genePearl = pearlDatabase.getPearl(gene, "gene");
        final Pearl phenotypePearl = pearlDatabase.getPearl(phenotype, "phenotype");
        if (phenotypePearl == null || genePearl == null) return;
        PearlRelationship relationship = findRelationship(genePearl, phenotypePearl);
        if (relationship != null) {
            int count = (int) relationship.getProperty("count");
            relationship.setProperty("count", count + 1);
        } else {
            relationship = genePearl.createRelationshipTo(phenotypePearl);
            relationship.setProperty("count", 1);
        }
    }

    private PearlRelationship findRelationship(Pearl genePearl, Pearl phenotypePearl) {
        for (PearlRelationship relationship : genePearl.getOutRelationships())
            if (relationship.getTarget().equals(phenotypePearl)) return relationship;
        return null;
    }

    private void unLeaf(List<String> genes) {
        genes.stream().map(gene->pearlDatabase.getPearl(gene, "gene")).filter(pearl -> pearl!=null).forEach(pearl -> pearl.setLeaf(false));
    }

    private void setWeights() {
        pearlDatabase.getPearls("phenotype").forEach(pearl -> {
            pearl.setWeight(0);
            pearl.getInRelationships().stream().map(PearlRelationship::getSource).forEach(source -> source.setWeight(1));
        });
        final int[] weight = {2};
        for (int i = 0; i < 3; i++) {
            final List<Pearl> wave = pearlDatabase.getPearls("gene").stream().filter(gene -> gene.getWeight() > 0).collect(Collectors.toList());
            wave.forEach(pearl -> {
                pearl.getInRelationships().stream().map(PearlRelationship::getSource).filter(source -> source.getWeight() < 0).forEach(source -> source.setWeight(weight[0]));
                pearl.getOutRelationships().stream().map(PearlRelationship::getTarget).filter(target -> target.getWeight() < 0).forEach(target -> target.setWeight(weight[0]));
            });
            weight[0]++;
        }
    }

    private void cleanDatabase() {
        pearlDatabase.getPearls("gene").stream().filter(pearl -> pearl.getWeight() < 0).forEach(pearlDatabase::remove);
    }

}
