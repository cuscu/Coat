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
package coat.vcf;

import coat.graphic.NaturalCell;
import coat.utils.OS;
import java.util.Map;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

/**
 * The table that shows the INFO of a variant.
 *
 * @author Pascual Lorente Arencibia (pasculorente@gmail.com)
 */
public class VariantInfoTable extends TableView<Info> implements VariantListener {

    private final TableColumn<Info, String> name = new TableColumn(OS.getResources().getString("name"));
    private final TableColumn<Info, String> value = new TableColumn(OS.getResources().getString("value"));
    private final TableColumn<Info, String> description = new TableColumn(OS.getResources().getString("description"));

    /**
     * Creates a new VariantInfoTable.
     */
    public VariantInfoTable() {
        setColumnResizePolicy(CONSTRAINED_RESIZE_POLICY);
        name.setCellValueFactory(new PropertyValueFactory("name"));
        value.setCellValueFactory(new PropertyValueFactory("value"));
        description.setCellValueFactory(new PropertyValueFactory("description"));
        name.setCellFactory(param -> new NaturalCell());
        value.setCellFactory(param -> new NaturalCell());
        description.setCellFactory(param -> new NaturalCell());
        getColumns().addAll(name, value, description);

    }

    @Override
    public void variantChanged(Variant variant, VCFHeader vcfHeader) {
        getItems().clear();
        if (variant != null) {
            for (String info : variant.getInfo().split(";")) {
                String[] pair = info.split("=");
                String id = pair[0];
                String desc = "";
                for (Map<String, String> property : vcfHeader.getInfos()) {
                    if (property.get("ID").equals(id)) {
                        desc = property.getOrDefault("Description", "");
                    }
                }
                String val = pair.length > 1 ? pair[1] : "yes";
                Info i = new Info(id, val, desc);
                getItems().add(i);
            }
        }
    }

}
