/*
 * Copyright (c) UICHUIMI 2016
 *
 * This file is part of Coat.
 *
 * Coat is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * Coat is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Foobar.
 * If not, see <http://www.gnu.org/licenses/>.
 */

/**
 *
 */
package coat.core.poirot.dataset;

/**
 * @author Lorente-Arencibia, Pascual (pasculorente@gmail.com)
 */
public class OmimEntry {

    // MIM Number	MIM Entry Type (see FAQ 1.3 at http://omim.org/help/faq)	Entrez Gene ID (NCBI)	Approved Gene Symbol (HGNC)	Ensembl Gene ID (Ensembl)
    final String[] values;

    public OmimEntry(String line) {
        values = line.split("\t");
    }

    public String getMimNumber() {
        return values[0];
    }

    public String getType() {
        return values.length > 1 ? values[1] : null;
    }

    public String getGeneId() {
        return values.length > 2 ? values[2] : null;
    }

    public String getApprovedGeneSymbol() {
        return values.length > 3 ? values[3] : null;
    }

    public String getEnsemblGeneId() {
        return values.length > 4 ? values[4] : null;
    }


}
