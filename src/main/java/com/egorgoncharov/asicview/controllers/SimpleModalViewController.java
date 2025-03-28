package com.egorgoncharov.asicview.controllers;

import com.egorgoncharov.asicview.service.assets.FontsGlobalManager;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;

public class SimpleModalViewController {
    @FXML
    private Label modalTitle;
    @FXML
    private Label modalMessage;
    @FXML
    private Button okButton;

    @FXML
    void close() {
        Stage stage = (Stage) okButton.getScene().getWindow();
        stage.close();
    }

    @FXML
    void initialize() {
        modalTitle.setFont(FontsGlobalManager.getFont("raleway", 24));
        modalMessage.setFont(FontsGlobalManager.getFont("inter", 13));
        okButton.setFont(FontsGlobalManager.getFont("inter", 13));
    }

    public void setRequiredInfo(String modalTitle, String modalMessage, Scene scene) {
        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.ESCAPE) {
                close();
            }
        });
        this.modalTitle.setText(modalTitle);
        this.modalMessage.setText(modalMessage);
    }

    public void updateText(String modalTitle, String modalMessage) {
        this.modalTitle.setText(modalTitle);
        this.modalMessage.setText(modalMessage);
    }
}
