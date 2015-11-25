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

package coat.core.poirot.dataset;

import coat.core.poirot.dataset.hgnc.HGNC;
import org.jetbrains.annotations.NotNull;

/**
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class MenthaParser {

    @NotNull
    public static String[] getEntry(String[] line) {
        /*
            uniprotkb:Q9JJ00
            uniprotkb:O35626
            -
            -
            uniprotkb:PLSCR1(gene name)
            uniprotkb:RASD1(gene name)
            psi-mi:"MI:0018"(two hybrid)
            -pubmed:21247419
            taxid:10090(Mus musculus)
            taxid:10090(Mus musculus)
            psi-mi:"MI:0915"(physical association)
            psi-mi:"MI:0469"(IntAct)
            intact:EBI-4325299
            mentha-score:0.309
        */
        final String method = extractMethod(line[6]);
        final String type = extractType(line[11]);
        final String database = extractDatabase(line[12]);
        final String score = extractScore(line[14]);
        final String id = extractId(line[13]);
        final String aSymbol = HGNC.getStandardSymbol(extractGeneName(line[4]));
        final String bSymbol = HGNC.getStandardSymbol(extractGeneName(line[5]));
        return new String[]{database, id, aSymbol, bSymbol, method, type, score};
    }

    private static String extractScore(String text) {
        // mentha-score:0.081
        if (text.startsWith("mentha-score:")) return text.substring(13);
        if (!text.equals("-")) System.err.println(text);
        return null;
    }

    private static String extractDatabase(String text) {
        // psi-mi:"MI:0463"(biogrid)
        return extractMethod(text);
    }

    private static String extractType(String text) {
        // psi-mi:"MI:0403"(colocalization)
        return extractMethod(text);
    }

    private static String extractMethod(String text) {
        // psi-mi:"MI:0401"(biochemical)
        if (text.startsWith("psi-mi")) {
            final int startPos = text.indexOf("(");
            final int endPos = text.indexOf(")");
            if (startPos > 0 && endPos > 0) return text.substring(startPos + 1, endPos);
        }
        System.err.println(text);
        return null;
    }

    private static String extractGeneName(String text) {
        // uniprotkb:MDP1(gene name)
        if (text.startsWith("uniprotkb:")) {
            int index = text.indexOf("(");
            if (index > 0) {
                String id = text.substring(10, index);
                if (id.contains(" ")) id = id.split(" ")[0].replace("\"", "");
                return id;
            }
        }
        System.err.println(text);
        return null;
    }

    private static String extractId(String text) {
        // BIOGRID:749408
        int index = text.indexOf(":");
        if (index > 0) return text.substring(index + 1);
        System.err.println(text);
        return null;
    }

}
