/******************************************************************************
 * Copyright (C) 2015 UICHUIMI                                                *
 *                                                                            *
 * This program is free software: you can redistribute it and/or modify it    *
 * under the terms of the GNU General Public License as published by the      *
 * Free Software Foundation, either version 3 of the License, or (at your     *
 * option) any later version.                                                 *
 *                                                                            *
 * This program is distributed in the hope that it will be useful, but        *
 * WITHOUT ANY WARRANTY; without even the implied warranty of                 *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                       *
 * See the GNU General Public License for more details.                       *
 *                                                                            *
 * You should have received a copy of the GNU General Public License          *
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.      *
 ******************************************************************************/

package coat.view;

import coat.core.poirot.dataset.Dataset;
import coat.core.poirot.dataset.VcfLoader;
import coat.core.reader.Reader;
import coat.core.vcfreader.VcfSaver;
import coat.utils.FileManager;
import coat.view.vcfreader.VariantsList;
import coat.view.vcfreader.VcfFiltersPane;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Orientation;
import javafx.scene.control.Button;
import javafx.scene.control.SplitPane;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.List;

/**
 * Shows a Vcf file.
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class VariantsViewer extends SplitPane implements Reader {

    private Property<String> title = new SimpleObjectProperty<>("Variants Reader");
    final Dataset dataset;
    final VariantsList variantsList = new VariantsList();
    final VcfFiltersPane vcfFiltersPane = new VcfFiltersPane();
    final InstanceProperties instanceProperties = new InstanceProperties();
    private final File file;

    public VariantsViewer(File file) {
        this.file = file;
        dataset = VcfLoader.createDataset(file);
        vcfFiltersPane.setInputVariants(FXCollections.observableArrayList(dataset.getInstances()));
        variantsList.setInputVariants(vcfFiltersPane.getOutputVariants());
        variantsList.selectedVariantProperty().addListener((observable, oldValue, newValue) -> instanceProperties.setInstance(newValue));

        final SplitPane splitPane0 = new SplitPane(variantsList, vcfFiltersPane);
        splitPane0.setDividerPositions(0.7);
        SplitPane.setResizableWithParent(vcfFiltersPane, false);
        splitPane0.setOrientation(Orientation.VERTICAL);
    this.getItems().addAll(splitPane0, instanceProperties);
        this.setDividerPositions(0.7);
        SplitPane.setResizableWithParent(instanceProperties, false);
    }

    @Override
    public Property<String> getTitle() {
        return title;
    }

    @Override
    public void saveAs() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(file.getParentFile());
        fileChooser.setTitle("Save variants");
        fileChooser.setInitialFileName(file.getName());
        fileChooser.getExtensionFilters().addAll(FileManager.VCF_FILTER, FileManager.TSV_FILTER);
        final File f = fileChooser.showSaveDialog(null);
        if (f != null){
            selectSaveMethod(f, fileChooser.getSelectedExtensionFilter());
        }
    }

    private void selectSaveMethod(File f, FileChooser.ExtensionFilter extension) {
        if (extension == FileManager.TSV_FILTER) VcfSaver.saveToTSV(dataset, variantsList.getVariants(), f);
        else if (extension == FileManager.VCF_FILTER) VcfSaver.saveToVcf(dataset, variantsList.getVariants(), f, file);
    }

    @Override
    public List<Button> getActions() {
        return null;
    }

    @Override
    public String getActionsName() {
        return null;
    }
}
