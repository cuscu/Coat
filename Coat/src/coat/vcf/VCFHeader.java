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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
    private List<String> unprocessedHeaders = new ArrayList();

    /**
     * Creates a new VCFHeader using the header lines of the vcfFile.
     *
     * @param vcfFile the source file to parse
     */
    public VCFHeader(File vcfFile) {
        try (BufferedReader reader = new BufferedReader(new FileReader(vcfFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.startsWith("#")) {
                    break;
                }
                if (line.startsWith("##INFO=<")) {
                    infos.add(parseInfo(line));
                } else if (line.startsWith("##FORMAT=<")) {
                    formats.add(parseFormat(line));
                }
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
        return generateMap(line.substring(8, line.length() - 1));
    }

    /**
     * Reads a FORMAT line and returns a Map.
     *
     * @param line the format line in the VCF
     * @return a map with the FORMAT keys=values
     */
    private Map<String, String> parseFormat(String line) {
        return generateMap(line.substring(10, line.length() - 1));
    }

    /**
     * Returns a LinkedHashMap with the content of the line parsed. So "ID=AC,Number=A,Type=Integer"
     * becomes a map. This method is convenient to parse almost any VCF header lines.
     *
     * @param line line to map, without ##INFO neither ##FORMAT neither &lt neither &gt
     * @return a map with the content of the line
     */
    private Map<String, String> generateMap(String line) {
        Map<String, String> map = new LinkedHashMap();
        int cursor = 0;
        String key = null;
        String value;
        // Am I reading a key or a value?
        boolean isKey = true;
        while (cursor < line.length()) {
            switch (line.charAt(cursor)) {
                case '"':
                    // If isKey is false, something went wrong
                    // Text in quotes
                    // token is the text between quotes
                    // place cursor at next position after end quote
                    int endQuotePosition = line.indexOf("\"", cursor + 1);
                    value = line.substring(cursor + 1, endQuotePosition);
                    cursor = endQuotePosition + 1;
                    map.put(key, value);
                    break;
                case '=':
                    // Equals symbol: cursor at next position and expected a value
                    cursor++;
                    isKey = false;
                    break;
                case ',':
                    // Comma symbol, cursor at next position and expected a key
                    cursor++;
                    isKey = true;
                    break;
                default:
                    // Text not in quotes
                    // token is the text between cursor and next "=" or ","
                    // cursor at "=" or ","
                    int end = cursor;
                    while (line.charAt(end) != '=' && line.charAt(end) != ',') {
                        end++;
                    }
                    if (isKey) {
                        key = line.substring(cursor, end);
                    } else {
                        value = line.substring(cursor, end);
                        map.put(key, value);
                    }
                    cursor = end;
            }
        }
        return map;
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
     * Adds a INFO header. This method will parse the complete line: ##INFO&ltID=STH,Type=...&gt
     *
     * @param line the INFO line in the VCF
     */
    public void addInfo(String line) {
        infos.add(parseInfo(line));
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
     * Adds a FORMAT header. This method will parse the complete line: ##FORMAT&ltID=STH,Type=...&gt
     *
     * @param line the FORMAT line from the VCF
     */
    public void addFormat(String line) {
        formats.add(parseFormat(line));
    }

    /**
     * Gets the content of the VCF header as a list of Strings, each String correspond to a header
     * line.
     *
     * @return a list with the headers lines
     */
    public List<String> getHeaders() {
        return unprocessedHeaders;
    }

}
