package coat.vcf.view;

import coat.graphic.ThresholdDialog;
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
import java.util.List;
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
    private final ChangeListener<Object> applyFilters = (observable, oldValue, newValue) -> applyFilters();

    private final ObservableList<Map<String, String>> infos = FXCollections.observableArrayList();
    private final ListChangeListener<Variant> variantsChangedListener = (ListChangeListener<Variant>) c -> applyFilters();
    private final static String[] FREQUENCY_IDS = {"AA_F", "EUR_F", "AFR_F", "AMR_F", "EA_F", "ASN_F",
            "afr_maf", "eur_maf", "amr_maf", "ea_maf", "asn_maf", "GMAF", "1KG14"};

    public FilterList() {
        Arrays.sort(FREQUENCY_IDS);
        configureButtons();
        configureFilters();
        this.getChildren().setAll(buttons, filters);
    }

    private void configureFilters() {
        filters.setEditable(true);
        filters.setCellFactory(param -> new FilterCell(infos));
        filters.getItems().addListener((ListChangeListener<VcfFilter>) change -> {
            change.next();
            if (change.wasAdded()) addFilters(change.getAddedSubList());
            else if (change.wasRemoved()) removeFilters(change.getRemoved());
        });
    }

    private void addFilters(List<? extends VcfFilter> addedFilters) {
        addedFilters.forEach(this::bindFilter);
        filters.getSelectionModel().select(0);
        filters.requestFocus();
        filters.scrollTo(0);
    }

    private void removeFilters(List<? extends VcfFilter> removedFilters) {
        removedFilters.forEach(this::unbindFilter);
        applyFilters();
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

    public void setInfos(ObservableList<Map<String, String>> infos) {
        this.infos.setAll(infos);
    }

    public ObservableList<Variant> getOutputVariants() {
        return outputVariants;
    }

    private void addFrequencyFilters() {
        String threshold = ThresholdDialog.askThresholdToUser();
        if (threshold != null) addFrequencyFilters(threshold);
    }

    private void addFrequencyFilters(String threshold) {
        final List<String> frequencyInfoIds = infos.stream().
                map(info -> info.get("ID")).
                filter(value -> Arrays.binarySearch(FREQUENCY_IDS, value) >= 0).
                collect(Collectors.toList());
        for (String id : frequencyInfoIds)
            filters.getItems().add(0, new VcfFilter(VcfFilter.Field.INFO, id, VcfFilter.Connector.LESS, threshold));
        applyFilters();
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
