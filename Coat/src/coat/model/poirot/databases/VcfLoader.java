package coat.model.poirot.databases;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class VcfLoader {

    private static List<String> headers;
    private static Dataset dataset;


    public static Dataset createDataset(File file) {
        headers = new ArrayList<>();
        headers.addAll(Arrays.asList("chrom", "pos", "id", "ref", "alt", "qual", "filter"));
        headers.addAll(readInfoHeaders(file));
        dataset = new Dataset();
        dataset.setColumnNames(headers);
        readVariants(file);
        return dataset;
    }

    private static void readVariants(File file) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            reader.lines().filter(line -> !line.startsWith("#")).map((VcfLoader::getFields)).forEach(dataset::addInstance);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static List<String> readInfoHeaders(File file) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            return reader.lines().filter(line -> line.startsWith("##INFO=<")).map(VcfLoader::extractName).collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    private static String extractName(String line) {
        // ##INFO=<ID=EUR_F,Number=1,Type=String,Description="Minor allele and frequency of existing variation in 1000 Genomes Phase 1 combined European population">
        return line.substring(8, line.indexOf(",")).replace("ID=", "");
    }

    private static Object[] getFields(String line) {
        final String[] variant = line.split("\t");
        final Object[] fields = new Object[headers.size()];
        // Copy chrom, pos, id, ref, alt, qual, filter
        System.arraycopy(variant, 0, fields, 0, 7);
        if (variant.length > 7) {
            final String[] infos = variant[7].split(";");
            for (String info : infos) {
                final String[] pair = info.split("=");
                final int position = dataset.getPositionOf(pair[0]);
                if (position > 0) fields[position] = pair.length > 1 ? pair[1] : true;
            }
        }
        return fields;
    }

}
