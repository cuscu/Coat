package coat.model.poirot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class BioGrid {

    private final static String accessPoint = "http://webservice.thebiogrid.org/interactions/";
    private final static String API_KEY = "c7f57b594c042c03aa00363678e94621";
    private final static String accessKey = "?accesskey=" + API_KEY;
    private final static String includeHeader = "&includeHeader=true";
    private final static String searchNames = "&searchNames=true";

    // http://webservice.thebiogrid.org/interactions/?accesskey=c7f57b594c042c03aa00363678e94621&searchNames=true

    public static List<String> getRelatedGenes(String gene) {
        try {
            URL url = new URL(accessPoint + accessKey + searchNames + "&geneList=" + gene);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                Set<String> related = new TreeSet<>();
                //System.out.println(reader.readLine());
                reader.lines().forEach(line -> {
                    final String[] strings = line.split("\t");
                    related.add(strings[7]);
                    related.add(strings[8]);
                    System.out.println(line);
                });
                related.remove(gene);
                return new ArrayList<>(related);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println(e.getMessage());
//            e.printStackTrace();
        }
        return null;
    }

    public static List<String> getInteractions(List<String> genes) {
        try {
            String geneList = toList(genes);
            URL url = new URL(accessPoint + accessKey + searchNames + "&geneList=" + geneList);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                return reader.lines().collect(Collectors.toList());
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
        return new ArrayList<>();
    }

    private static String toList(List<String> genes) {
        String pepa = genes.get(0);
        for (int i = 1; i < genes.size(); i++) pepa += "|" + genes.get(i);
        return pepa;
    }
}
