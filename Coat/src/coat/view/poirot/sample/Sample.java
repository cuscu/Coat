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

package coat.view.poirot.sample;

import coat.utils.OS;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import vcf.VcfFile;

import java.io.File;
import java.util.logging.Logger;

/**
 * Created by uichuimi on 16/05/16.
 */
class Sample {

    private final static Logger log = Logger.getLogger(Sample.class.getName());

    private final VcfFile vcfFile;
    private Property<String> name = new SimpleObjectProperty<>();
    private Property<Sample> father = new SimpleObjectProperty<>();
    private Property<Sample> mother = new SimpleObjectProperty<>();
    private Property<Sex> sex = new SimpleObjectProperty<>(Sex.FEMALE);
    private Property<Boolean> affected = new SimpleBooleanProperty(true);

    Sample(File file) {
        vcfFile = new VcfFile(file);
        name.setValue(vcfFile.getHeader().getSamples().get(0));
    }

    Property<Sample> motherProperty() {
        return mother;
    }

    Property<Sample> fatherProperty() {
        return father;
    }

    Property<String> nameProperty() {
        return name;
    }

    public String getName() {
        return name.getValue();
    }

    Sample getMother() {
        return mother.getValue();
    }

    Sample getFather() {
        return father.getValue();
    }

    Property<Sex> sexProperty() {
        return sex;
    }

    Sex getSex() {
        return sex.getValue();
    }

    Property<Boolean> affectedProperty() {
        return affected;
    }

    public boolean isAffected() {
        return affected.getValue();
    }


    enum Sex {
        MALE {
            @Override
            public String toString() {
                return OS.getString("male");
            }
        }, FEMALE {
            @Override
            public String toString() {
                return OS.getString("female");
            }
        }
    }
}
