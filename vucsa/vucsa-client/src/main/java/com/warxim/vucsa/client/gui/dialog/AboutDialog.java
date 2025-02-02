/*
 * Vulnerable Client-Server Application (VuCSA)
 *
 * Copyright (C) 2021 Michal Válka
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If
 * not, see <https://www.gnu.org/licenses/>.
 */
package com.warxim.vucsa.client.gui.dialog;

import com.warxim.vucsa.client.gui.GuiBundle;
import com.warxim.vucsa.common.Constant;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * About dialog.
 */
public final class AboutDialog {
    private AboutDialog() {
    }

    /**
     * Shows about dialog.
     */
    public static void show() {
        var alert = new Alert(Alert.AlertType.INFORMATION);

        alert.setTitle("About VuCSA");
        alert.setHeaderText("VuCSA v" + Constant.VERSION);

        var icon = GuiBundle.getInstance().getLogo();

        var image = new ImageView(icon);
        image.setFitWidth(50);
        image.setFitHeight(50);
        alert.setGraphic(image);

        ((Stage) alert.getDialogPane().getScene().getWindow()).getIcons().add(icon);

        // Info
        var infoLabel = new Label("Vulnerable client-server application (VuCSA) is made for learning/presenting how to perform penetration tests of non-http thick clients.");

        // Version
        var versionLabel = new Label("Version:");
        versionLabel.getStyleClass().add("input-label");
        var versionValueLabel = new Label(Constant.VERSION);

        // Link
        var linkLabel = new Label("Website: ");
        linkLabel.getStyleClass().add("input-label");
        var link = new Hyperlink(Constant.WEB);
        link.setOnAction(AboutDialog::onWebClick);

        // Copyright
        var copyrightLabel = new Label("Copyright (C) Michal Valka, 2022, all rights reserved.");

        // Grid
        var grid = new GridPane();
        grid.setMaxWidth(Double.MAX_VALUE);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        // Info
        grid.add(infoLabel, 0, 0, 2, 1);

        // Version
        grid.add(versionLabel, 0, 1);
        grid.add(versionValueLabel, 1, 1);

        // Link
        grid.add(linkLabel, 0, 2);
        grid.add(link, 1, 2);

        // Copyright
        grid.add(copyrightLabel, 0, 3, 2, 1);

        alert.getDialogPane().setContent(grid);

        alert.showAndWait();
    }

    /**
     * Opens VuCSA website.
     */
    private static void onWebClick(ActionEvent event) {
        try {
            GuiBundle.getInstance().getHostServices().showDocument(Constant.WEB);
        } catch (Exception e) {
            Logger.getGlobal().log(Level.SEVERE, "Could not open VuCSA link.");
        }
    }
}
