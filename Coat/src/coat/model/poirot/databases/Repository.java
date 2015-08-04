package coat.model.poirot.databases;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class Repository {

    private static Map<DatasetName, Dataset> datasets = new HashMap<>();

    public static Dataset getDataset(DatasetName name) {
        if (!datasets.containsKey(name)) loadDataset(name);
        return datasets.get(name);

    }

    private static void loadDataset(DatasetName name) {
        switch (name) {
            case OMIM:
                datasets.put(DatasetName.OMIM, loadOmimDataset());
                break;
            case HPRD_EXPRESSION:
                datasets.put(DatasetName.HPRD_EXPRESSION, loadHPRDExpressionDataset());
                break;
        }
    }

    private static Dataset loadOmimDataset() {
        final List<String> headers = Arrays.asList("symbol", "name", "status", "disorders");
        final Dataset dataset = new Dataset(headers);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(OmimDatabase.class.getResourceAsStream("omim-normalized.tsv.gz"))))) {
            reader.lines().map(Repository::toOmim).forEach(dataset::addInstance);
        } catch (IOException e) {
            e.printStackTrace();
        }
        dataset.createIndex(0);
        return dataset;
    }

    private static Object[] toOmim(String line) {
        final String[] fields = line.split("\t");
        final String standardSymbol = HGNCDatabase.getStandardSymbol(fields[0]);
        if (standardSymbol != null) fields[0] = standardSymbol;
        return fields;
    }

    private static Dataset loadHPRDExpressionDataset() {
        final List<String> headers = Arrays.asList("hprd_id", "refseq_id", "symbol", "expression", "status", "reference_id");
        final Dataset dataset = new Dataset(headers);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(Repository.class.getResourceAsStream("hprd-phenotypes.tsv.gz"))))) {
            reader.lines().map(Repository::toHPRDExpression).forEach(dataset::addInstance);
        } catch (IOException e) {
            e.printStackTrace();
        }
        dataset.createIndex(0);
        return dataset;
    }

    private static Object[] toHPRDExpression(String line) {
        final String[] fields = line.split("\t");
        final String standardSymbol = HGNCDatabase.getStandardSymbol(fields[0]);
        if (standardSymbol != null) fields[0] = standardSymbol;
        return fields;
    }

    public enum DatasetName {
        OMIM, HPRD_EXPRESSION
    }

}
