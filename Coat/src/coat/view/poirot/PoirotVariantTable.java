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

package coat.view.poirot;

import coat.core.variant.Variant;
import coat.utils.OS;
import coat.view.graphic.NaturalCell;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.jetbrains.annotations.NotNull;

/**
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class PoirotVariantTable extends TableView<Variant> {

    PoirotVariantTable() {
        getColumns().addAll(getCoordinateColumn(), getVariantColumn(), getIdColumn(), getConsequenceColumn(), getSIFTColumn());
        setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        setMaxHeight(200);
    }

    private @NotNull TableColumn<Variant, String> getCoordinateColumn() {
        final TableColumn<Variant, String> coordinate = new TableColumn<>(OS.getString("coordinate"));
        coordinate.setCellFactory(param -> new NaturalCell<>());
        coordinate.setCellValueFactory(param -> new SimpleObjectProperty<>(getCoordinate(param.getValue())));
        return coordinate;
    }

    private TableColumn<Variant, String> getVariantColumn() {
        final TableColumn<Variant, String> variant = new TableColumn<>(OS.getString("variant"));
        variant.setCellFactory(param -> new NaturalCell<>());
        variant.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getRef() + "/" + param.getValue().getAlt()));
        return variant;
    }

    @NotNull
    private TableColumn<Variant, String> getIdColumn() {
        final TableColumn<Variant, String> id = new TableColumn<>(OS.getString("id"));
        id.setCellFactory(param -> new NaturalCell<>());
        id.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getId()));
        return id;
    }

    private TableColumn<Variant, String> getBiotypeColumn() {
        final TableColumn<Variant, String> biotype = new TableColumn<>(OS.getString("biotype"));
        biotype.setCellFactory(param -> new NaturalCell<>());
        biotype.setCellValueFactory(param -> new SimpleObjectProperty<>((String) param.getValue().getInfo("BIO")));
        return biotype;
    }

    private TableColumn<Variant, String> getConsequenceColumn() {
        final TableColumn<Variant, String> consequences = new TableColumn<>(OS.getString("consequences"));
        consequences.setCellFactory(param -> new NaturalCell<>());
        consequences.setCellValueFactory(param -> new SimpleObjectProperty<>((String) param.getValue().getInfo("CONS")));
        return consequences;
    }

    private TableColumn<Variant, String> getSIFTColumn() {
        final TableColumn<Variant, String> sifts = new TableColumn<>("SIFTs");
        sifts.setCellFactory(param -> new NaturalCell<>());
        sifts.setCellValueFactory(param -> new SimpleObjectProperty<>((String) param.getValue().getInfo("SIFTs")));
        return sifts;
    }

    private String getCoordinate(Variant variant) {
        return variant.getChrom() + ":" + variant.getPos();
    }
}
