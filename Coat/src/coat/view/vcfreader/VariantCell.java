package coat.view.vcfreader;

import coat.model.vcfreader.Variant;
import coat.view.graphic.NaturalCell;

/**
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class VariantCell extends NaturalCell {

    @Override
    protected void updateItem(Object item, boolean empty) {
        super.updateItem(item, empty);
        if (!empty && getTableRow() != null) {
            getTableRow().getStyleClass().removeAll("protein-coding", "damaging");
            Variant v = (Variant) getTableRow().getItem();
            if (v != null) {
                final String biotype = (String) v.getInfos().getOrDefault("BIO", "");
                switch (biotype) {
                    case "protein_coding":
                        getTableRow().getStyleClass().add("protein-coding");
                        break;
                }
                final String siftp = (String) v.getInfos().getOrDefault("SIFTp", "");
                switch (siftp) {
                    case "DAMAGING":
                        getTableRow().getStyleClass().add("damaging");
                }
            }
        }

    }

}
