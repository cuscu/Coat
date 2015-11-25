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
public class BiogridParser {

    public static String[] getEntry(String[] line) {
    /*
        #BioGRID Interaction ID=152330
        Entrez Gene Interactor A=851811
        Entrez Gene Interactor B=851810
        BioGRID ID Interactor A=32277
        BioGRID ID Interactor B=32276
        Systematic Name Interactor A=YDR225W
        Systematic Name Interactor B=YDR224C
        Official Symbol Interactor A=HTA1
        Official Symbol Interactor B=HTB1
        Synonyms Interactor A=H2A1|SPT11|histone H2A|L000000827
        Synonyms Interactor B=SPT12|histone H2B|L000000829
        Experimental System=Co-purification
        Experimental System Type=physical
        Author=Grant PA (1999)
        Pubmed ID=10026213
        Organism Interactor A=559292
        Organism Interactor B=559292
        Throughput=Low Throughput
        Score=-
        Modification=-
        Phenotypes=-
        Qualifications=-
        Tags=-
        Source Database=BIOGRID
     */
        final String id = line[0];
        final String aSymbol = HGNC.getStandardSymbol(line[7]);
        final String bSymbol = HGNC.getStandardSymbol(line[8]);
        final String method = line[11];
        final String type = line[12];
        final String score = line[18];
        final String database = line[23].toLowerCase();
        return new String[]{database, id, aSymbol, bSymbol, method, type, score};
    }
}
