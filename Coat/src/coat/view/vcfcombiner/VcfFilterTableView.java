package coat.view.vcfcombiner;

import coat.model.vcfreader.VcfFilter;
import coat.utils.OS;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

/**
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class VcfFilterTableView extends TableView<VcfFilter> {


    public VcfFilterTableView() {
        final TableColumn<VcfFilter, String> nameColumn = new TableColumn<>(OS.getString("name"));
        final TableColumn<VcfFilter, VcfFilter.Field> fieldTableColumn = new TableColumn<>(OS.getString("field"));
        final TableColumn<VcfFilter, String> infoTableColumn = new TableColumn<>(OS.getString("info"));
        final TableColumn<VcfFilter, VcfFilter.Connector> connectorTableColumn = new TableColumn<>(OS.getString("connector"));
        final TableColumn<VcfFilter, String> valueTableColumn = new TableColumn<>(OS.getString("value"));
        getColumns().addAll(nameColumn, fieldTableColumn, infoTableColumn, connectorTableColumn, valueTableColumn);

        nameColumn.setCellValueFactory(param -> param.getValue().getNameProperty());
        fieldTableColumn.setCellValueFactory(param -> param.getValue().getFieldProperty());
        infoTableColumn.setCellValueFactory(param -> param.getValue().getInfoProperty());
        connectorTableColumn.setCellValueFactory(param -> param.getValue().getConnectorProperty());
        valueTableColumn.setCellValueFactory(param -> param.getValue().getValueProperty());

    }
}
