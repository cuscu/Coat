package coat.model.poirot;

import org.junit.Test;

/**
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class PoirotTest {

    @Test
    public void test() {
//        final List<String> genes = ReadList.read(new File("test/genes.list"));
//        final List<String> phenotypes = Arrays.asList("schizophrenia", "brain");
/*
        final List<String> genes = ReadList.read(new File("test/genes2.list"));
        final List<String> phenotypes = Arrays.asList("hypomagnesemia");

        final PearlDatabase database = new PearlDatabase(phenotypes);

        if (genes != null) {
            genes.forEach(geneName -> {
                final Pearl pearl = new Pearl(geneName, "gene");
                database.add(pearl);
            });
            for (int i = 0; i < 2; i++) {
                System.out.println(i + ": " + database.size());
                List<Pearl> unvisitedPearls = database.getLeafPearls();
                AtomicInteger counter = new AtomicInteger(0);
                unvisitedPearls.parallelStream().forEach(pearl -> {
                    if (counter.incrementAndGet() % 500 == 0) {
                        System.out.println(counter);
                    }
                    database.expand(pearl);
                });
            }
        }
        final long pearlsWithPhenotypes = database.getPearls().stream().filter(pearl -> !pearl.getPhenotypes().isEmpty()).count();
        if (pearlsWithPhenotypes == 0) {
            System.out.println("No evidence found");
        } else {
            database.getPearls().stream().filter(pearl -> !pearl.getPhenotypes().isEmpty()).forEach(pearl -> pearl.setDistanceToPhenotype(0));

            List<Pearl> weighted = database.getPearls().stream().filter(pearl -> pearl.getDistanceToPhenotype() >= 0).collect(Collectors.toList());
            int[] weight = new int[]{1};

            int i = 0;
            while (weighted.size() < database.size() && i < 4) {
                for (Pearl pearl : weighted) {
                    pearl.getRelationships().forEach(relatedPearl -> {
                        if (relatedPearl.getDistanceToPhenotype() < 0) relatedPearl.setDistanceToPhenotype(weight[0]);
                    });
                }
                weight[0]++;
                i++;
                weighted = database.getPearls().stream().filter(pearl -> pearl.getDistanceToPhenotype() >= 0).collect(Collectors.toList());
            }
            int total = 0;
            for (String gene : genes) {
                Pearl pearl = database.getPearl(gene);
                if (pearl != null && pearl.getDistanceToPhenotype() >= 0) {
                    total++;
                    List<List<Pearl>> paths = database.findShortestPaths(pearl);
                    for (List<Pearl> p : paths)
                        System.out.println(String.format("(%d)%s->%s", p.size(), p.toString(), p.get(p.size() - 1).getPhenotypes().toString()));
                    System.out.println();

                }
            }
            System.out.println(String.format("%d/%d (%.2f%%)", total, genes.size(), 100.0* total / genes.size()));

        }
        */
    }

}
