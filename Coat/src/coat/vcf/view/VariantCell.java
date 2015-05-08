package coat.vcf.view;

import coat.graphic.NaturalCell;
import coat.vcf.Variant;

/**
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class VariantCell extends NaturalCell {

    @Override
    protected void updateItem(Object item, boolean empty) {
        super.updateItem(item, empty);
        getStyleClass().clear();
        if (!empty && getTableRow() != null) {
            Variant v = (Variant) getTableRow().getItem();
            if (v != null) {
                final String status = (String) v.getInfos().getOrDefault("BIO", "");
                switch (status) {
                    case "protein_coding":
                        setStyle("-fx-text-fill: forestgreen");
                        break;
                    case "wrong":
                    case "warning":
                    default:
                        setStyle(null);
                }
                final String siftp = (String) v.getInfos().getOrDefault("SIFTp", "");
                switch (siftp){
                    case "DAMAGING":
                        setStyle("-fx-text-fill: darkred");
                }
            }
        }
    }

}
