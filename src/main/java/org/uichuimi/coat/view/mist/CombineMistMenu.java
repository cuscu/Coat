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

package org.uichuimi.coat.view.mist;

import org.uichuimi.coat.core.tool.Tool;
import org.uichuimi.coat.core.tool.ToolMenu;
import org.uichuimi.coat.utils.OS;

/**
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class CombineMistMenu implements ToolMenu {


    @Override
    public String getName() {
        return OS.getString("combine.mist");
    }

    @Override
    public Tool getTool() {
        return new CombineMIST();
    }

    @Override
    public String getIconPath() {
        return "/img/black/documents_mist.png";
    }
}
