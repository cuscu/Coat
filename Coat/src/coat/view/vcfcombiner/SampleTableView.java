package coat.view.vcfcombiner;

import coat.utils.OS;
import coat.view.vcfreader.Sample;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;

import java.io.File;
import java.util.Arrays;

/**
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class SampleTableView extends TableView<Sample> {

    SampleTableView(){
        final TableColumn<Sample, Sample.Level> levelColumn = new TableColumn<>(OS.getString("level"));
        final TableColumn<Sample, Long> numberOfVariantsColumn = new TableColumn<>(OS.getString("variants"));
        final TableColumn<Sample, String> nameColumn = new TableColumn<>(OS.getString("name"));
        final TableColumn<Sample, Boolean> enableColumn = new TableColumn<>();
        final TableColumn<Sample, File> bamFileColumn = new TableColumn<>(OS.getString("bam.file"));
        final TableColumn<Sample, File> mistFileColumn = new TableColumn<>(OS.getString("mist.file"));

        getColumns().addAll(Arrays.asList(enableColumn, nameColumn, numberOfVariantsColumn, levelColumn, bamFileColumn, mistFileColumn));
        setEditable(true);
        setId("sample-table");
        setColumnResizePolicy(CONSTRAINED_RESIZE_POLICY);

        enableColumn.setCellValueFactory(param -> param.getValue().getEnabledProperty());
        enableColumn.setCellFactory(param -> new CheckBoxTableCell<>());

        nameColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getFile().getName()));
        nameColumn.getStyleClass().add("text-column");
        nameColumn.setPrefWidth(150);

        numberOfVariantsColumn.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getNumberOfVariants()));
        numberOfVariantsColumn.getStyleClass().add("text-column");

        levelColumn.setCellValueFactory(param -> param.getValue().getLevelProperty());
        levelColumn.setCellFactory(param -> new LevelComboBoxCell());
        levelColumn.setPrefWidth(200);

        bamFileColumn.setCellValueFactory(param -> param.getValue().getBamFileProperty());
        bamFileColumn.setCellFactory(param -> new BamTableCell());
        bamFileColumn.setPrefWidth(200);

        mistFileColumn.setCellValueFactory(param -> param.getValue().getMistFileProperty());
        mistFileColumn.setCellFactory(param -> new MistTableCell());
        mistFileColumn.setPrefWidth(200);
    }

}
