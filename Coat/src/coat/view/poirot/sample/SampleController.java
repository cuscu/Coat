/*
 * Copyright (c) UICHUIMI 2016
 *
 * This file is part of Coat.
 *
 * Coat is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * Coat is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Foobar.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package coat.view.poirot.sample;/*
 * Copyright (c) UICHUIMI 2016
 *
 * This file is part of Coat.
 *
 * Coat is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * Coat is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Foobar.
 * If not, see <http://www.gnu.org/licenses/>.
 */

import javafx.beans.property.Property;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * Created by uichuimi on 16/05/16.
 */
public class SampleController {
    @FXML
    private Label father;
    @FXML
    private Label mother;
    @FXML
    private Label name;
    @FXML
    private ImageView icon;

    @FXML
    private void initialize() {
        icon.setPreserveRatio(true);
        icon.setSmooth(true);
        icon.setFitWidth(32);
    }

    public ImageView getIcon() {
        return icon;
    }

    void setIcon(IconType iconType) {
        icon.setImage(iconType.getIcon());
    }

    StringProperty nameProperty() {
        return name.textProperty();
    }

    StringProperty fatherProperty() {
        return father.textProperty();
    }

    StringProperty motherProperty() {
        return mother.textProperty();
    }

    Property<Image> iconProperty() {
        return icon.imageProperty();
    }


    enum IconType {
        MALE_UNAFFECTED {
            @Override
            Image getIcon() {
                return new Image("coat/img/black/male-unaffected.png");
            }
        }, MALE_AFFECTED {
            @Override
            Image getIcon() {
                return new Image("coat/img/black/male-affected.png");
            }
        }, FEMALE_AFFECTED {
            @Override
            Image getIcon() {
                return new Image("coat/img/black/female-affected.png");
            }
        }, FEMALE_UNAFFECTED {
            @Override
            Image getIcon() {
                return new Image("coat/img/black/female-unaffected.png");
            }
        };

        abstract Image getIcon();
    }

}
