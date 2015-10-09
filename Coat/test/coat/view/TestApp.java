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

import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;

/**
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class TestApp extends Application {

    private Stage stage;

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.stage = primaryStage;
        primaryStage.show();
        testOne();
    }

    public void setParent(Parent parent) {
        final Scene scene = new Scene(parent);
        scene.getStylesheets().add("default.css");
        stage.setScene(scene);
    }

    public void testOne() {
//        final Dataset dataset = VcfLoader.createDataset(new File("s002.vcf"));
//
//        final VariantsList variantsList = new VariantsList();
//        final VcfFiltersPane vcfFiltersPane = new VcfFiltersPane();
//        final InstanceProperties instanceProperties = new InstanceProperties();
//
//        vcfFiltersPane.setInputVariants(FXCollections.observableArrayList(dataset.getInstances()));
//        variantsList.setInputVariants(vcfFiltersPane.getOutputVariants());
//        variantsList.selectedVariantProperty().addListener((observable, oldValue, newValue) -> instanceProperties.setInstance(newValue));
//
//
//        final SplitPane splitPane0 = new SplitPane(variantsList, vcfFiltersPane);
//        splitPane0.setDividerPositions(0.7);
//        SplitPane.setResizableWithParent(vcfFiltersPane, false);
//        splitPane0.setOrientation(Orientation.VERTICAL);
//
//        final SplitPane splitPane = new SplitPane(splitPane0, instanceProperties);
//        splitPane.setDividerPositions(0.7);
//        SplitPane.setResizableWithParent(instanceProperties, false);

        final VariantsViewer variantsViewer = new VariantsViewer(new File("s002.vcf"));
        setParent(variantsViewer);
    }

}
