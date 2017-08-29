/*
 * Copyright (c) UICHUIMI 2017
 *
 * This file is part of Coat.
 *
 * Coat is free software:
 * you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * Coat is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with Coat.
 *
 * If not, see <http://www.gnu.org/licenses/>.
 *
 */

package coat.view.vcfcombiner;

import coat.core.tool.Tool;
import coat.core.tool.ToolMenu;
import coat.utils.OS;

/**
 * Menu entry of the Combine Vcf tool.
 *
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class CombineVcfMenu implements ToolMenu {
    @Override
    public String getName() {
        return OS.getString("combine.vcf");
    }

    @Override
    public String getIconPath() {
        return "coat/img/black/documents_vcf.png";
    }

    @Override
    public Tool getTool() {
        return new CombineVcfTool();
    }
}
