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
package com.warxim.vucsa.client.gui.controller;

import javafx.application.Platform;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ScrollPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * Log tab controller.
 * <p>Displays logs from {@link Logger#getGlobal()}.</p>
 */
public final class LogController extends Handler implements Initializable {
    /**
     * Maximum number of Text elements in text flow.
     */
    private static final int LIMIT_LOG_CHILDREN = 150;

    private static final PseudoClass INFO_PSEUDO_CLASS = PseudoClass.getPseudoClass("info");
    private static final PseudoClass SEVERE_PSEUDO_CLASS = PseudoClass.getPseudoClass("severe");
    private static final PseudoClass WARNING_PSEUDO_CLASS = PseudoClass.getPseudoClass("warning");
    private static final PseudoClass FINE_PSEUDO_CLASS = PseudoClass.getPseudoClass("fine");
    private static final PseudoClass CONFIG_PSEUDO_CLASS = PseudoClass.getPseudoClass("config");
    private static final PseudoClass OTHER_PSEUDO_CLASS = PseudoClass.getPseudoClass("other");

    /**
     * Text flow for logs.
     */
    @FXML
    private TextFlow logFlow;
    /**
     * Scroll pane containing text flow.
     */
    @FXML
    private ScrollPane scrollPane;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Logger.getGlobal().addHandler(this);
    }

    @Override
    public void publish(LogRecord logRecord) {
        Platform.runLater(() -> this.addLogRecord(logRecord));
    }

    @Override
    public void flush() {
        // No action needed.
    }

    @Override
    public void close() {
        // No action needed.
    }

    /**
     * Adds log logRecord to the flow.
     */
    private void addLogRecord(LogRecord logRecord) {
        // Level.
        var level = logRecord.getLevel();

        Text levelText;
        if (level == Level.INFO) {
            levelText = new Text("[INFO] ");
            levelText.pseudoClassStateChanged(INFO_PSEUDO_CLASS, true);
        } else if (level == Level.SEVERE) {
            levelText = new Text("[SEVERE] ");
            levelText.pseudoClassStateChanged(SEVERE_PSEUDO_CLASS, true);
        } else if (level == Level.WARNING) {
            levelText = new Text("[WARNING] ");
            levelText.pseudoClassStateChanged(WARNING_PSEUDO_CLASS, true);
        } else if (level == Level.FINE) {
            levelText = new Text("[FINE] ");
            levelText.pseudoClassStateChanged(FINE_PSEUDO_CLASS, true);
        } else if (level == Level.CONFIG) {
            levelText = new Text("[CONFIG] ");
            levelText.pseudoClassStateChanged(CONFIG_PSEUDO_CLASS, true);
        } else {
            levelText = new Text("[OTHER] ");
            levelText.pseudoClassStateChanged(OTHER_PSEUDO_CLASS, true);
        }
        levelText.getStyleClass().add("log-logRecord");
        levelText.setFont(Font.font(null, FontWeight.BOLD, 11));

        // Message.
        var messageText = new Text(logRecord.getMessage());

        logFlow.getChildren().add(levelText);
        logFlow.getChildren().add(messageText);

        // Exception.
        if (logRecord.getThrown() != null) {
            var exception = new StringWriter();
            exception.append(System.lineSeparator());
            exception.append("\tException:");
            exception.append(System.lineSeparator());
            exception.append("\t");
            logRecord.getThrown().printStackTrace(new PrintWriter(exception));
            var exceptionText = new Text(exception.toString());
            logFlow.getChildren().add(exceptionText);
        }

        // Separator.
        var logSeparator = new Text(System.lineSeparator());
        logSeparator.setFont(Font.font(null, FontWeight.NORMAL, 1));
        logSeparator.setLineSpacing(0);
        logFlow.getChildren().add(logSeparator);

        // Limit children.
        var size = logFlow.getChildren().size();
        if (size > LIMIT_LOG_CHILDREN) {
            logFlow.getChildren().remove(0, size - LIMIT_LOG_CHILDREN);
        }

        // Scroll down automatically.
        scrollPane.layout();
        scrollPane.setVvalue(1D);
    }
}