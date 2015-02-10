/*
 * Copyright (C) 2014 UICHUIMI
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
package coat.reader;

import java.io.File;
import java.util.List;
import javafx.scene.control.Button;

/**
 * Abstract class that is used to read a File. Use this class for the controllers of the FXML views.
 * You will have access to the file by the getter and the setter. The method saveAs is called when
 * the 'save as' button. The methods {@code getActions} and {@code getActionsName} are for the
 * buttons bar.
 *
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public abstract class Reader {

    /**
     * Associated file.
     */
    protected File file;

    public void setFile(File file) {
        this.file = file;
    }

    public File getFile() {
        return file;
    }

    /**
     * This method will be callled when user clicks on save as button.
     */
    public abstract void saveAs();

    /**
     * Get the list of buttons of the tool. Return null if no actions are available.
     *
     * @return the list of buttons or null
     */
    public abstract List<Button> getActions();

    /**
     * The title of the actions tab.
     *
     * @return
     */
    public abstract String getActionsName();

}
