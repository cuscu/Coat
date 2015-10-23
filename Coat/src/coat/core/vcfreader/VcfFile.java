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
package coat.core.vcfreader;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Stores in memory a Vcf file data.
 *
 * @author Lorente Arencibia, Pascual <pasculorente@gmail.com>
 */
public class VcfFile {

    private final ObservableList<Variant> variants = FXCollections.observableArrayList();
    private final ObservableList<Map<String, String>> infos = FXCollections.observableArrayList();
    private final List<Map<String, String>> formats = new ArrayList<>();
    private final List<String> unformattedHeaders = new ArrayList<>();
    private File file;

    public VcfFile(File file) {
        this.file = file;
        readFile(file);
    }

    private void readFile(File file) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            readLines(reader);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void readLines(final BufferedReader reader) {
        reader.lines().forEach(line -> {
            if (!line.startsWith("#")) variants.add(new Variant(line));
            else processMetaLine(line);
        });
    }

    private void processMetaLine(String line) {
        unformattedHeaders.add(line);
        if (line.startsWith("##INFO=<"))
            infos.add(MapGenerator.parse(line.substring(8, line.length() - 1)));
        else if (line.startsWith("##FORMAT=<"))
            formats.add(MapGenerator.parse(line.substring(10, line.length() - 1)));
    }

    public ObservableList<Variant> getVariants() {
        return variants;
    }

    public ObservableList<Map<String, String>> getInfos() {
        return infos;
    }

    public List<String> getUnformattedHeaders() {
        return unformattedHeaders;
    }

    public void addInfoLines(String... lines) {
        for (String line : lines) {
            infos.add(MapGenerator.parse(line.substring(8, line.length() - 1)));
            addToUnformattedHeaders(line);
        }
    }

    private void addToUnformattedHeaders(String line) {
        if (unformattedHeaders.contains(line)) return;
        int posOfLastInfoLine = -1;
        for (int i = 0; i < unformattedHeaders.size(); i++)
            if (unformattedHeaders.get(i).startsWith("##INFO=")) {
                posOfLastInfoLine = i;
                if (unformattedHeaders.get(i).compareTo(line) > 0) {
                    unformattedHeaders.add(i, line);
                    return;
                }
            }
        if (posOfLastInfoLine != -1) unformattedHeaders.add(posOfLastInfoLine + 1, line);
        else unformattedHeaders.add(1, line);
    }


    public File getFile() {
        return file;
    }
}
