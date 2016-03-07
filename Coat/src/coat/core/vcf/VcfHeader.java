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

package coat.core.vcf;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Stores headers of Variant Call Format Version 4.2.
 *
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class VcfHeader {

    private static final Pattern META_LINE = Pattern.compile("##([^=]+)=(.+)");
    private static final Pattern META_LINE_CONTENT = Pattern.compile("<(.*)>");
    private static final Pattern FIELDS_LINE = Pattern.compile("#CHROM(.*)");
    private final Map<String, List<Map<String, String>>> complexHeaders = new TreeMap<>();
    private final Map<String, String> singleHeaders = new TreeMap<>();
    private final List<String> samples = new ArrayList<>();

    /**
     * Adds a header line
     *
     * @param line
     */
    public void addHeader(String line) {
        final Matcher metaLine = META_LINE.matcher(line);
        if (metaLine.matches()) addMetaLine(metaLine);
        else addFormatLine(line);
    }

    private void addMetaLine(Matcher metaLine) {
        final String key = metaLine.group(1);
        final String value = metaLine.group(2);
        final Matcher contentMatcher = META_LINE_CONTENT.matcher(value);
        if (contentMatcher.matches()) addComplexHeader(key, contentMatcher.group(1));
        else addSingleHeader(key, value);
    }

    private void addComplexHeader(String key, String value) {
        complexHeaders.putIfAbsent(key, new ArrayList<>());
        final List<Map<String, String>> headers = complexHeaders.get(key);
        final Map<String, String> map = MapGenerator.parse(value);
        if (!headerContainsId(key, headers)) {
            headers.add(map);
        }
    }

    private boolean headerContainsId(String key, List<Map<String, String>> headers) {
        for (Map<String, String> header : headers) if (header.get("ID").equals(key)) return true;
        return false;
    }

    private void addSingleHeader(String key, String value) {
        singleHeaders.putIfAbsent(key, value);
    }

    private void addFormatLine(String line) {
        final Matcher matcher = FIELDS_LINE.matcher(line);
        if (matcher.matches()) {
            final String[] split = line.split("\t");
            int numberOfSamples = split.length - 9;
            if (numberOfSamples > 0) for (int i = 0; i < numberOfSamples; i++) samples.add(split[i + 9]);
//            if (numberOfSamples > 0) samples.addAll(Arrays.asList(split).subList(9, numberOfSamples));
        }
    }

    public Map<String, List<Map<String, String>>> getComplexHeaders() {
        return complexHeaders;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("##fileformat=").append(singleHeaders.get("fileformat")).append(System.lineSeparator());
        appendSingleHeaders(builder);
        appendComplexHeaders(builder);
        appendFormatLine(builder);
        return builder.toString();
    }

    private void appendSingleHeaders(StringBuilder builder) {
        singleHeaders.entrySet().stream()
                .filter(entry -> !entry.getKey().equals("fileformat"))
                .forEach(entry -> {
                    builder.append("##").append(entry.getKey()).append("=");
                    if (entry.getValue().contains(" ")) builder.append("\"").append(entry.getValue()).append("\"");
                    else builder.append(entry.getValue());
                    builder.append(System.lineSeparator());
                });
    }

    private void appendComplexHeaders(StringBuilder builder) {
        complexHeaders.entrySet().stream()
                .forEach(entry -> {
                    final String type = entry.getKey();
                    entry.getValue().forEach(map -> {
                        builder.append("##").append(type).append("=<ID=").append(map.get("ID"));
                        for (Map.Entry<String, String> pair : map.entrySet()) {
                            if (pair.getKey().equals("ID")) continue;
                            builder.append(",").append(pair.getKey()).append("=");
                            if (pair.getValue().contains(" "))
                                builder.append("\"").append(pair.getValue()).append("\"");
                            else builder.append(pair.getValue());
                        }
                        builder.append(">").append(System.lineSeparator());
                    });
                });
    }

    private void appendFormatLine(StringBuilder builder) {
        builder.append("#CHROM\tPOS\tID\tREF\tALT\tQUAL\tFILTER\tINFO");
        if (!samples.isEmpty()) {
            builder.append("\tFORMAT");
            samples.forEach(f -> builder.append("\t").append(f));
        }
        builder.append(System.lineSeparator());
    }

    public List<String> getSamples() {
        return samples;
    }

    public List<String> getIdList(String type) {
        final List<Map<String, String>> list = complexHeaders.get(type);
        if (list == null) return Collections.emptyList();
        return list.stream().map(map -> map.get("ID")).collect(Collectors.toList());
    }

    public int getSampleIndex(String sample) {
        return samples.indexOf(sample);
    }

    public Map<String, String> getSimpleHeaders() {
        return singleHeaders;
    }


}
