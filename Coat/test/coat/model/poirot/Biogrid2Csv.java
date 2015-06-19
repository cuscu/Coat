package coat.model.poirot;

import org.junit.Test;

import java.io.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class Biogrid2Csv {

    @Test
    public void test() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(getClass().getResourceAsStream("biogrid.txt.gz"))));
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream("biogrid.csv.gz"))))) {
            writer.write("id,source,target,database,type,method,score");
            writer.newLine();
            final String header = reader.readLine();
            reader.lines().forEach(line -> {
                String local = toLocal(line);
                if (local != null)
                    try {
                        writer.write(local);
                        writer.newLine();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
            });

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    private String toLocal(String line) {
        final String[] row = line.split("\t");
        // Alt. ID(s) interactor A/B
        final String from = extractGeneName(row[2]);
        final String to = extractGeneName(row[3]);
        final String method = extractMethod(row[6]);
        final String type = extractType(row[11]);
        final String database = extractDatabase(row[12]);
        final String id = extractId(row[13]);
        final String score = extractScore(row[14]);
        if (from != null && to != null) {
            final String source = from.toUpperCase().replace(",", ".");
            final String target = to.toUpperCase().replace(",", ".");
            return String.format("%s,%s,%s,%s,%s,%s,%s", id, source, target, database, type, method, score);
        }
        return null;
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

    private String extractMethod(String cell) {
        // psi-mi:"MI:0004"(affinity chromatography technology)
        return extractType(cell);
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

    private String extractDatabase(String cell) {
        // psi-mi:"MI:0463"(biogrid)
        final int openBracket = cell.indexOf("(");
        final int closeBracket = cell.indexOf(")");
        return cell.substring(openBracket + 1, closeBracket);
    }

    private String extractId(String cell) {
        // BIOGRID:643614
        return cell.replace("BIOGRID:", "");
    }

    private String extractScore(String cell) {
        return cell.equals("-") ? "-" : cell.split(":")[1];
    }
}
