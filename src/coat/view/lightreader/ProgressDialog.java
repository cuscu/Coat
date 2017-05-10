/*
 * Copyright (c) UICHUIMI 2017
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

package coat.view.lightreader;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;

/**
 * Created by uichuimi on 8/05/17.
 */
public class ProgressDialog extends BorderPane {
    private final ProgressBar progressBar = new ProgressBar();
    private final Label progressInfo = new Label();

    public ProgressDialog() {
        StackPane.setAlignment(progressInfo, Pos.CENTER);
        final StackPane stackPane = new StackPane(progressBar, progressInfo);
        setCenter(stackPane);
        final Label saving = new Label("Saving. Closing this window will"
                + " NOT interrupt the saving process");
        setTop(saving);
        progressBar.setMaxWidth(Double.MAX_VALUE);
    }

    public void update(long total, long read, long passed) {
        Platform.runLater(() -> {
            final double progress = (double) read / total;
            progressBar.setProgress(progress);
            progressInfo.setText(String.format("%d read, %d saved, %d total",
                    read, passed, total));
        });
    }
}
