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

package coat.core.poirot.dataset.hgnc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 * Standard gene name database. Use <code>getStandardSymbol()</code> to get the standard HGNC symbol of the gene, and
 * <code>getName()</code> to get the whole name of the gene.
 * <p>
 * 0 HGNC ID
 * 1 Approved Symbol
 * 2 Approved Name
 * 3 Previous Symbols
 * 4 Synonyms
 *
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class HGNCDatabase {

    private final static Map<String, String> symbols = new HashMap<>();
    private final static Map<String, String> names = new HashMap<>();

    static {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(HGNCDatabase.class.getResourceAsStream("hgnc-complete.tsv.gz"))))) {
            reader.readLine();
            reader.lines().map(line -> line.split("\t")).forEach(row -> {
                final String standardName = row[1];
                final String[] previousNames = row[3].split("\\|");
                Arrays.stream(previousNames).forEach(name -> symbols.put(name, standardName));
                final String[] synonyms = row[4].split("\\|");
                Arrays.stream(synonyms).forEach(name -> symbols.put(name, standardName));
                names.put(standardName, row[2]);
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets the HGNC standard Symbol of the given gene.
     *
     * @param symbol current symbol
     * @return the standard symbol or the passed symbol if not standard value available
     */
    public static String getStandardSymbol(String symbol) {
        return symbols.getOrDefault(symbol, symbol);
    }

    /**
     * Gets the name (or description) of the gene.
     *
     * @param symbol standard symbol. Get it using <code>getStandardSymbol()</code>
     * @return the name or null if not available
     */
    public static String getName(String symbol) {
        return names.get(symbol);
    }
}
