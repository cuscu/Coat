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
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * info fields are stored in a list. each info is a map.
 *
 * @author Pascual Lorente Arencibia (pasculorente@gmail.com)
 */
public class VCFHeader {

    private List<Map<String, String>> infos = new ArrayList<>();

    /**
     * Creates a new VCFHeader using the info of the vcfFile.
     *
     * @param vcfFile the vcfFile to parse
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
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
    }

    Map<String, String> parseInfo(String line) {
        Map<String, String> map = new TreeMap<>();
        String substring = line.substring(8, line.length() - 1);
        InfoTokenizer it = new InfoTokenizer(substring);
        String token;
        while ((token = it.nextToken()) != null) {
            // =
            it.nextToken();
            // value
            String value = it.nextToken();
            map.put(token, value);
        }
        return map;

    }

    private class InfoTokenizer {

        private String line;

        public InfoTokenizer(String line) {
            this.line = line;
        }

        private String nextToken() {
            if (line.isEmpty()) {
                return null;
            }
            int pos = 0;
            String token;
            switch (line.charAt(pos)) {
                case '"':
                    int endQuotePosition = line.indexOf("\"", pos + 1);
                    token = line.substring(pos + 1, endQuotePosition);
                    line = line.substring(endQuotePosition + 1);
                    break;
                case '=':
                    token = "=";
                    line = line.substring(1);
                    break;
                default:
                    while (line.charAt(pos) != '=' && line.charAt(pos) != ',') {
                        pos++;
                    }
                    token = line.substring(0, pos);
                    line = line.substring(pos);
            }
            if (line.startsWith(",")) {
                line = line.substring(1);
            }
            return token;
        }

    }

    /**
     * Gets the list of infos. Each info is a map (key=value), as the VCF ##INFO= line. Use
     * {@code getInfos().get("ID")} to get th ID.
     *
     * @return the list of infos.
     */
    public List<Map<String, String>> getInfos() {
        return infos;
    }

}
