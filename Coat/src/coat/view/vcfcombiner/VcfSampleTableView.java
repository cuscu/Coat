package coat.view.vcfcombiner;

import coat.utils.OS;
import coat.view.vcfreader.VcfSample;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.io.File;
import java.util.Arrays;

/**
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
class VcfSampleTableView extends TableView<VcfSample> {

    VcfSampleTableView(){
        final TableColumn<VcfSample, VcfSample.Level> levelColumn = new TableColumn<>(OS.getString("level"));
//        final TableColumn<Sample, String> nameColumn = new TableColumn<>(OS.getString("name"));
        final TableColumn<VcfSample, Long> numberOfVariantsColumn = new TableColumn<>(OS.getString("variants"));
        final TableColumn<VcfSample, Boolean> enableColumn = new TableColumn<>(OS.getString("name"));
        final TableColumn<VcfSample, File> bamFileColumn = new TableColumn<>(OS.getString("bam.file"));
        final TableColumn<VcfSample, File> mistFileColumn = new TableColumn<>(OS.getString("mist.file"));

        getColumns().addAll(Arrays.asList(enableColumn, numberOfVariantsColumn, levelColumn, bamFileColumn, mistFileColumn));
        setEditable(true);
        setId("sample-table");
        setColumnResizePolicy(CONSTRAINED_RESIZE_POLICY);

        enableColumn.setCellValueFactory(param -> param.getValue().enabledProperty());
//        enableColumn.setCellFactory(param -> new CheckBoxTableCell<>());
        enableColumn.setCellFactory(param -> new SampleCheckBoxTableCell());
        enableColumn.getStyleClass().add("text-column");
        enableColumn.setPrefWidth(200);

//        nameColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getFile().getName()));
//        nameColumn.getStyleClass().add("text-column");
//        nameColumn.setPrefWidth(150);

        numberOfVariantsColumn.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getNumberOfVariants()));
        numberOfVariantsColumn.getStyleClass().add("text-column");

        levelColumn.setCellValueFactory(param -> param.getValue().levelProperty());
        levelColumn.setCellFactory(param -> new LevelComboBoxCell());
        levelColumn.setPrefWidth(200);

        bamFileColumn.setCellValueFactory(param -> param.getValue().bamFileProperty());
        bamFileColumn.setCellFactory(param -> new BamTableCell());
        bamFileColumn.setPrefWidth(200);

        mistFileColumn.setCellValueFactory(param -> param.getValue().mistFileProperty());
        mistFileColumn.setCellFactory(param -> new MistTableCell());
        mistFileColumn.setPrefWidth(200);
    }

}
