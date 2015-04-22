package coat.vcf;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class VcfStats {

    private List<InfoStats> infoStats = new ArrayList<>();

    public VcfStats(VcfFile vcfFile) {
        infoStats.add(new InfoStats());
    }

    public List<InfoStats> getInfoStats() {
        return infoStats;
    }
}
