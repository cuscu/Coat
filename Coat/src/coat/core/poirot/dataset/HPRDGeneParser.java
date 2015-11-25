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
public class HPRDGeneParser {
    private final static AtomicInteger incrementalId = new AtomicInteger(100000);
    private final static String EMPTY = "-";

    public static String[] getEntry(String[] line) {
         /*
            1. Interactor 1 Gene symbol
            2. Interactor 1 HPRD id
            3. Interactor 1 RefSeq id
            4. Interactor 2 Gene symbol
            5. Interactor 2 HPRD id
            6. Interactor 2 RefSeq id
            7. Experiment type (in vivo, in vitro and yeast 2-hybrid)
            8. Pubmed id
             */
        final String id = String.valueOf(incrementalId.getAndIncrement());
        final String aSymbol = HGNC.getStandardSymbol(line[0]);
        final String bSymbol = HGNC.getStandardSymbol(line[3]);
        final String method = EMPTY;
        final String type = line[6];
        final String score = EMPTY;
        final String database = "HPRD";
        return new String[]{database, id, aSymbol, bSymbol, method, type, score};
    }

}
