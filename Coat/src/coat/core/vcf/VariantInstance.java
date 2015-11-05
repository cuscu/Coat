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

package coat.core.vcf;

import coat.core.poirot.dataset.Dataset;
import coat.core.poirot.dataset.Instance;

/**
 * Custom instance for a Variant. The Dataset must contain the meta info of a VCF file.
 *
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class VariantInstance extends Instance {

    public VariantInstance(Dataset dataset, Object[] fields) {
        super(dataset, fields);
    }

    public String getChromosome() {
        return (String) getField(0);
    }

    public int getPosition() {
        return (int) getField(1);
    }

    public String getId() {
        return (String) getField(2);
    }

    public String getReference() {
        return (String) getField(3);
    }

    public String getAlternative() {
        return (String) getField(4);
    }

    public double getQuality() {
        return (double) getField(5);
    }

    public String getFilter() {
        return (String) getField(6);
    }

    //#CHROM	POS	ID	REF	ALT	QUAL	FILTER	INFO	FORMAT	sqz_001


}
