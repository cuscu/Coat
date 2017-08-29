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

package coat.view.vcfreader.filter;

import coat.utils.OS;
import coat.view.graphic.SizableImageView;
import coat.view.vcfreader.VariantsTable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * Created by uichuimi on 1/07/16.
 */
public class BooleanFilterColumn<S, T> extends FilterTableColumn<S, T> {

    private final RadioButton trueBox = new RadioButton(String.valueOf(true));
    private final RadioButton falseBox = new RadioButton(String.valueOf(false));
    private final RadioButton anyBox = new RadioButton("Any");

    private final Stage stage = new Stage(StageStyle.UTILITY);

    public BooleanFilterColumn(VariantsTable table, String title) {
        super(table, title);
        createFilterMenu();
        anyBox.setSelected(true);
    }

    private void createFilterMenu() {
        addContextMenu();
        final ToggleGroup group = new ToggleGroup();
        trueBox.setToggleGroup(group);
        anyBox.setToggleGroup(group);
        falseBox.setToggleGroup(group);
        trueBox.setOnAction(event -> updateTable());
        falseBox.setOnAction(event -> updateTable());
        anyBox.setOnAction(event -> updateTable());
        final VBox options = new VBox(5, trueBox, falseBox, anyBox);
        final HBox hBox = new HBox(10, new Label(getText()), options);
        hBox.setPadding(new Insets(5));
        hBox.setAlignment(Pos.CENTER_LEFT);
        stage.setScene(new Scene(hBox));
        stage.setTitle(getText());
    }

    private void addContextMenu() {
        final ImageView filterIcon = new SizableImageView("coat/img/black/filter.png", SizableImageView.SMALL_SIZE);
        final MenuItem filterMenuItem = new MenuItem(OS.getString("filter"), filterIcon);
        setContextMenu(new ContextMenu(filterMenuItem));
        filterMenuItem.setOnAction(event -> showFilterMenu());
    }

    private void showFilterMenu() {
        if (stage.isShowing()) stage.requestFocus();
        else stage.show();
    }

    @Override
    protected boolean filter(S item, T value) {
        if (anyBox.isSelected()) return true;
        final boolean val = Boolean.valueOf((String) value);
        return val && trueBox.isSelected() || !val && falseBox.isSelected();
    }

    @Override
    public void clear() {
        anyBox.setSelected(true);
    }
}
