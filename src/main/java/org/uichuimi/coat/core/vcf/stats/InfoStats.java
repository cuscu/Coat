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

package org.uichuimi.coat.core.vcf.stats;

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
