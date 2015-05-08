package coat.vcf;

import java.util.List;
import java.util.TreeMap;

/**
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class InfoStats {
    private List<Double> values;
    private TreeMap<String, Integer> counts;

    public void setCounts(TreeMap<String, Integer> counts) {
        this.counts = counts;
    }

    public TreeMap<String, Integer> getCounts() {
        return counts;
    }

    public void setValues(List<Double> values) {
        this.values = values;
    }

    public List<Double> getValues() {
        return values;
    }
}
