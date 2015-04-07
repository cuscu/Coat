/*
 * Copyright (C) 2015 UICHUIMI
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package coat.vcf;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 *
 * @author Lorente Arencibia, Pascual <pasculorente@gmail.com>
 */
class VcfFile {

    private final List<Variant> variants = new ArrayList<>();
    private final List<Map<String, String>> infos = new ArrayList<>();
    private final List<Map<String, String>> formats = new ArrayList<>();
    private final List<String> unformattedHeaders = new ArrayList<>();
    private final ObservableList<VcfFilter> filters = FXCollections.observableArrayList();
    private final ObservableList<Variant> filteredVariants = FXCollections.observableArrayList();

    private String headerLine;

    public VcfFile(File file) {
        read(file);
        filters.addListener((Observable change) -> applyFilters());
    }

    private void read(File file) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            readLines(reader);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void readLines(final BufferedReader reader) {
        reader.lines().forEach(line -> {
            if (!line.startsWith("#"))
                variants.add(new Variant(line));
            else if (line.startsWith("##INFO=<"))
                infos.add(new MapGenerator().parse(line.substring(8, line.length() - 1)));
            else if (line.startsWith("##FORMAT=<"))
                formats.add(new MapGenerator().parse(line.substring(10, line.length() - 1)));
            else if (line.startsWith("#CHROM"))
                headerLine = line;
            else
                unformattedHeaders.add(line);
        });
    }

    public List<Variant> getVariants() {
        return variants;
    }

    public List<Map<String, String>> getInfos() {
        return infos;
    }

    public List<Map<String, String>> getFormats() {
        return formats;
    }

    public List<String> getUnformattedHeaders() {
        return unformattedHeaders;
    }

    public ObservableList<Variant> getFilteredVariants() {
        return filteredVariants;
    }

    public ObservableList<VcfFilter> getFilters() {
        return filters;
    }

    private void applyFilters() {
        filteredVariants.setAll(variants.stream().filter(this::pass).collect(Collectors.toList()));
    }

    private boolean pass(Variant variant) {
        return filters.stream().allMatch(filter -> filter.pass(variant));
    }

}
