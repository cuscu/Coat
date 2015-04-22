package coat.vcf.view;

import coat.vcf.InfoStats;
import coat.vcf.VcfStats;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

/**
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class StatsReader extends VBox {

    private final ComboBox<InfoStats> infoComboBox = new ComboBox<>();
    private final GridPane gridPane = new GridPane();
    private final TableView statsTable = new TableView();
    private final Label minLabel = new Label("min");
    private final Label min = new Label();
    private final Label maxLabel = new Label("max");
    private final Label max = new Label();


    public StatsReader(VcfStats vcfStats) {
        infoComboBox.getItems().setAll(vcfStats.getInfoStats());
        infoComboBox.getSelectionModel().selectedItemProperty().addListener((obs, prev, current) -> show(current));
        initGridPane();
        getChildren().addAll(infoComboBox, gridPane);
    }

    private void initGridPane() {
        gridPane.add(minLabel, 0, 0);
        gridPane.add(min, 1, 0);
        gridPane.add(maxLabel, 0, 1);
        gridPane.add(max, 1, 1);
    }

    private void show(InfoStats newValue) {
        min.setText(newValue.getMin() + "");
        max.setText(newValue.getMax() + "");
    }
}
