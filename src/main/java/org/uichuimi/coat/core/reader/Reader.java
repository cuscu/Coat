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

package org.uichuimi.coat.core.reader;

import javafx.beans.property.Property;
import javafx.scene.control.Button;

import java.util.List;

/**
 * Abstract class that is used to read a File. Use this class for the controllers of the FXML views.
 * You will have access to the file by the getter and the setter. The method saveAs is called when
 * the 'save as' button. The methods {@code getActions} and {@code getActionsName} are for the
 * buttons bar.
 *
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public interface Reader {

    Property<String> titleProperty();

    /**
     * This method will be called when user clicks on save as button.
     */
    void saveAs();

    /**
     * Get the list of buttons of the tool. Return null if no actions are available.
     *
     * @return the list of buttons or null
     */
    List<Button> getActions();

    /**
     * The title of the actions tab.
     *
     * @return the name of the menu
     */
    String getActionsName();

}
