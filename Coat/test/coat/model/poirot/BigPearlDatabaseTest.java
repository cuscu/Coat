package coat.model.poirot;

import org.junit.Test;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class BigPearlDatabaseTest {

    private final static PearlDatabase database = new PearlDatabase();
    private final static File genes = new File("omim", "allGenes");
    private final static File databaseFile = new File("omim", "biogrid-database.txt");
    private final static AtomicInteger processed = new AtomicInteger();

    private final List<String> buffer = new ArrayList<>();
    private final static int MAX_GENES_PER_CONNECTION = 10;

    private final List<String> blackList = Arrays.asList("UBC");


    @Test
    public void generate() {
        try (BufferedReader reader = new BufferedReader(new FileReader(genes))) {
            System.out.println("Reading genes...");
            reader.lines().forEach((gene) -> {
                buffer.add(gene);
                if (buffer.size() == MAX_GENES_PER_CONNECTION) {
                    processGenes(buffer);
                    buffer.clear();
                }
            });
            processGenes(buffer);
            buffer.clear();
            System.out.println("All genes read...");
            saveToFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void processGenes(List<String> genes) {
        if (processed.addAndGet(genes.size()) % 1000 == 0) System.out.println(processed.get() + " genes processed");
        final List<String> interactions = BioGrid.getInteractions(genes);
        interactions.forEach(this::addInteraction);
    }

    private void addInteraction(String interaction) {
        final String[] row = interaction.split("\t");
        final String from = row[7].toUpperCase();
        final String to = row[8].toUpperCase();
        for (String regex : blackList) if (from.matches(regex) || to.matches(regex)) return;
        final String type = row[12];
        final Pearl source = database.getOrCreate(from, "gene");
        final Pearl target = database.getOrCreate(to, "gene");
        addRelationship(type, source, target);
    }

    private void addRelationship(String type, Pearl source, Pearl target) {
        PearlRelationship relationship = findRelationship(source, target);
        if (relationship != null) updateRelationship(type, relationship);
        else createRelationship(type, source, target);
    }

    private void createRelationship(String type, Pearl source, Pearl target) {
        PearlRelationship relationship = source.createRelationshipTo(target);
        relationship.setProperty("count", 1);
        relationship.setProperty("types", new ArrayList<>(Arrays.asList(type)));
    }

    private void updateRelationship(String type, PearlRelationship relationship) {
        final int count = (int) relationship.getProperty("count");
        relationship.setProperty("count", count + 1);
        final List<String> types = (List<String>) relationship.getProperty("types");
        types.add(type);
    }

    private PearlRelationship findRelationship(Pearl genePearl, Pearl phenotypePearl) {
        for (PearlRelationship relationship : genePearl.getOutRelationships())
            if (relationship.getTarget().equals(phenotypePearl)) return relationship;
        return null;
    }

    private void saveToFile() {
        processed.set(0);
        final List<Pearl> pearls = database.getPearls("gene");
        System.out.printf("Saving %d genes\n", pearls.size());
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(databaseFile))) {
            for (Pearl pearl : pearls) {
                if (processed.incrementAndGet() % 1000 == 0) System.out.println(processed.get() + " genes saved");
                for (PearlRelationship relationship : pearl.getOutRelationships())
                    writeRelationship(writer, relationship);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeRelationship(BufferedWriter writer, PearlRelationship relationship) throws IOException {
        final List<String> types = (List<String>) relationship.getProperty("types");
        final long physical = count(types, "physical");
        final long genetic = count(types, "genetic");
        writer.write(String.format("%s,%s,%d,%d", relationship.getSource().getName(), relationship.getTarget().getName(), physical, genetic));
        writer.newLine();
    }

    private long count(List<String> types, String value) {
        return types.stream().filter(s -> s.equals(value)).count();
    }


}
