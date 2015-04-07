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

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Returns a LinkedHashMap with the content of the line parsed. So "ID=AC,Number=A,Type=Integer"
 * becomes a map. This class is convenient to parse almost any VCF header lines.
 *
 * @author UICHUIMI
 */
public class MapGenerator {

    private final static String QUOTE = "\"";
    private final static String COMMA = ",";

    private int cursor = 0;
    private String key;
    private String value;
    private final Map<String, String> map = new LinkedHashMap<>();
    private String line;
    private boolean isKey = true;

    /**
     *
     * @param line line to map, without ##INFO neither ##FORMAT neither &lt neither &gt
     * @return a map with the content of the line
     */
    public Map<String, String> parse(String line) {
        this.line = line;
        start();
        return map;
    }

    private void start() {
        while (cursor < line.length())
            nextCharacter();
    }

    private void nextCharacter() {
        switch (line.charAt(cursor)) {
            case '"':
                putQuotedValue();
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
                putUnquotedValue();
        }
    }

    private void putUnquotedValue() {
        int end = endOfToken();
        if (isKey)
            key = line.substring(cursor, end);
        else {
            value = line.substring(cursor, end);
            map.put(key, value);
        }
        cursor = end;
    }

    private int endOfToken() {
        // Text not in quotes
        // token is the text between cursor and next "=" or ","
        // cursor at "=" or ","
        int end = cursor;
        while (line.charAt(end) != '=' && line.charAt(end) != ',')
            end++;
        return end;
    }

    private void putQuotedValue() {
        // If isKey is false, something went wrong
        // Text in quotes
        // token is the text between quotes
        // place cursor at next position after end quote
        int endQuotePosition = line.indexOf("\"", cursor + 1);
        value = line.substring(cursor + 1, endQuotePosition);
        cursor = endQuotePosition + 1;
        map.put(key, value);
    }

}
