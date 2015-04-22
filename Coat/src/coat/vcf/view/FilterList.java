package coat.vcf.view;

import coat.utils.OS;
import coat.vcf.Variant;
import coat.vcf.VcfFilter;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class FilterList extends VBox {

    private final ListView<VcfFilter> filters = new ListView<>();
    private ObservableList<Variant> inputVariants = FXCollections.observableArrayList();
    private final ObservableList<Variant> outputVariants = FXCollections.observableArrayList();

    private final Button addFilter = new Button(OS.getResources().getString("add.filter"));
    private final Button addFrequencyFilters = new Button(OS.getResources().getString("add.frequency.filters"));
    private final HBox buttons = new HBox(5, addFilter, addFrequencyFilters);

    private final ChangeListener applyFilters = (observable, oldValue, newValue) -> applyFilters();
    private final ObservableList<Map<String, String>> infos = FXCollections.observableArrayList();
    private final ListChangeListener<Variant> variantsChangedListener = (ListChangeListener<Variant>) c -> applyFilters();

    public FilterList() {
        configureButtons();
        configureFilters();
        this.getChildren().setAll(buttons, filters);
    }

    private void configureFilters() {
        filters.setEditable(true);
        filters.setCellFactory(param -> new FilterCell(infos));
        filters.getItems().addListener((ListChangeListener<VcfFilter>) change -> {
            change.next();
            if (change.wasAdded()) change.getAddedSubList().forEach(this::bindFilter);
            else if (change.wasRemoved()) change.getRemoved().forEach(this::unbindFilter);
            applyFilters();
        });
    }

    private void unbindFilter(VcfFilter filter) {
        filter.getValueProperty().removeListener(applyFilters);
        filter.getInfoProperty().removeListener(applyFilters);
        filter.getConnectorProperty().removeListener(applyFilters);
        filter.getFieldProperty().removeListener(applyFilters);
        filter.getEnabledProperty().removeListener(applyFilters);
        filter.getStrictProperty().removeListener(applyFilters);
    }

    private void bindFilter(VcfFilter filter) {
        filter.getValueProperty().addListener(applyFilters);
        filter.getInfoProperty().addListener(applyFilters);
        filter.getConnectorProperty().addListener(applyFilters);
        filter.getFieldProperty().addListener(applyFilters);
        filter.getEnabledProperty().addListener(applyFilters);
        filter.getStrictProperty().addListener(applyFilters);
    }

    private void configureButtons() {
        addFilter.setOnAction(event -> addFilter());
        addFrequencyFilters.setOnAction(event -> addFrequencyFilters());
        buttons.setAlignment(Pos.CENTER);
        buttons.setPadding(new Insets(5));
        VBox.setVgrow(filters, Priority.ALWAYS);

    }

    public void setInputVariants(ObservableList<Variant> inputVariants) {
        this.inputVariants.removeListener(variantsChangedListener);
        this.inputVariants = inputVariants;
        this.inputVariants.addListener(variantsChangedListener);
        applyFilters();
    }

    public void setInfos(ObservableList<Map<String, String>> infos){
        this.infos.setAll(infos);
    }

    public ObservableList<Variant> getOutputVariants() {
        return outputVariants;
    }

    private void addFrequencyFilters() {
        VcfFilter[] filters = {
                new VcfFilter(VcfFilter.Field.INFO, "AA_F", VcfFilter.Connector.LESS, "0.01"),
                new VcfFilter(VcfFilter.Field.INFO, "AFR_F", VcfFilter.Connector.LESS, "0.01"),
                new VcfFilter(VcfFilter.Field.INFO, "EUR_F", VcfFilter.Connector.LESS, "0.01"),
                new VcfFilter(VcfFilter.Field.INFO, "AMR_F", VcfFilter.Connector.LESS, "0.01"),
                new VcfFilter(VcfFilter.Field.INFO, "EA_F", VcfFilter.Connector.LESS, "0.01"),
                new VcfFilter(VcfFilter.Field.INFO, "ASN_F", VcfFilter.Connector.LESS, "0.01")};
        this.filters.getItems().addAll(0, Arrays.asList(filters));
    }

    private void addFilter() {
        this.filters.getItems().add(0, new VcfFilter());
    }

    private void applyFilters() {
        outputVariants.setAll(inputVariants.stream().filter(this::pass).collect(Collectors.toList()));
    }

    private boolean pass(Variant variant) {
        return filters.getItems().stream().allMatch(filter -> filter.pass(variant));
    }


}
