/*
 * Copyright (c) UICHUIMI 2017
 *
 * This file is part of Coat.
 *
 * Coat is free software:
 * you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * Coat is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with Coat.
 *
 * If not, see <http://www.gnu.org/licenses/>.
 *
 */

package coat.core.vcf;

import coat.core.vcf.stats.InfoStats;
import org.uichuimi.vcf.variant.Variant;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class VcfStats {

    private final Map<String, InfoStats> stats = new TreeMap<>();
    private final List<Variant> variants;

    public VcfStats(List<Variant> variants) {
        this.variants = variants;
        variants.get(0).getHeader().getInfoLines().values().stream()
		        .map(line -> line.getValue("ID")).forEach(info -> stats.put(info, getStats(info)));
    }

    private InfoStats getStats(String id) {
        final TreeMap<String, Integer> counts = new TreeMap<>();
        final List<Double> values = new ArrayList<>();
        variants.forEach(variant -> {
            final Object o = variant.getInfo().get(id);
            if (o == null) return;
            if (o.getClass() == String.class) {
                String[] value = ((String) o).split(",");
                try {
                    Double val = Double.valueOf(value[0]);
                    if (!Double.isNaN(val)) values.add(val);
                } catch (NumberFormatException ex) {
                    for (String v : value) processString(counts, v);
                }
            } else {
                try {
                    double value = (double) o;
                    values.add(value);
                } catch (ClassCastException ex) {
//                    ex.printStackTrace();
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
