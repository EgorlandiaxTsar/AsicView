package com.egorgoncharov.asicview.controllers;

import com.egorgoncharov.asicview.appdata.AppDataGlobalManager;
import com.egorgoncharov.asicview.appdata.xml.Asic;
import com.egorgoncharov.asicview.appdata.xml.Layout;
import com.egorgoncharov.asicview.service.assets.FontsGlobalManager;
import com.egorgoncharov.asicview.service.fetching.DataFetchingService;
import com.egorgoncharov.asicview.service.fetching.Result;
import com.egorgoncharov.asicview.service.theme.ThemeGlobalManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;

public class AddAsicViewController {
    private static final Logger logger = LogManager.getLogger(AddAsicViewController.class);
    @FXML
    private Label modalTitle;
    @FXML
    private Label asicIpSectionLabel;
    @FXML
    private Label authDataSectionLabel;
    @FXML
    private Label additionalDataSectionLabel;
    @FXML
    private FlowPane asicIpInputsContainer;
    @FXML
    private TextField asicIpInput1;
    @FXML
    private TextField asicIpInput2;
    @FXML
    private TextField asicIpInput3;
    @FXML
    private TextField asicIpInput4;
    @FXML
    private TextField nameField;
    @FXML
    private TextField passwordField;
    @FXML
    private TextField commentField;
    @FXML
    private TextField positionField;
    @FXML
    private TextField refreshIntervalField;
    @FXML
    private Button saveButton;
    @FXML
    private Button checkButton;
    @FXML
    private Button cancelButton;
    private AnchorPane asicsContainer;
    private AppRootViewController appRootViewController;

    @FXML
    void cancel() {
        logger.info("Closing scene 'AddAsicView'");
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    @FXML
    void check() {
        logger.info("Loading scene 'SimpleModalView'");
        String modalTitle = "Выполняется Подключение";
        String modalMessage = "Загрузка...";
        Stage stage = new Stage();
        logger.info("Loading scene 'SimpleModalView'");
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/templates/simple-modal-view_" + ThemeGlobalManager.getTheme().getFxmlFileModifier() + ".fxml"));
        Scene scene;
        try {
            scene = new Scene(fxmlLoader.load(), 400, 285);
        } catch (IOException e) {
            logger.error("Failed to load scene 'SimpleModalView'", e);
            return;
        }
        fxmlLoader.<SimpleModalViewController>getController().setRequiredInfo(modalTitle, modalMessage, scene);
        stage.setResizable(false);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setAlwaysOnTop(true);
        stage.setTitle(modalTitle);
        stage.setScene(scene);
        stage.show();
        if (!nameField.getText().isEmpty() && !passwordField.getText().isEmpty() && !asicIpInput1.getText().isEmpty() && !asicIpInput2.getText().isEmpty() && !asicIpInput3.getText().isEmpty() && !asicIpInput4.getText().isEmpty()) {
            Result response = DataFetchingService.instantFetch(
                    asicIpInput1.getText() + "." + asicIpInput2.getText() + "." + asicIpInput3.getText() + "." + asicIpInput4.getText(),
                    nameField.getText(),
                    passwordField.getText(),
                    5000
            );
            modalTitle = "Результат Подключения";
            modalMessage = response.getResponse().getMessage() + " - " + response.getResponse().getStatusCode();
        } else {
            modalTitle = "Недостаточно Данных";
            modalMessage = "Укажите имя пользователя, пароль, а так-же корректный IP-Адрес ASIC.";
        }
        stage.setTitle(modalTitle);
        fxmlLoader.<SimpleModalViewController>getController().updateText(modalTitle, modalMessage);
    }

    @FXML
    void save() {
        if (!nameField.getText().isEmpty() && !passwordField.getText().isEmpty() && !asicIpInput1.getText().isEmpty() && !asicIpInput2.getText().isEmpty() && !asicIpInput3.getText().isEmpty() && !asicIpInput4.getText().isEmpty()) {
            Asic newAsic = new Asic(
                    asicIpInput1.getText() + "." + asicIpInput2.getText() + "." + asicIpInput3.getText() + "." + asicIpInput4.getText(),
                    commentField.getText(),
                    positionField.getText().isEmpty() ? AppDataGlobalManager.getAppData().getAsics().size() : Integer.parseInt(positionField.getText()) - 1,
                    Math.max(refreshIntervalField.getText().isEmpty() ? 1000 : Integer.parseInt(refreshIntervalField.getText()), 1000),
                    Layout.LOCAL,
                    new ArrayList<>(),
                    nameField.getText(),
                    passwordField.getText(),
                    false

            );
            logger.info("Adding new ASIC (target IP: " + newAsic.getIp() + ")");
            boolean exists = false;
            for (Asic asic : AppDataGlobalManager.getAppData().getAsics()) {
                if (asic.getIp().equals(newAsic.getIp())) {
                    exists = true;
                    break;
                }
                if (asic.getPosition() == newAsic.getPosition()) {
                    asic.setPosition(newAsic.getPosition());
                }
            }
            if (exists) {
                logger.info("Loading scene 'SimpleModalView'");
                Stage stage = new Stage();
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/templates/simple-modal-view_" + ThemeGlobalManager.getTheme().getFxmlFileModifier() + ".fxml"));
                Scene scene;
                try {
                    scene = new Scene(fxmlLoader.load(), 400, 285);
                } catch (IOException e) {
                    logger.error("Failed to load scene 'SimpleModalView'", e);
                    return;
                }
                fxmlLoader.<SimpleModalViewController>getController().setRequiredInfo("Ошибка Добавления", "Указанный ASIC уже существует", scene);
                stage.setResizable(false);
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.setAlwaysOnTop(true);
                stage.setTitle("Ошибка добавления");
                stage.setScene(scene);
                stage.show();
                return;
            }
            AppDataGlobalManager.getAppData().getAsics().add(newAsic);
            AppDataGlobalManager.saveAppData();
            asicsContainer.getChildren().clear();
            AppDataGlobalManager.getAppData().getAsics().forEach(asic -> appRootViewController.addAsic(asic.getIp(), asic.getComment(), asic.getPosition(), asic.isOpened()));
            appRootViewController.sortItems();
            appRootViewController.updateSortIcons();
            logger.info("Closing scene 'AddAsicView'");
            Stage stage = (Stage) saveButton.getScene().getWindow();
            stage.close();
        }
    }

    @FXML
    void formatPosition(KeyEvent event) {
        TextField source = (TextField) event.getSource();
        if (!source.getText().isEmpty()) {
            int position = source.getText().replaceAll("\\D+", "").isEmpty() ? 0 : Integer.parseInt(source.getText().replaceAll("\\D+", ""));
            if (source.getText().replaceAll("\\D+", "").length() != source.getText().length()) {
                source.setText(String.valueOf(position));
            }
            if (position < 1) {
                source.setText("1");
            }
            int maxPosition = AppDataGlobalManager.getAppData().getAsics().size();
            if (position > maxPosition + 1) {
                source.setText(String.valueOf(maxPosition + 1));
            }
        }
    }

    @FXML
    void initialize() {
        modalTitle.setFont(FontsGlobalManager.getFont("raleway", 24));
        asicIpSectionLabel.setFont(FontsGlobalManager.getFont("inter", 10));
        authDataSectionLabel.setFont(FontsGlobalManager.getFont("inter", 10));
        additionalDataSectionLabel.setFont(FontsGlobalManager.getFont("inter", 10));
        asicIpInput1.setFont(FontsGlobalManager.getFont("inter", 13));
        asicIpInput2.setFont(FontsGlobalManager.getFont("inter", 13));
        asicIpInput3.setFont(FontsGlobalManager.getFont("inter", 13));
        asicIpInput4.setFont(FontsGlobalManager.getFont("inter", 13));
        nameField.setFont(FontsGlobalManager.getFont("inter", 13));
        passwordField.setFont(FontsGlobalManager.getFont("inter", 13));
        commentField.setFont(FontsGlobalManager.getFont("inter", 13));
        positionField.setFont(FontsGlobalManager.getFont("inter", 13));
        refreshIntervalField.setFont(FontsGlobalManager.getFont("inter", 13));
        saveButton.setFont(FontsGlobalManager.getFont("inter", 13));
        checkButton.setFont(FontsGlobalManager.getFont("inter", 13));
        cancelButton.setFont(FontsGlobalManager.getFont("inter", 13));
        asicIpInput1.setOnKeyReleased(event -> {
            if (asicIpInput1.getText().length() == 3 || event.getCode() == KeyCode.UP) {
                asicIpInput2.requestFocus();
            }
        });
        asicIpInput2.setOnKeyReleased(event -> {
            if (asicIpInput2.getText().length() == 3 || event.getCode() == KeyCode.UP) {
                asicIpInput3.requestFocus();
            }
            if (event.getCode() == KeyCode.DOWN) {
                asicIpInput1.requestFocus();
            }
        });
        asicIpInput3.setOnKeyReleased(event -> {
            if (asicIpInput3.getText().length() == 3 || event.getCode() == KeyCode.UP) {
                asicIpInput4.requestFocus();
            }
            if (event.getCode() == KeyCode.DOWN) {
                asicIpInput2.requestFocus();
            }
        });
        asicIpInput4.setOnKeyReleased(event -> {
            if (event.getCode() == KeyCode.DOWN) {
                asicIpInput3.requestFocus();
            }
        });
        asicIpInput1.setOnKeyTyped(event -> {
            if (!asicIpInput1.getText().isEmpty()) {
                int value = asicIpInput1.getText().replaceAll("\\D+", "").isEmpty() ? 0 : Integer.parseInt(asicIpInput1.getText().replaceAll("\\D+", ""));
                if (asicIpInput1.getText().replaceAll("\\D+", "").length() != asicIpInput1.getText().length()) {
                    asicIpInput1.setText(String.valueOf(value));
                }
            }
        });
        asicIpInput2.setOnKeyTyped(event -> {
            if (!asicIpInput2.getText().isEmpty()) {
                int value = asicIpInput2.getText().replaceAll("\\D+", "").isEmpty() ? 0 : Integer.parseInt(asicIpInput2.getText().replaceAll("\\D+", ""));
                if (asicIpInput2.getText().replaceAll("\\D+", "").length() != asicIpInput2.getText().length()) {
                    asicIpInput2.setText(String.valueOf(value));
                }
            }
        });
        asicIpInput3.setOnKeyTyped(event -> {
            if (!asicIpInput3.getText().isEmpty()) {
                int value = asicIpInput3.getText().replaceAll("\\D+", "").isEmpty() ? 0 : Integer.parseInt(asicIpInput3.getText().replaceAll("\\D+", ""));
                if (asicIpInput3.getText().replaceAll("\\D+", "").length() != asicIpInput3.getText().length()) {
                    asicIpInput3.setText(String.valueOf(value));
                }
            }
        });
        asicIpInput4.setOnKeyTyped(event -> {
            if (!asicIpInput4.getText().isEmpty()) {
                int value = asicIpInput4.getText().replaceAll("\\D+", "").isEmpty() ? 0 : Integer.parseInt(asicIpInput4.getText().replaceAll("\\D+", ""));
                if (asicIpInput4.getText().replaceAll("\\D+", "").length() != asicIpInput4.getText().length()) {
                    asicIpInput4.setText(String.valueOf(value));
                }
            }
        });
    }

    public void setRequiredInfo(AnchorPane asicsContainer, AppRootViewController appRootViewController, Scene scene) {
        this.asicsContainer = asicsContainer;
        this.appRootViewController = appRootViewController;
        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                save();
            } else if (event.getCode() == KeyCode.ESCAPE) {
                cancel();
            }
        });
        refreshIntervalField.setOnKeyTyped((event) -> {
            if (!refreshIntervalField.getText().isEmpty()) {
                int value = refreshIntervalField.getText().replaceAll("\\D+", "").isEmpty() ? 0 : Integer.parseInt(refreshIntervalField.getText().replaceAll("\\D+", ""));
                if (refreshIntervalField.getText().replaceAll("\\D+", "").length() != refreshIntervalField.getText().length()) {
                    refreshIntervalField.setText(String.valueOf(value));
                }
                if (value < 1) {
                    refreshIntervalField.setText("1000");
                }
            }
        });
    }
}
