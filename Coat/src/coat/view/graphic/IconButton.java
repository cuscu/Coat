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

package coat.view.graphic;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;

/**
 * A button that only shows an icon. Text is displayed on mouse hover, as a Tooltip. Also, the "graphic-button" CSS class
 * is added.
 *
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class IconButton extends Button {

    public static final String GRAPHIC_BUTTON = "graphic-button";

    /**
     * name is displayed on tooltip. This node has "graphic-button" CSS class.
     *
     * @param name button name/text to display
     * @param icon image or graphic to show
     */
    public IconButton(String name, Node icon) {
        super(null, icon);
        setTooltip(new Tooltip(name));
        getStyleClass().add(GRAPHIC_BUTTON);
    }
}
