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
package coat.model.vcfreader;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Returns a LinkedHashMap with the content of the line parsed. So "ID=AC,Number=A,Type=Integer"
 * becomes a map. This class is convenient to parse almost any VCF header lines.
 *
 * @author UICHUIMI
 */
class MapGenerator {

    private final static char QUOTE = '"';
    private final static char COMMA = ',';
    private final static char EQUALS = '=';

    private static int cursor;
    private static String key;
    private static String value;
    private static Map<String, String> map = new LinkedHashMap<>();
    private static String line;
    private static boolean isKey;

    /**
     * @param line line to map, without ##INFO neither ##FORMAT neither &lt neither &gt
     * @return a map with the content of the line
     */
    public synchronized static Map<String, String> parse(String line) {
        MapGenerator.line = line;
        map = new HashMap<>();
        cursor = 0;
        isKey = true;
        return start();
    }

    private static Map<String, String> start() {
        while (cursor < line.length()) nextCharacter();
        return map;
    }

    private static void nextCharacter() {
        switch (line.charAt(cursor)) {
            case QUOTE:
                putQuotedValue();
                break;
            case EQUALS:
                // Equals symbol: cursor at next position and expected a value
                cursor++;
                isKey = false;
                break;
            case COMMA:
                // Comma symbol, cursor at next position and expected a key
                cursor++;
                isKey = true;
                break;
            default:
                putUnquotedValue();
        }
    }

    private static void putUnquotedValue() {
        int end = endOfToken();
        if (isKey)
            key = line.substring(cursor, end);
        else {
            value = line.substring(cursor, end);
            map.put(key, value);
        }
        cursor = end;
    }

    private static int endOfToken() {
        // Text not in quotes
        // token is the text between cursor and next "=" or ","
        // cursor at "=" or ","
        int end = cursor;
        while (line.charAt(end) != EQUALS && line.charAt(end) != COMMA) end++;
        return end;
    }

    private static void putQuotedValue() {
        // If isKey is false, something went wrong
        // Text in quotes
        // token is the text between quotes
        // place cursor at next position after end quote
        int endQuotePosition = line.indexOf(QUOTE, cursor + 1);
        value = line.substring(cursor + 1, endQuotePosition);
        cursor = endQuotePosition + 1;
        map.put(key, value);
    }

}
