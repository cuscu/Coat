package coat.view.vcfreader;

import coat.model.vcfreader.Variant;
import javafx.geometry.Insets;
import javafx.scene.control.TableCell;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

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
            String codon = (String) item.getInfos().get("COD");
            if (codon == null){
                ref.setText(item.getRef());
                alt.setText(item.getAlt());
            } else {
                String [] cods = codon.split("[-/]");
                String amino = (String) item.getInfos().get("AMINO");
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
