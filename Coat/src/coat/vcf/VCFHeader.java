/*
 * Copyright (C) 2014 UICHUIMI
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
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class is used for advanced header treatment. Currently it only process the info lines,
 * storing the id, description... as a map.
 *
 * @author Pascual Lorente Arencibia (pasculorente@gmail.com)
 */
public class VCFHeader {

    /**
     * The list of INFO lines.
     */
    private final List<Map<String, String>> infos = new ArrayList();
    /**
     * The list of FORMAT lines.
     */
    private final List<Map<String, String>> formats = new ArrayList();
    /**
     * The list of header lines.
     */
    private Set<String> unprocessedHeaders = new LinkedHashSet();

    private Set<String> unformattedInfos = new LinkedHashSet();
    private Set<String> unformattedFormats = new LinkedHashSet();

    private static String masterHeader = "#CHROM\tPOS\tID\tREF\tALT\tQUAL\tFILTER\tINFO";

    /**
     * Creates a new VCFHeader using the header lines of the vcfFile.
     *
     * @param vcfFile the source file to parse
     */
    public VCFHeader(File vcfFile) {
        try (BufferedReader reader = new BufferedReader(new FileReader(vcfFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.startsWith("#"))
                    break;
                if (line.startsWith("##INFO=<")) {
                    infos.add(parseInfo(line));
                    unformattedInfos.add(line);
                } else if (line.startsWith("##FORMAT=<")) {
                    formats.add(parseFormat(line));
                    unformattedFormats.add(line);
                } else if (line.startsWith("#CHROM"))
                    masterHeader = line;
                else
                    unprocessedHeaders.add(line);
            }
        } catch (IOException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Reads a INFO line and returns a Map.
     *
     * @param line the INFO line in the VCF
     * @return a map with the INFO keys=values
     */
    private Map<String, String> parseInfo(String line) {
        return new MapGenerator().parse(line.substring(8, line.length() - 1));
    }

    /**
     * Reads a FORMAT line and returns a Map.
     *
     * @param line the format line in the VCF
     * @return a map with the FORMAT keys=values
     */
    private Map<String, String> parseFormat(String line) {
        return new MapGenerator().parse(line.substring(10, line.length() - 1));
    }

    /**
     * Gets the list of infos. Each info is a map (key=value), as the VCF ##INFO= line. Use
     * {@code getInfos().get("ID")} to get th ID.
     *
     * @return the list of infos
     */
    public List<Map<String, String>> getInfos() {
        return infos;
    }

    /**
     * Gets the list of infos. Each info is a map (key=value), as the VCF ##INFO= line. Use
     * {@code getInfos().get("ID")} to get th ID.
     *
     * @return the list of formats
     */
    public List<Map<String, String>> getFormats() {
        return formats;
    }

    /**
     * Gets the content of the VCF header as a list of Strings, each String correspond to a header
     * line. This list is unmodificable.
     *
     * @return a copy of the list with the headers lines
     */
    public Set<String> getHeaders() {
        Set<String> copy = new LinkedHashSet(unprocessedHeaders);
        copy.addAll(unformattedFormats);
        copy.addAll(unformattedInfos);
        copy.add(masterHeader);
        return copy;
    }

    /**
     * Adds the line to the headers if it is a valid line.
     *
     * @param line
     */
    public void add(String line) {
        if (line.startsWith("##INFO=<"))
            infos.add(parseInfo(line));
        else if (line.startsWith("##FORMAT=<"))
            formats.add(parseFormat(line));
        else if (!line.startsWith("##"))
            return;
        unprocessedHeaders.add(line);
    }

}
