/*
 * Copyright (c) UICHUIMI 2016
 *
 * This file is part of Coat.
 *
 * Coat is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * Coat is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Foobar.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package coat.core.variant;

import coat.utils.OS;

import java.util.*;

/**
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public class Samples {

    private final Map<String, String>[] formats;

    Samples(int size) {
        formats = new Map[size];
        for (int i = 0; i < formats.length; i++) formats[i] = new LinkedHashMap<>();
    }

    public void setFormat(int index, String key, String value) {
        key = StringStore.getInstance(key);
        value = StringStore.getInstance(value);
        formats[index].put(key, value);
    }

    public String getFormat(int index, String key) {
        return formats[index].get(key);
    }

    @Override
    public String toString() {
        if (formats.length == 0) return "";
        final List<String> keys = new ArrayList<>();
        final List<List<String>> samples = new ArrayList<>();
        for (Map<String, String> format : formats)
            if (format == null) samples.add(Collections.emptyList());
            else {
                final List<String> sample = new ArrayList<>();
                for (Map.Entry<String, String> entry : format.entrySet()) {
                    String key = entry.getKey();
                    String value = entry.getValue();
                    if (!keys.contains(key)) keys.add(key);
                    sample.add(value);
                }
                samples.add(sample);
            }
        final StringBuilder builder = new StringBuilder("\t");
        builder.append(OS.asString(":", keys));
        for (List<String> list : samples) builder.append("\t").append(OS.asString(":", list));
        return builder.toString();
    }
}
