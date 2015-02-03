/*
 * Copyright (C) 2014 uichuimi03
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
package coat.vcf;

/**
 *
 * @author Pascual Lorente Arencibia
 */
public interface VariantListener {

    /**
     * Called when the user has selected a different variant inthe variants table.
     *
     * @param variant the new variant
     * @param header the headers of the VCF file
     */
    public void variantChanged(Variant variant, VCFHeader header);
}
