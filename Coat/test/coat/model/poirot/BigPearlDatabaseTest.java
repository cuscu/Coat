package coat.model.poirot;

import org.junit.Test;

import java.io.*;
import java.util.*;
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
    private final static int MAX_GENES_PER_CONNECTION = 50;

    private final List<String> blackList = Arrays.asList("UBC");

    private final static File bioGridFile = new File("/home/unidad03/Documentos/BIOGRID-ALL-3.3.124.mitab.txt");
    private final static Set<String> types = new TreeSet<>();

    @Test
    public void generateFromFile() {
        try (BufferedReader reader = new BufferedReader(new FileReader(bioGridFile))) {
            reader.readLine();
            reader.lines().forEach(this::process);
        } catch (IOException e) {
            e.printStackTrace();
        }
        saveToFile();
    }

    private void process(String line) {
        final String[] row = line.split("\t");
        final String from;
        final String to;
        final String type;
        // 0 and 1 (not used)
        // Unique identifier for interactors, represented as databaseName:ac, where databaseName is the name of
        // the corresponding database as defined in the PSI-MI controlled vocabulary, and ac is the unique primary
        // identifier of the molecule in the database. Identifiers from multiple databases can be separated by “|”.
        // It is recommended that proteins be identified by stable identifiers such as their UniProtKB or RefSeq
        // accession number.

        // databaseName:ac|databaseName:ac
        // entrez gene/locuslink:6416

        // 2 and 3
        // Alternative identifier for interactors, for example the official gene symbol as defined by a recognised
        // nomenclature committee. Representation as databaseName:identifier. Multiple identifiers separated by “|”.
        // biogrid:198544|entrez gene/locuslink:Ccna1
        // biogrid:62121|entrez gene/locuslink:CG13159|entrez gene/locuslink:Dmel_CG13159
        // biogrid:35532|entrez gene/locuslink:MCK1|entrez gene/locuslink:YNL307C
        from = extractGeneName(row[2]);
        to = extractGeneName(row[3]);

        // 4 and 5 (not used)
        // Aliases,separated by “|”. Representation as databaseName:identifier. Multiple identifiers separated by “|”.

        // entrez gene/locuslink:ABP-280(gene name synonym)|
        // entrez gene/locuslink:ABP280A(gene name synonym)|
        // entrez gene/locuslink:ABPA(gene name synonym)|
        // entrez gene/locuslink:ABPL(gene name synonym)|
        // entrez gene/locuslink:FLN2(gene name synonym)|
        // entrez gene/locuslink:MFM5(gene name synonym)|
        // entrez gene/locuslink:MPD4(gene name synonym)

        // entrez gene/locuslink:CG12029(gene name synonym)|
        // entrez gene/locuslink:DmelCG12029(gene name synonym)|
        // entrez gene/locuslink:"l(3)63Ed"(gene name synonym)|
        // entrez gene/locuslink:"l(3)SH5"(gene name synonym)

        // 6
        // Interaction detection methods, taken from the corresponding PSI-MI controlled vocabulary, and represented
        // as darabaseName:identifier(methodName), separated by “|”.

        // psi-mi:"MI:0018"(two hybrid)
        // psi-mi:"MI:0004"(affinity chromatography technology)
        // psi-mi:"MI:0254"(genetic interference)
        // psi-mi:"MI:0114"(x-ray crystallography)
        // psi-mi:"MI:0004"(affinity chromatography technology)


        // 7 (not used)
        // First author surname(s) of the publication(s) in which this interaction has been shown, optionally
        // followed by additional indicators.

        // Stephenson A (2005)
        // Rodriguez-Navarro S (2004)
        // Krogan NJ (2003)

        // 8 (not used)
        // Identifier of the publication in which this interaction has been shown. Database name taken from the
        // PSI-MI controlled vocabulary, represented as databaseName:identifier. Multiple identifiers separated by “|”.

        // pubmed:11489916


        // 9 and 10 (not used)
        // NCBI Taxonomy identifier. Database name for NCBI taxid taken from the PSI-MI controlled vocabulary,
        // represented as databaseName:identifier. Multiple identifiers separated by “|”. Note: In this column, the
        // databaseName:identifier(speciesName) notation is only there for consistency. Currently no taxonomy
        // identifiers other than NCBI taxid are anticipated, apart from the use of -1 to indicate “in vitro” and -2 to
        // indicate “chemical synthesis”.

        // taxid:559292

        // 11
        // Interaction types, taken from the corresponding PSI-MI controlled vocabulary, and represented as
        // dataBaseName:identifier(interactionType), separated by “|”.

        // psi-mi:"MI:0407"(direct interaction)
        // psi-mi:"MI:0915"(physical association)
        // psi-mi:"MI:0403"(colocalization)
        // psi-mi:"MI:0796"(suppressive genetic interaction defined by inequality)
        type = extractType(row[11]);

        // 12
        // Source databases and identifiers, taken from the corresponding PSI-MI controlled vocabulary, and represented
        // as databaseName:identifier(sourceName). Multiple source databases can be separated by “|”.

        // psi-mi:"MI:0463"(biogrid)

        // 13
        // Interaction identifier(s) in the corresponding source database, represented by databaseName:identifier

        // biogrid:154390


        // 14 (not used)
        // Confidence score. Denoted as scoreType:value. There are many different types of confidence score, but so
        // far no controlled vocabulary. Thus the only current recommendation is to use score types consistently
        // within one source. Multiple scores separated by “|”.

        // score:-3.5947

        if (from != null && to != null) {
            final String FROM = from.toUpperCase().replace(",", ".");
            final String TO = to.toUpperCase().replace(",", ".");
            for (String regex : blackList) if (FROM.matches(regex) || TO.matches(regex)) return;
            final Pearl source = database.getOrCreate(FROM, "gene");
            final Pearl target = database.getOrCreate(TO, "gene");
            addRelationship(type, source, target);
            types.add(type);
        }
    }

    private String extractGeneName(String cell) {
        // databaseName:identifier. Multiple identifiers separated by “|”.
        // biogrid:198544|entrez gene/locuslink:Ccna1
        // biogrid:62121|entrez gene/locuslink:CG13159|entrez gene/locuslink:Dmel_CG13159
        // biogrid:35532|entrez gene/locuslink:MCK1|entrez gene/locuslink:YNL307C
        final String[] entries = cell.split("\\|");
        for (String entry : entries) {
            final String[] pair = entry.split(":");
            if (pair[0].equals("entrez gene/locuslink")) return pair[1];
        }
        return null;
    }

    private String extractType(String cell) {
        // dataBaseName:identifier(interactionType), separated by “|”.

        // psi-mi:"MI:0407"(direct interaction)
        // psi-mi:"MI:0915"(physical association)
        // psi-mi:"MI:0403"(colocalization)
        // psi-mi:"MI:0796"(suppressive genetic interaction defined by inequality)
        final String[] entries = cell.split("\\|");
        final String entry = entries[0];
        final int openBracket = entry.indexOf("(");
        final int closeBracket = entry.indexOf(")");
        return entry.substring(openBracket + 1, closeBracket);

    }

    public void generateFromWeb() {
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
        interactions.forEach((interaction) -> BigPearlDatabaseTest.this.addInteraction(interaction, genes));
    }

    private void addInteraction(String interaction, List<String> genes) {
        final String[] row = interaction.split("\t");
        final String from = row[7].toUpperCase().replace(",", ".");
        // Only source relationships
        if (genes.contains(from)) {
            final String to = row[8].toUpperCase().replace(",", ".");
            for (String regex : blackList) if (from.matches(regex) || to.matches(regex)) return;
            final String type = row[12];
            final Pearl source = database.getOrCreate(from, "gene");
            final Pearl target = database.getOrCreate(to, "gene");
            addRelationship(type, source, target);
        }
    }

    private void addRelationship(String type, Pearl source, Pearl target) {
        final PearlRelationship relationship = findRelationship(source, target);
        if (relationship == null) createRelationship(type, source, target);
        else updateRelationship(type, relationship);
    }

    private void createRelationship(String type, Pearl source, Pearl target) {
        final PearlRelationship relationship = source.createRelationshipTo(target);
        relationship.setProperty(type, 1);
    }

    private void updateRelationship(String type, PearlRelationship relationship) {
        final int count = (int) relationship.getOrDefaultProperty(type, 0);
        relationship.setProperty(type, count + 1);
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
            writer.write("source,target");
            for (String type : types) writer.write("," + type);
            writer.newLine();
            for (Pearl pearl : pearls) {
                if (processed.incrementAndGet() % 10000 == 0) System.out.println(processed.get() + " genes saved");
                for (PearlRelationship relationship : pearl.getOutRelationships())
                    writeRelationship(writer, relationship);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeRelationship(BufferedWriter writer, PearlRelationship relationship) throws IOException {
        relationship.getProperties();
        writer.write(String.format("%s,%s", relationship.getSource().getName(), relationship.getTarget().getName()));
        for (String type : types) writer.write("," + relationship.getOrDefaultProperty(type, 0));
        writer.newLine();
    }

}
