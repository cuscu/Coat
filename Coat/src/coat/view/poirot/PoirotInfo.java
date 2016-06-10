/******************************************************************************
 * Copyright (C) 2015 UICHUIMI                                                *
 * *
 * This program is free software: you can redistribute it and/or modify it    *
 * under the terms of the GNU General Public License as published by the      *
 * Free Software Foundation, either version 3 of the License, or (at your     *
 * option) any later version.                                                 *
 * *
 * This program is distributed in the hope that it will be useful, but        *
 * WITHOUT ANY WARRANTY; without even the implied warranty of                 *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                       *
 * See the GNU General Public License for more details.                       *
 * *
 * You should have received a copy of the GNU General Public License          *
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.      *
 ******************************************************************************/

package coat.view.poirot;

import coat.core.poirot.hgnc.HGNC;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import poirot.core.Pearl;
import poirot.core.PearlRelationship;
import poirot.view.GraphNode;
import poirot.view.GraphRelationship;
import poirot.view.Selectable;
import vcf.Variant;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class PoirotInfo extends VBox {

    private final PoirotVariantTable poirotVariantTable = new PoirotVariantTable();
    private final PoirotRelationshipTable poirotRelationshipTable = new PoirotRelationshipTable();

    public PoirotInfo() {
        VBox.setVgrow(poirotVariantTable, Priority.ALWAYS);
        VBox.setVgrow(poirotRelationshipTable, Priority.ALWAYS);
    }

    public void setItem(Selectable item) {
        getChildren().clear();
        if (item == null) return;
        if (item.getClass().equals(GraphNode.class)) selectNode((GraphNode) item);
        else if (item.getClass().equals(GraphRelationship.class)) setRelationship((GraphRelationship) item);
    }

    private void selectNode(GraphNode graphNode) {
        if (graphNode.getPearl().getType() == Pearl.Type.GENE) showGeneDescription(graphNode.getPearl());
        else showFenotipeDescription(graphNode.getPearl());
    }

    private void showGeneDescription(Pearl pearl) {
        final String symbol = pearl.getId();
        String description = getDescription(symbol);
        if (pearl.getType() == Pearl.Type.GENE) {
            final Hyperlink hyperlink = getHyperlink(symbol + "(" + description + ")", "http://v4.genecards.org/cgi-bin/carddisp.pl?gene=" + symbol);
            getChildren().add(hyperlink);
        }
        final java.util.List<Variant> variants = (java.util.List<Variant>) pearl.getProperties().get("variants");
        if (variants != null) {
            poirotVariantTable.getItems().setAll(variants);
            getChildren().add(poirotVariantTable);
        }
//            for (Variant vcf : variants) infoBox.getChildren().add(new Label(simplified(vcf)));
    }

    private Hyperlink getHyperlink(String text, String url) {
        final Hyperlink hyperlink = new Hyperlink(text);
        hyperlink.setOnAction(event -> new Thread(() -> {
            try {
                Desktop.getDesktop().browse(new URI(url));
            } catch (IOException | URISyntaxException e1) {
                e1.printStackTrace();
            }
        }).start());
        return hyperlink;
    }

    private String getDescription(String symbol) {
        String description = HGNC.getName(symbol);
//        if (description == null) {
//            loadOmimDataset();
//            final java.util.List<Instance> instances = omimDataset.getInstances(symbol, 0);
//            if (!instances.isEmpty()) description = (String) instances.get(0).getField(1);
//        }
        return description;
    }

    private void showFenotipeDescription(Pearl pearl) {
        if (pearl.getId().startsWith("omim:")) {
            final String mimNumber = pearl.getId().split(":")[1];
            final Hyperlink hyperlink = getHyperlink(pearl.getId() + " " + pearl.getProperties().get("name"), "http://omim.org/entry/" + mimNumber);
            getChildren().add(hyperlink);
        } else {
            getChildren().add(new Label(pearl.getId()));
        }
        getChildren().add(new Label(pearl.getProperties().toString()));
    }

    public void setRelationship(GraphRelationship relationship) {
        getChildren().clear();
        if (relationship == null) return;
        final PearlRelationship firstPearlRelationship = relationship.getRelationships().get(0);
        getChildren().add(new Label(firstPearlRelationship.getSource().getId() + " <--> " + firstPearlRelationship.getTarget().getId()));
        getChildren().add(poirotRelationshipTable);
        poirotRelationshipTable.getItems().setAll(relationship.getRelationships());
//        relationship.getRelationships().forEach(pearlRelationship -> getChildren().add(new Label(pearlRelationship.getProperties().toString())));
    }

    public void clearView() {
        getChildren().clear();
    }
}
