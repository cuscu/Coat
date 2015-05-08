package coat.vcf.view;

import coat.vcf.InfoStats;
import coat.vcf.VcfStats;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

import java.util.Map;

/**
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class StatsReader extends HBox {

    public static final int MAX_VALUES = 7;
    private final ListView<String> infoListView = new ListView<>();
    private final GridPane gridPane = new GridPane();
    private final Label minLabel = new Label("min");
    private final Label min = new Label();
    private final Label maxLabel = new Label("max");
    private final Label max = new Label();
    private final PieChart pieChart = new PieChart();
    private final HBox editPane = new HBox();

    private final VcfStats vcfStats;

    public StatsReader(VcfStats vcfStats) {
        this.vcfStats = vcfStats;
        infoListView.getItems().addAll(vcfStats.getStats().keySet());
        infoListView.getSelectionModel().selectedItemProperty().addListener((obs, prev, current) -> show(current));
        initGridPane();
        getChildren().addAll(infoListView, editPane);
    }

    private void initGridPane() {
        gridPane.add(minLabel, 0, 0);
        gridPane.add(min, 1, 0);
        gridPane.add(maxLabel, 0, 1);
        gridPane.add(max, 1, 1);
    }

    private void show(String newValue) {
        InfoStats infoStats = vcfStats.getStats().get(newValue);
        if (infoStats.getCounts().size() < infoStats.getValues().size()) asNumber(infoStats);
        else asString(infoStats);
    }

    private void asString(InfoStats infoStats) {
        final Map<String, Integer> counts = infoStats.getCounts();
        System.out.println("Generating data");
        pieChart.getData().clear();
        counts.forEach((s, integer) -> {
            PieChart.Data data = new PieChart.Data(s, integer);
            addToPieChart(data);
        });
        editPane.getChildren().setAll(pieChart);
    }

    private void addToPieChart(PieChart.Data data) {
        insertInOrder(data);
        if (pieChart.getData().size() > MAX_VALUES){
            double sum = 0.0;
            while (pieChart.getData().size() > MAX_VALUES) {
                sum += pieChart.getData().get(MAX_VALUES).getPieValue();
                pieChart.getData().remove(MAX_VALUES);
            }
            PieChart.Data others = new PieChart.Data("others", sum);
            pieChart.getData().add(others);
        }
    }

    private void insertInOrder(PieChart.Data data) {
        boolean added = false;
        for (int i = 0; i < pieChart.getData().size(); i++) {
            if (pieChart.getData().get(i).getPieValue() > data.getPieValue()) {
                pieChart.getData().add(i, data);
                added = true;
            }
        }
        if (!added) pieChart.getData().add(data);

    }

    private void asNumber(InfoStats infoStats) {
        min.setText(infoStats.getValues().stream().min(Double::compare) + "");
        max.setText(infoStats.getValues().stream().max(Double::compare) + "");
        editPane.getChildren().setAll(gridPane);
    }
}
