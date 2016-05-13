/******************************************************************************
 * Copyright (C) 2015 UICHUIMI                                                *
 *                                                                            *
 * This program is free software: you can redistribute it and/or modify it    *
 * under the terms of the GNU General Public License as published by the      *
 * Free Software Foundation, either version 3 of the License, or (at your     *
 * option) any later version.                                                 *
 *                                                                            *
 * This program is distributed in the hope that it will be useful, but        *
 * WITHOUT ANY WARRANTY; without even the implied warranty of                 *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                       *
 * See the GNU General Public License for more details.                       *
 *                                                                            *
 * You should have received a copy of the GNU General Public License          *
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.      *
 ******************************************************************************/

package coat.core.vcf;

import coat.core.vcf.stats.InfoStats;
import javafx.scene.layout.VBox;
import vcf.VcfFile;

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
        vcfFile.getHeader().getComplexHeaders().get("INFO").stream().map(map -> map.get("ID")).forEach((info) -> {
            InfoStats infoStats = processInfo(info);
            stats.put(info, infoStats);
        });
    }

    private InfoStats processInfo(String id) {
        TreeMap<String, Integer> counts = new TreeMap<>();
        List<Double> values = new ArrayList<>();
        vcfFile.getVariants().forEach(variant -> {
            final Object o = variant.getInfo().getInfo(id);
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
