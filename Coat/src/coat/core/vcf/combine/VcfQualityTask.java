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

package coat.core.vcf.combine;

import coat.core.vcf.Variant;
import coat.view.vcfreader.VcfSample;
import javafx.concurrent.Task;

import java.util.List;

/**
 * This task will score each variant with metadata generated from the bam and mist files related with the samples
 * genetic information.
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 * @author Jacob
 */
class VcfQualityTask extends Task<List<Variant>> {

    private final List<Variant> variants;
    private final List<VcfSample> samples;

    public VcfQualityTask(List<Variant> variants, List<VcfSample> samples) {
        this.variants = variants;
        this.samples = samples;
    }


    @Override
    protected List<Variant> call() throws Exception {
        return variants;
    }

}
