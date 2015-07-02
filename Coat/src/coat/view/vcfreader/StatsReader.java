package coat.view.vcfreader;

import coat.model.vcfreader.InfoStats;
import coat.model.vcfreader.VcfStats;
import javafx.geometry.Pos;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

import java.util.*;

/**
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class StatsReader extends HBox {

    public static final int MAX_VALUES = 7;
    public static final int ITEMS_PIE_CHART = 5;
    private final ListView<String> infoListView = new ListView<>();
    private final GridPane gridPane = new GridPane();
    private final Label minLabel = new Label("min");
    private final Label min = new Label();
    private final Label maxLabel = new Label("max");
    private final Label max = new Label();
    private final PieChart pieChart = new PieChart();
    private final HBox editPane = new HBox();
    private final Label noData = new Label("Not enough data");
    private final BarChart<String, Number> barChart = new BarChart<>(new CategoryAxis(), new NumberAxis());

    private final VcfStats vcfStats;

    public StatsReader(VcfStats vcfStats) {
        this.vcfStats = vcfStats;
        infoListView.getItems().addAll(vcfStats.getStats().keySet());
        infoListView.getSelectionModel().selectedItemProperty().addListener((obs, prev, current) -> show(current));
        setAlignment(Pos.CENTER);
        HBox.setHgrow(editPane, Priority.ALWAYS);
        initGridPane();
        getChildren().addAll(infoListView, editPane);
        editPane.setAlignment(Pos.CENTER);
        HBox.setHgrow(pieChart, Priority.ALWAYS);
        HBox.setHgrow(barChart, Priority.ALWAYS);
        configurePieChart();
    }

    private void configurePieChart() {
        pieChart.setStartAngle(90);

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
        pieChart.getData().clear();
        final Map<String, Integer> counts = infoStats.getCounts();
        if (counts.isEmpty()) {
            editPane.getChildren().setAll(noData);
        } else {
            final List<String> names = new ArrayList<>();
            counts.keySet().forEach(names::add);
            Collections.sort(names, (key1, key2) -> counts.get(key2).compareTo(counts.get(key1)));
            if (counts.get(names.get(0)) > 1) {
                int sum = 0;
                for (int i = 0; i < ITEMS_PIE_CHART && i < names.size(); i++) {
                    final String name = names.get(i);
                    final int count = counts.get(name);
                    sum += count;
                    pieChart.getData().add(new PieChart.Data(name + "(" + count + ")", count));
                }
                if (counts.size() > ITEMS_PIE_CHART) {
                    int others = 0;
                    for (int i = ITEMS_PIE_CHART; i < names.size(); i++) others += counts.get(names.get(i));
                    if (others > 0 && others < sum)
                        pieChart.getData().add(new PieChart.Data("Others(" + others + ")", others));

                }
                editPane.getChildren().setAll(pieChart);
            } else editPane.getChildren().setAll(noData);
        }
    }

    private void asNumber(InfoStats infoStats) {
        double minValue = infoStats.getValues().stream().min(Double::compare).get();
        double maxValue = infoStats.getValues().stream().max(Double::compare).get();
        double range = maxValue - minValue;
        Map<Double, Integer> map = new TreeMap<>();
        infoStats.getValues().forEach(value -> {
            double interval = Math.floor(((value - minValue) * 50 / range)) * range * 0.02 + minValue;
            int current = map.getOrDefault(interval, 0);
            map.put(interval, current + 1);
        });
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        map.forEach((x, y) -> series.getData().add(new XYChart.Data<>(String.format("%.2f", x), y)));
        barChart.getData().clear();
        barChart.getData().add(series);
        editPane.getChildren().setAll(barChart);
    }
}
