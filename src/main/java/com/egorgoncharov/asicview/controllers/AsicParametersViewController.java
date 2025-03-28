package com.egorgoncharov.asicview.controllers;

import com.egorgoncharov.asicview.appdata.AppDataGlobalManager;
import com.egorgoncharov.asicview.appdata.xml.Asic;
import com.egorgoncharov.asicview.appdata.xml.Layout;
import com.egorgoncharov.asicview.appdata.xml.ManualLayoutSetting;
import com.egorgoncharov.asicview.service.assets.FontsGlobalManager;
import com.egorgoncharov.asicview.service.fetching.DataFetchingService;
import com.egorgoncharov.asicview.service.fetching.Result;
import com.egorgoncharov.asicview.service.theme.ThemeGlobalManager;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class AsicParametersViewController {
    private static final Logger logger = LogManager.getLogger(AsicParametersViewController.class);
    private final List<ManualLayoutSetting> manualLayoutSettings = new ArrayList<>();
    @FXML
    private ScrollPane rootScrollPane;
    @FXML
    private Label modalTitle;
    @FXML
    private Label authDataSectionLabel;
    @FXML
    private Label colorSchemeSectionLabel;
    @FXML
    private Label viewDataSectionLabel;
    @FXML
    private Label additionalDataSectionLabel;
    @FXML
    private FlowPane viewSettingsContainer;
    @FXML
    private FlowPane manualLayoutSettingsContainer;
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
    private Button localViewMode;
    @FXML
    private Button globalViewMode;
    @FXML
    private Button manualViewMode;
    @FXML
    private Button saveButton;
    @FXML
    private Button cancelButton;
    private AnchorPane asicsContainer;
    private AppRootViewController appRootViewController;
    private Asic currentAsic;

    @FXML
    void cancel() {
        logger.info("Closing scene 'AsicParameterView'");
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    @FXML
    void save() {
        if (!nameField.getText().isEmpty() && !passwordField.getText().isEmpty()) {
            for (Asic asic : AppDataGlobalManager.getAppData().getAsics()) {
                if (asic.getPosition() == Integer.parseInt(positionField.getText()) - 1) {
                    asic.setPosition(currentAsic.getPosition());
                }
            }
            currentAsic.setUsername(nameField.getText());
            currentAsic.setPassword(passwordField.getText());
            currentAsic.setComment(commentField.getText());
            currentAsic.setPosition(positionField.getText().isEmpty() ? currentAsic.getPosition() : Integer.parseInt(positionField.getText()) - 1);
            int refreshInterval = refreshIntervalField.getText().isEmpty() ? currentAsic.getRefreshInterval() : Integer.parseInt(refreshIntervalField.getText());
            currentAsic.setRefreshInterval(Math.max(refreshInterval, 1000));
            DataFetchingService.updateWorkerRefreshInterval(currentAsic.getIp(), currentAsic.getRefreshInterval());
            for (Node manualLayoutSettingContainer : manualLayoutSettingsContainer.getChildren()) {
                FlowPane manualLayoutSettingContainerConverted = (FlowPane) manualLayoutSettingContainer;
                Label slotText = (Label) ((BorderPane) manualLayoutSettingContainerConverted.getChildren().get(0)).getChildren().get(0);
                TextField minText = (TextField) manualLayoutSettingContainerConverted.getChildren().get(1);
                TextField maxText = (TextField) manualLayoutSettingContainerConverted.getChildren().get(2);
                if (minText.getText().replaceAll("\\D+", "").isEmpty() || maxText.getText().replaceAll("\\D+", "").isEmpty()) {
                    continue;
                }
                int slot = Integer.parseInt(slotText.getText().replaceAll("\\D+", ""));
                int min = Integer.parseInt(minText.getText().replaceAll("\\D+", ""));
                int max = Integer.parseInt(maxText.getText().replaceAll("\\D+", ""));
                manualLayoutSettings.add(new ManualLayoutSetting(slot, min, max));
            }
            currentAsic.getManualLayoutSettings().clear();
            currentAsic.getManualLayoutSettings().addAll(manualLayoutSettings);
            asicsContainer.getChildren().clear();
            AppDataGlobalManager.getAppData().getAsics().forEach(asic -> appRootViewController.addAsic(asic.getIp(), asic.getComment(), asic.getPosition(), asic.isOpened()));
            appRootViewController.sortItems();
            appRootViewController.updateSortIcons();
            logger.info("Closing scene 'AsicParametersView'");
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
            if (position > maxPosition) {
                source.setText(String.valueOf(maxPosition));
            }
        }
    }

    @FXML
    void initialize() {
        rootScrollPane.getContent().setOnScroll(scrollEvent -> {
            double deltaY = scrollEvent.getDeltaY() * 0.01;
            rootScrollPane.setVvalue(rootScrollPane.getVvalue() - deltaY);
        });
        modalTitle.setFont(FontsGlobalManager.getFont("raleway", 24));
        authDataSectionLabel.setFont(FontsGlobalManager.getFont("inter", 10));
        colorSchemeSectionLabel.setFont(FontsGlobalManager.getFont("inter", 10));
        viewDataSectionLabel.setFont(FontsGlobalManager.getFont("inter", 10));
        additionalDataSectionLabel.setFont(FontsGlobalManager.getFont("inter", 10));
        nameField.setFont(FontsGlobalManager.getFont("inter", 13));
        passwordField.setFont(FontsGlobalManager.getFont("inter", 13));
        commentField.setFont(FontsGlobalManager.getFont("inter", 13));
        positionField.setFont(FontsGlobalManager.getFont("inter", 13));
        refreshIntervalField.setFont(FontsGlobalManager.getFont("inter", 13));
        localViewMode.setFont(FontsGlobalManager.getFont("inter", 13));
        globalViewMode.setFont(FontsGlobalManager.getFont("inter", 13));
        manualViewMode.setFont(FontsGlobalManager.getFont("inter", 13));
        saveButton.setFont(FontsGlobalManager.getFont("inter", 13));
        cancelButton.setFont(FontsGlobalManager.getFont("inter", 13));
    }

    public void setRequiredInfo(String asicIp, AnchorPane asicsContainer, AppRootViewController appRootViewController, Scene scene) {
        this.asicsContainer = asicsContainer;
        this.appRootViewController = appRootViewController;
        for (Asic asic : AppDataGlobalManager.getAppData().getAsics()) {
            if (asic.getIp().equalsIgnoreCase(asicIp)) {
                currentAsic = asic;
                break;
            }
        }
        nameField.setText(currentAsic.getUsername());
        passwordField.setText(currentAsic.getPassword());
        commentField.setText(currentAsic.getComment());
        positionField.setText(String.valueOf(currentAsic.getPosition() + 1));
        refreshIntervalField.setText(String.valueOf(currentAsic.getRefreshInterval()));
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
        if (currentAsic.getLayout() == Layout.LOCAL) {
            localViewMode.setStyle("-fx-background-color: " + ThemeGlobalManager.getTheme().rgbToHex(ThemeGlobalManager.getTheme().getMainColor()) + "; -fx-background-radius: 5;");
            globalViewMode.setStyle("-fx-background-color: " + ThemeGlobalManager.getTheme().rgbToHex(ThemeGlobalManager.getTheme().getSecondLayerColor()) + "; -fx-background-radius: 5;");
            manualViewMode.setStyle("-fx-background-color: " + ThemeGlobalManager.getTheme().rgbToHex(ThemeGlobalManager.getTheme().getSecondLayerColor()) + "; -fx-background-radius: 5;");
        } else if (currentAsic.getLayout() == Layout.GLOBAL) {
            globalViewMode.setStyle("-fx-background-color: " + ThemeGlobalManager.getTheme().rgbToHex(ThemeGlobalManager.getTheme().getMainColor()) + "; -fx-background-radius: 5;");
            localViewMode.setStyle("-fx-background-color: " + ThemeGlobalManager.getTheme().rgbToHex(ThemeGlobalManager.getTheme().getSecondLayerColor()) + "; -fx-background-radius: 5;");
            manualViewMode.setStyle("-fx-background-color: " + ThemeGlobalManager.getTheme().rgbToHex(ThemeGlobalManager.getTheme().getSecondLayerColor()) + "; -fx-background-radius: 5;");
        } else {
            manualViewMode.setStyle("-fx-background-color: " + ThemeGlobalManager.getTheme().rgbToHex(ThemeGlobalManager.getTheme().getMainColor()) + "; -fx-background-radius: 5;");
            localViewMode.setStyle("-fx-background-color: " + ThemeGlobalManager.getTheme().rgbToHex(ThemeGlobalManager.getTheme().getSecondLayerColor()) + "; -fx-background-radius: 5;");
            globalViewMode.setStyle("-fx-background-color: " + ThemeGlobalManager.getTheme().rgbToHex(ThemeGlobalManager.getTheme().getSecondLayerColor()) + "; -fx-background-radius: 5;");
        }
        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                save();
            } else if (event.getCode() == KeyCode.ESCAPE) {
                cancel();
            }
        });
        localViewMode.setOnMouseClicked((event) -> {
            localViewMode.setStyle("-fx-background-color: " + ThemeGlobalManager.getTheme().rgbToHex(ThemeGlobalManager.getTheme().getMainColor()) + "; -fx-background-radius: 5;");
            globalViewMode.setStyle("-fx-background-color: " + ThemeGlobalManager.getTheme().rgbToHex(ThemeGlobalManager.getTheme().getSecondLayerColor()) + "; -fx-background-radius: 5;");
            manualViewMode.setStyle("-fx-background-color: " + ThemeGlobalManager.getTheme().rgbToHex(ThemeGlobalManager.getTheme().getSecondLayerColor()) + "; -fx-background-radius: 5;");
            currentAsic.setLayout(Layout.LOCAL);
        });
        globalViewMode.setOnMouseClicked((event) -> {
            globalViewMode.setStyle("-fx-background-color: " + ThemeGlobalManager.getTheme().rgbToHex(ThemeGlobalManager.getTheme().getMainColor()) + "; -fx-background-radius: 5;");
            localViewMode.setStyle("-fx-background-color: " + ThemeGlobalManager.getTheme().rgbToHex(ThemeGlobalManager.getTheme().getSecondLayerColor()) + "; -fx-background-radius: 5;");
            manualViewMode.setStyle("-fx-background-color: " + ThemeGlobalManager.getTheme().rgbToHex(ThemeGlobalManager.getTheme().getSecondLayerColor()) + "; -fx-background-radius: 5;");
            currentAsic.setLayout(Layout.GLOBAL);
        });
        manualViewMode.setOnMouseClicked((event) -> {
            manualViewMode.setStyle("-fx-background-color: " + ThemeGlobalManager.getTheme().rgbToHex(ThemeGlobalManager.getTheme().getMainColor()) + "; -fx-background-radius: 5;");
            localViewMode.setStyle("-fx-background-color: " + ThemeGlobalManager.getTheme().rgbToHex(ThemeGlobalManager.getTheme().getSecondLayerColor()) + "; -fx-background-radius: 5;");
            globalViewMode.setStyle("-fx-background-color: " + ThemeGlobalManager.getTheme().rgbToHex(ThemeGlobalManager.getTheme().getSecondLayerColor()) + "; -fx-background-radius: 5;");
            currentAsic.setLayout(Layout.MANUAL);
        });
        loadSlotsSettings();
    }

    private void addManualLayoutSetting(ManualLayoutSetting manualLayoutSetting) {
        FlowPane manualLayoutSettingContainer = new FlowPane();
        manualLayoutSettingContainer.setPrefSize(390, 30);
        manualLayoutSettingContainer.setHgap(5);
        BorderPane slotNumberContainer = new BorderPane();
        slotNumberContainer.setPrefSize(100, 30);
        slotNumberContainer.setStyle("-fx-background-color: " + ThemeGlobalManager.getTheme().rgbToHex(ThemeGlobalManager.getTheme().getSecondLayerColor()) + "; -fx-background-radius: 5;");
        Label slotNumberLabel = new Label("Слот " + manualLayoutSetting.getSlot());
        slotNumberLabel.setTextFill(ThemeGlobalManager.getTheme().getFontColor());
        slotNumberLabel.setFont(FontsGlobalManager.getFont("inter", 13));
        slotNumberContainer.setCenter(slotNumberLabel);
        manualLayoutSettingContainer.getChildren().add(slotNumberContainer);
        TextField maxTextField = new TextField();
        TextField minTempField = new TextField();
        minTempField.setPromptText("Мин");
        if (manualLayoutSetting.getMinimalTemperature() > 0) {
            minTempField.setText(String.valueOf(manualLayoutSetting.getMinimalTemperature()));
        }
        minTempField.setPrefSize(140, 30);
        minTempField.setFont(FontsGlobalManager.getFont("inter", 13));
        minTempField.setAlignment(Pos.CENTER);
        minTempField.setStyle("-fx-background-color: " + ThemeGlobalManager.getTheme().rgbToHex(ThemeGlobalManager.getTheme().getSecondLayerColor()) + "; -fx-background-radius: 5; -fx-text-fill: " + ThemeGlobalManager.getTheme().rgbToHex(ThemeGlobalManager.getTheme().getFontColor()) + ";");
        minTempField.setOnKeyTyped((event) -> {
            if (!minTempField.getText().isEmpty()) {
                int value = minTempField.getText().replaceAll("\\D+", "").isEmpty() ? 0 : Integer.parseInt(minTempField.getText().replaceAll("\\D+", ""));
                if (minTempField.getText().replaceAll("\\D+", "").length() != minTempField.getText().length()) {
                    minTempField.setText(String.valueOf(value));
                }
                if (value < 1) {
                    minTempField.setText("1");
                }
            }
        });
        manualLayoutSettingContainer.getChildren().add(minTempField);
        maxTextField.setPromptText("Макс");
        if (manualLayoutSetting.getMaximalTemperature() > 0) {
            maxTextField.setText(String.valueOf(manualLayoutSetting.getMaximalTemperature()));
        }
        maxTextField.setPrefSize(140, 30);
        maxTextField.setFont(FontsGlobalManager.getFont("inter", 13));
        maxTextField.setAlignment(Pos.CENTER);
        maxTextField.setStyle("-fx-background-color: " + ThemeGlobalManager.getTheme().rgbToHex(ThemeGlobalManager.getTheme().getSecondLayerColor()) + "; -fx-background-radius: 5; -fx-text-fill: " + ThemeGlobalManager.getTheme().rgbToHex(ThemeGlobalManager.getTheme().getFontColor()) + ";");
        maxTextField.setOnKeyTyped((event) -> {
            if (!maxTextField.getText().isEmpty()) {
                int value = maxTextField.getText().replaceAll("\\D+", "").isEmpty() ? 0 : Integer.parseInt(maxTextField.getText().replaceAll("\\D+", ""));
                if (maxTextField.getText().replaceAll("\\D+", "").length() != maxTextField.getText().length()) {
                    maxTextField.setText(String.valueOf(value));
                }
                if (value < 1) {
                    maxTextField.setText("1");
                }

            }
        });
        manualLayoutSettingContainer.getChildren().add(maxTextField);
        manualLayoutSettingsContainer.getChildren().add(manualLayoutSettingContainer);
    }

    private void addViewMapSetting(int slotNumber, boolean viewAllowed) {
        FlowPane settingContainer = new FlowPane();
        HBox slotNumberContainer = new HBox();
        FlowPane.setMargin(slotNumberContainer, new Insets(0, 5, 0, 0));
        slotNumberContainer.setPrefSize(320, 30);
        slotNumberContainer.setAlignment(Pos.CENTER_LEFT);
        slotNumberContainer.setStyle("-fx-background-color: " + ThemeGlobalManager.getTheme().rgbToHex(ThemeGlobalManager.getTheme().getSecondLayerColor()) + "; -fx-background-radius: 5;");
        Label slotNumberLabel = new Label("Слот " + slotNumber);
        slotNumberLabel.setTextFill(ThemeGlobalManager.getTheme().getFontColor());
        slotNumberLabel.setFont(FontsGlobalManager.getFont("inter", 13));
        HBox.setMargin(slotNumberLabel, new Insets(0, 0, 0, 20));
        slotNumberContainer.getChildren().add(slotNumberLabel);
        settingContainer.getChildren().add(slotNumberContainer);
        Button checkboxButton = new Button(viewAllowed ? "Да" : "Нет");
        checkboxButton.setPrefSize(65, 30);
        checkboxButton.setStyle("-fx-background-color: " + (viewAllowed ? ThemeGlobalManager.getTheme().rgbToHex(ThemeGlobalManager.getTheme().getMainColor()) : ThemeGlobalManager.getTheme().rgbToHex(ThemeGlobalManager.getTheme().getSecondLayerColor())) + "; -fx-background-radius: 5;");
        checkboxButton.setTextFill(ThemeGlobalManager.getTheme().getFontColor());
        checkboxButton.setCursor(Cursor.HAND);
        checkboxButton.setOnMouseClicked((event) -> {
            if (checkboxButton.getText().equals("Да")) {
                checkboxButton.setText("Нет");
                checkboxButton.setStyle("-fx-background-color: " + ThemeGlobalManager.getTheme().rgbToHex(ThemeGlobalManager.getTheme().getSecondLayerColor()) + "; -fx-background-radius: 5;");
                if (currentAsic.getViewMap().containsKey(slotNumber)) {
                    currentAsic.getViewMap().replace(slotNumber, false);
                } else {
                    currentAsic.getViewMap().put(slotNumber, false);
                }
            } else {
                checkboxButton.setText("Да");
                checkboxButton.setStyle("-fx-background-color: " + ThemeGlobalManager.getTheme().rgbToHex(ThemeGlobalManager.getTheme().getMainColor()) + "; -fx-background-radius: 5;");
                if (currentAsic.getViewMap().containsKey(slotNumber)) {
                    currentAsic.getViewMap().replace(slotNumber, true);
                } else {
                    currentAsic.getViewMap().put(slotNumber, true);
                }
            }
        });
        settingContainer.getChildren().add(checkboxButton);
        viewSettingsContainer.getChildren().add(settingContainer);
    }

    private void loadSlotsSettings() {
        Result searchedResult = null;
        for (Result result : DataFetchingService.getFetchedAsics()) {
            if (result.getResponse().getIp().equals(currentAsic.getIp())) {
                searchedResult = result;
                break;
            }
        }
        if (searchedResult != null) {
            if (searchedResult.getResponse().isSuccessful()) {
                searchedResult.getAsic().getSlots().forEach(slot -> {
                    ManualLayoutSetting manualLayoutSetting = currentAsic.getManualLayoutSettings().stream().filter(e -> e.getSlot() == slot.getNumber()).findFirst().orElse(null);
                    addManualLayoutSetting(Objects.requireNonNullElseGet(manualLayoutSetting, () -> new ManualLayoutSetting(slot.getNumber(), 60, 120)));
                    boolean viewAllowed = true;
                    for (Map.Entry<Integer, Boolean> entry : currentAsic.getViewMap().entrySet()) {
                        if (entry.getKey() == slot.getNumber()) {
                            viewAllowed = entry.getValue();
                            break;
                        }
                    }
                    addViewMapSetting(slot.getNumber(), viewAllowed);
                });
            } else {
                currentAsic.getManualLayoutSettings().forEach(this::addManualLayoutSetting);
            }
        }
    }
}
