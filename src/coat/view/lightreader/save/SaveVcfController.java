/*
 * Copyright (c) UICHUIMI 2017
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

package coat.view.lightreader.save;

import htsjdk.variant.vcf.VCFHeader;
import htsjdk.variant.vcf.VCFInfoHeaderLine;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by uichuimi on 9/05/17.
 */
public class SaveVcfController extends BorderPane {
    @FXML
    private FlowPane infos;
    @FXML
    private FlowPane samples;
    private VCFHeader vcfHeader;

    private List[] result = null;

    public SaveVcfController(VCFHeader vcfHeader) {
        this.vcfHeader = vcfHeader;
        try {
            final FXMLLoader loader = new FXMLLoader(getClass()
                    .getResource("save-vcf.fxml"));
            loader.setRoot(this);
            loader.setController(this);
            loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void initialize() {
        for (VCFInfoHeaderLine header : vcfHeader.getInfoHeaderLines()) {
            final CheckBox checkBox = new CheckBox(header.getID());
            infos.getChildren().add(checkBox);
            checkBox.setSelected(true);
        }
        for (String sample : vcfHeader.getSampleNamesInOrder()) {
            final CheckBox checkBox = new CheckBox(sample);
            samples.getChildren().add(checkBox);
            checkBox.setSelected(true);
        }
    }

    @FXML
    private void save() {
        result = new List[2];
        result[0] = new LinkedList<String>();
        result[1] = new LinkedList<String>();
        for (Node node : infos.getChildren()) {
            final CheckBox checkBox = (CheckBox) node;
            if (checkBox.isSelected()) result[0].add(checkBox.getText());
        }
        for (Node node : samples.getChildren()) {
            final CheckBox checkBox = (CheckBox) node;
            if (checkBox.isSelected()) result[1].add(checkBox.getText());
        }
        getScene().getWindow().hide();
    }

    public List[] getLists() {
        return result;
    }
}
