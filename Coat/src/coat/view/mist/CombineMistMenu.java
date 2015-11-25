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

package coat.view.mist;

import coat.core.tool.Tool;
import coat.core.tool.ToolMenu;

/**
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class CombineMistMenu implements ToolMenu {


    @Override
    public String getName() {
        return "Combine Mist";
    }

    @Override
    public Tool getTool() {
        return new CombineMIST();
    }

    @Override
    public String getIconPath() {
        return "coat/img/black/documents_mist.png";
    }
}
