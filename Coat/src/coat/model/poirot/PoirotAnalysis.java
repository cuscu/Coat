package coat.model.poirot;

import coat.CoatView;
import javafx.concurrent.Task;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

/**
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class PoirotAnalysis extends Task<PearlDatabase> {
    private final List<String> genes;
    private final List<String> phenotypes;
    private final PearlDatabase pearlDatabase = new PearlDatabase();
    private final Map<String, List<String>> phenotypeGenes = new HashMap<>();
    private final static File localRelationships = new File("biogrid-database.txt");
    private final List<MyRelationship> relationships = new ArrayList<>();
    private String[] headers;


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
        loadRelationships();
        addInitialPhenotypes();
        addInitialGenes();
    }

    private void loadRelationships() {
        System.out.println("Loading biogrid");
        loadBioGrid();
        System.out.println("Loading mentha");
        loadMentha();
    }

    private void loadMentha() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(PoirotAnalysis.class.getResourceAsStream("mentha.txt.gz"))))) {
            System.out.println(reader.lines().count());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadBioGrid() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(PoirotAnalysis.class.getResourceAsStream("biogrid-database.txt")))) {
            headers = reader.readLine().split(",");
            headers = Arrays.copyOfRange(headers, 2, headers.length);
            reader.lines().forEach(line -> {
                try {

                    final String row[] = line.split(",");
                    int[] relations = new int[headers.length];
                    for (int i = 0; i < relations.length; i++) relations[i] = Integer.valueOf(row[i + 2]);
                    relationships.add(new MyRelationship(row[0], row[1], relations));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
        } catch (IOException e) {
            CoatView.printMessage(localRelationships.getAbsolutePath(), "error");
            e.printStackTrace();
        }
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
//        connectWithGenes(genes);
        connectWithLocalGenes(genes);
        connectWithPhenotypes(genes);
        unLeaf(genes);
    }

    private void connectWithLocalGenes(List<String> genes) {
        genes.forEach(geneName -> relationships.stream().
                filter(relationship -> relationship.getSource().equals(geneName) || relationship.getTarget().equals(geneName)).
                forEach(relationship -> {
                    final Pearl source = pearlDatabase.getOrCreate(relationship.getSource(), "gene");
                    final Pearl target = pearlDatabase.getOrCreate(relationship.getTarget(), "gene");
                    updateRelationship(relationship, source, target);
                }));
    }

    private void updateRelationship(MyRelationship myRelationship, Pearl source, Pearl target) {
        PearlRelationship relationship = findRelationship(source, target);
        if (relationship == null) {
            relationship = source.createRelationshipTo(target);
            for (int i = 0; i < headers.length; i++)
                if (myRelationship.getRelations()[i] > 0)
                    relationship.setProperty(headers[i], myRelationship.getRelations()[i]);
            relationship.setProperty("total", Arrays.stream(myRelationship.getRelations()).sum());
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
            int total = (int) relationship.getProperty("total");
            relationship.setProperty("total", total + 1);
        } else {
            relationship = genePearl.createRelationshipTo(phenotypePearl);
            relationship.setProperty("total", 1);
        }
    }

    private PearlRelationship findRelationship(Pearl source, Pearl target) {
        for (PearlRelationship relationship : source.getOutRelationships())
            if (relationship.getTarget().equals(target)) return relationship;
        return null;
    }

    private void unLeaf(List<String> genes) {
        genes.stream().map(gene -> pearlDatabase.getPearl(gene, "gene")).filter(pearl -> pearl != null).forEach(pearl -> pearl.setLeaf(false));
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

    private class MyRelationship {
        private final String source;
        private final String target;
        private final int[] relations;

        public MyRelationship(String source, String target, int[] relations) {

            this.source = source;
            this.target = target;
            this.relations = relations;
        }

        public String getSource() {
            return source;
        }

        public String getTarget() {
            return target;
        }

        public int[] getRelations() {
            return relations;
        }

    }
}
