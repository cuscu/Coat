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

package coat.view.vcfreader;

import javafx.geometry.Insets;
import javafx.scene.control.TableCell;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import vcf.Variant;

/**
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
class VariantCard extends TableCell<Variant, Variant> {

    private final TextField ref = new TextField();
    private final TextField alt = new TextField();
    private final VBox variantBox = new VBox(ref, alt);

    public VariantCard() {
        setText(null);
        makeNatural(ref);
        makeNatural(alt);
        ref.styleProperty().bind(styleProperty());
    }

    private void makeNatural(TextField textField) {
        textField.setMinWidth(0);
        textField.setBackground(null);
        textField.setEditable(false);
        textField.setPadding(new Insets(0));
        textField.setOnMouseClicked(event ->  getTableView().getSelectionModel().select(getIndex()));
    }

    @Override
    protected void updateItem(Variant item, boolean empty) {
        super.updateItem(item, empty);
        if (!empty && item != null) {
            String codon = (String) item.getInfo().getInfo("COD");
            if (codon == null){
                ref.setText(item.getRef());
                alt.setText(item.getAlt());
            } else {
                String [] cods = codon.split("[-/]");
                String amino = (String) item.getInfo().getInfo("AMINO");
                if (amino == null){
                    ref.setText(cods[0]);
                    alt.setText(cods[1]);
                } else {
                    String [] aminos = amino.split("[-/]");
                    ref.setText(cods[0] + " (" + aminos[0] + ")");
                    alt.setText(cods[1] + " (" + (aminos.length > 1 ? aminos[1] : aminos[0]) + ")");
                }

            }

            setGraphic(variantBox);
        } else {
            setGraphic(null);
        }
    }


}
