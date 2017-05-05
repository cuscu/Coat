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

package coat.view.vcfcombiner;

import coat.core.tool.Tool;
import coat.utils.OS;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.io.IOException;

/**
 * Main panel of the Combine Vcf Tool.
 *
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class CombineVcfTool extends Tool {

    private final Property<String> title = new SimpleStringProperty(OS.getString("combine.vcf"));

    public CombineVcfTool() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("vcf-combiner.fxml"), OS.getResources());
            final Parent parent = loader.load();
            getChildren().add(parent);
            VBox.setVgrow(parent, Priority.ALWAYS);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Property<String> titleProperty() {
        return title;
    }

}
