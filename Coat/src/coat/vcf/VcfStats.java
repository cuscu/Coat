package coat.vcf;

import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class VcfStats extends VBox {

    private final Map<String, InfoStats> stats = new TreeMap<>();
    private final VcfFile vcfFile;

    public VcfStats(VcfFile vcfFile) {
        this.vcfFile = vcfFile;
        vcfFile.getInfos().forEach(stringStringMap -> {
            String id = stringStringMap.get("ID");
            InfoStats infoStats = processInfo(id);
            stats.put(id, infoStats);
        });
    }

    private InfoStats processInfo(String id) {
        TreeMap<String, Integer> counts = new TreeMap<>();
        List<Double> values = new ArrayList<>();
        vcfFile.getVariants().forEach(variant -> {
            final Object o = variant.getInfos().get(id);
            if (o == null) return;
            if (o.getClass() == String.class) {
                String value = (String) o;
                try {
                    double val = Double.valueOf(value);
                    values.add(val);
                } catch (NumberFormatException ex) {
                    processString(counts, value);
                }
            } else {
                try {
                    double value = (double) o;
                    values.add(value);
                } catch (ClassCastException ex) {
                    ex.printStackTrace();
                }
            }
        });
        InfoStats infoStats = new InfoStats();
        infoStats.setValues(values);
        infoStats.setCounts(counts);
        return infoStats;
    }

    private void processString(Map<String, Integer> counts, String value) {
        final Integer integer = counts.getOrDefault(value, 0);
        counts.put(value, integer + 1);
    }

    public Map<String, InfoStats> getStats() {
        return stats;
    }

}
