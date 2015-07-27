package coat.view.vcfreader;

import coat.model.vcfreader.Variant;
import javafx.scene.control.TableRow;

/**
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class VcfRow extends TableRow<Variant> {

    @Override
    protected void updateItem(Variant item, boolean empty) {
        super.updateItem(item, empty);
        if (!empty) {
            getStyleClass().removeAll("protein-coding", "damaging");
            final String biotype = (String) item.getInfos().getOrDefault("BIO", "");
            switch (biotype) {
                case "protein_coding":
                    getStyleClass().add("protein-coding");
                    break;
            }
            final String siftp = (String) item.getInfos().getOrDefault("SIFTp", "");
            switch (siftp) {
                case "DAMAGING":
                    getStyleClass().add("damaging");
            }


        }
    }
}
