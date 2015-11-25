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

/**
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class ProteinAtlasParser {
    public static String[] getEntry(String[] line) {
        /*
            0 Gene
            1 Gene name
            2 Tissue
            3 Cell type
            4 Level
            5 Expression type
            6 Reliability
          */
        final String gene = HGNC.getStandardSymbol(line[1]);
        final String expression = line[2];
        final String status = line[4];
        if (status.equals("Low") || status.equals("Medium") || status.equals("High"))
            return new String[]{"ProteinAtlas", gene, expression, status};
        return null;
    }


}
