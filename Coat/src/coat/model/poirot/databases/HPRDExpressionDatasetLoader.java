package coat.model.poirot.databases;

import javafx.concurrent.Task;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

/**
 * Loads the HPRD expression database, which relates genes to where they are expressed in body. (0) hprd_id,
 * (1) refseq_id, (2) symbol, (3) expression, (4) status, (5) reference_id.
 *
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class HPRDExpressionDatasetLoader extends Task<Dataset> {

    private Dataset dataset = new Dataset();

    @Override
    protected Dataset call() throws Exception {
        loadEntries();
        setColumnNames();
        return dataset;
    }

    private void setColumnNames() {
        dataset.setColumnNames(Arrays.asList("hprd_id", "refseq_id", "symbol", "expression", "status", "reference_id"));
    }

    private void loadEntries() {
        final List<Instance> collect = readFileInstances();
        if (collect != null) {
//            CoatView.printMessage("Omim database successfully loaded", "info");
            dataset.getInstances().addAll(collect);
            dataset.createIndex(2);
        }
    }

    private List<Instance> readFileInstances() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(OmimDatasetLoader.class.getResourceAsStream("hprd-phenotypes.tsv.gz"))))) {
            return reader.lines()
                    .map(line -> line.split("\t"))
                    .map(line -> new Instance(dataset, line))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Integer getIntegerOrNull(String value) {
        try {
            return Integer.valueOf(value);
        } catch (Exception ignored) {
            return null;
        }
    }
}
