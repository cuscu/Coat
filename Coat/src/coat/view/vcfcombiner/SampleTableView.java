package coat.view.vcfcombiner;

import coat.utils.OS;
import coat.view.vcfreader.Sample;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;

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
        getColumns().addAll(Arrays.asList(enableColumn, nameColumn, numberOfVariantsColumn, levelColumn));
        setEditable(true);
        setId("sample-table");
        enableColumn.setCellValueFactory(param -> param.getValue().getEnabledProperty());
        enableColumn.setCellFactory(param -> new CheckBoxTableCell<>());

        nameColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getFile().getName()));
        nameColumn.setPrefWidth(150);
        nameColumn.getStyleClass().add("text-column");

        numberOfVariantsColumn.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getNumberOfVariants()));
        numberOfVariantsColumn.getStyleClass().add("text-column");

        levelColumn.setCellValueFactory(param -> param.getValue().getLevelProperty());
        levelColumn.setCellFactory(param -> new LevelCell());
        levelColumn.setPrefWidth(200);
    }

    public void addFilterColumn() {
        final int index = getColumns().size() - 4;
        TableColumn<Sample, Boolean> filterColumn = new TableColumn<>("F" + index);
        getColumns().add(filterColumn);
        filterColumn.setCellValueFactory(param -> param.getValue().getFilterStatus(index));
        filterColumn.setCellFactory(param -> new CheckBoxTableCell<>());

    }
}
