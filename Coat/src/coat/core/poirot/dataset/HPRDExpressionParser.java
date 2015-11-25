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

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class HPRDExpressionParser {

    private final static AtomicInteger counter = new AtomicInteger(100000);

    public static String [] getEntry(String[] line) {
         /*
         0 hprd_id
         1 refseq_id
         2 geneSymbol
         3 expression_term
         4 status
         5 reference_id
         */
        final String gene = HGNC.getStandardSymbol(line[2]);
        final String expression = line[3];
        final String status = line[4];
        return new String[]{"HPRD", gene, expression.toLowerCase(), status};
    }
}
