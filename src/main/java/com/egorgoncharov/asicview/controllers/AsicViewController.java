package com.egorgoncharov.asicview.controllers;

import com.egorgoncharov.asicview.appdata.AppDataGlobalManager;
import com.egorgoncharov.asicview.appdata.xml.Asic;
import com.egorgoncharov.asicview.appdata.xml.Layout;
import com.egorgoncharov.asicview.appdata.xml.ManualLayoutSetting;
import com.egorgoncharov.asicview.appdata.xml.Theme;
import com.egorgoncharov.asicview.service.assets.FontsGlobalManager;
import com.egorgoncharov.asicview.service.comparators.ChipNameComparator;
import com.egorgoncharov.asicview.service.fetching.DataFetchingService;
import com.egorgoncharov.asicview.service.fetching.Result;
import com.egorgoncharov.asicview.service.fetching.data.AsicEntity;
import com.egorgoncharov.asicview.service.fetching.data.ChipEntity;
import com.egorgoncharov.asicview.service.fetching.data.SlotEntity;
import com.egorgoncharov.asicview.service.theme.ThemeGlobalManager;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.CacheHint;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.*;

public class AsicViewController {
    private static final Logger logger = LogManager.getLogger(AsicViewController.class);
    private final Font inter12 = FontsGlobalManager.getFont("inter", 12);
    private final Font inter14 = FontsGlobalManager.getFont("inter", 14);
    private final List<SlotUI> slots = new ArrayList<>();
    @FXML
    private Label ipLabel;
    @FXML
    private HBox slotsContainer;
    @FXML
    private ScrollPane slotsContainerScrollPane;
    private String asicIp;
    private AnchorPane asicsContainer;
    private AppRootViewController appRootViewController;
    private SortingMethods sortingMode = SortingMethods.getDefault();

    @FXML
    void openParametersModal() {
        logger.info("Loading scene 'AsicParametersView'");
        Stage stage = new Stage();
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/templates/asic-parameters-view_" + ThemeGlobalManager.getTheme().getFxmlFileModifier() + ".fxml"));
        logger.info("Theme changed to " + AppDataGlobalManager.getAppData().getTheme().toString().toLowerCase());
        Scene scene;
        try {
            scene = new Scene(fxmlLoader.load());
        } catch (IOException e) {
            logger.error("Failed to load scene 'AsicParametersView'", e);
            return;
        }
        fxmlLoader.<AsicParametersViewController>getController().setRequiredInfo(asicIp, asicsContainer, appRootViewController, scene);
        stage.setMinWidth(465);
        stage.setMinHeight(500);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setAlwaysOnTop(true);
        stage.setTitle("Параметры азика " + asicIp);
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    void changeTheme() {
        AppDataGlobalManager.getAppData().setTheme(AppDataGlobalManager.getAppData().getTheme() == Theme.LIGHT ? Theme.DARK : Theme.LIGHT);
        logger.info("Loading scene 'SimpleModalView'");
        Stage stage = new Stage();
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/templates/simple-modal-view_" + ThemeGlobalManager.getTheme().getFxmlFileModifier() + ".fxml"));
        logger.info("Theme changed to " + AppDataGlobalManager.getAppData().getTheme().toString().toLowerCase());
        Scene scene;
        try {
            scene = new Scene(fxmlLoader.load(), 400, 285);
        } catch (IOException e) {
            logger.error("Failed to load scene 'AsicParametersView'", e);
            return;
        }
        fxmlLoader.<SimpleModalViewController>getController().setRequiredInfo("Смена Темы", "Вы сменили тему на " + AppDataGlobalManager.getAppData().getTheme().toString().toLowerCase() + ". Что-бы применить тему перезагрузите приложение.", scene);
        stage.setResizable(false);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setAlwaysOnTop(true);
        stage.setTitle("Смена темы");
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    void initialize() {
        ipLabel.setFont(FontsGlobalManager.getFont("inter", 15));
    }

    public Timeline setRequiredInfo(String asicIp, AnchorPane asicsContainer, AppRootViewController appRootViewController) {
        this.asicIp = asicIp;
        this.asicsContainer = asicsContainer;
        this.appRootViewController = appRootViewController;
        ipLabel.setText(asicIp);
        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(1000), event -> executeTimelineHandler()));
        timeline.setOnFinished(event -> timeline.play());
        timeline.play();
        return timeline;
    }

    protected String getReadableElapsed(int elapsed) {
        long days = elapsed / (24 * 60 * 60);
        long hours = (elapsed % (24 * 60 * 60)) / (60 * 60);
        long minutes = (elapsed % (60 * 60)) / 60;
        return String.format("%dD %02d:%02d", days, hours, minutes);
    }

    protected Chip createChip(ChipEntity chip, double minTemp, double maxTemp) {
        Chip container = new Chip();
        container.setPrefSize(300, 25);
        container.setSpacing(5);
        BorderPane mainDataContainer = new BorderPane();
        mainDataContainer.setPrefHeight(25);
        mainDataContainer.setStyle("-fx-background-color: " + ThemeGlobalManager.getTheme().rgbToHex(ThemeGlobalManager.getTheme().getFirstLayerColor()) + "; -fx-background-radius: 5;");
        container.getChildren().add(mainDataContainer);
        BorderPane chipNameContainer = new BorderPane();
        chipNameContainer.setPrefSize(60, 25);
        chipNameContainer.setStyle("-fx-background-color: " + classifyColorByTemp(minTemp, maxTemp, chip.getTemperature()) + "; -fx-background-radius: 5;");
        chipNameContainer.setOnMouseClicked(event -> sortingMode = sortingMode != SortingMethods.NAME_UP ? SortingMethods.NAME_UP : SortingMethods.NAME_DOWN);
        chipNameContainer.setCursor(Cursor.HAND);
        mainDataContainer.setLeft(chipNameContainer);
        Label chipNameLabel = new Label(chip.getName());
        chipNameLabel.setTextFill(ThemeGlobalManager.getTheme().getFontColor());
        chipNameLabel.setFont(inter14);
        chipNameLabel.setCache(true);
        chipNameLabel.setCacheShape(true);
        chipNameLabel.setCacheHint(CacheHint.SPEED);
        chipNameContainer.setCenter(chipNameLabel);
        BorderPane chipTemperatureContainer = new BorderPane();
        chipTemperatureContainer.setPrefSize(60, 25);
        chipTemperatureContainer.setStyle("-fx-background-color: " + ThemeGlobalManager.getTheme().rgbToHex(ThemeGlobalManager.getTheme().getFirstLayerColor()) + "; -fx-background-radius: 5;");
        chipTemperatureContainer.setOnMouseClicked(event -> sortingMode = sortingMode != SortingMethods.TEMP_UP ? SortingMethods.TEMP_UP : SortingMethods.TEMP_DOWN);
        chipTemperatureContainer.setCursor(Cursor.HAND);
        mainDataContainer.setRight(chipTemperatureContainer);
        Label chipTemperatureLabel = new Label(String.valueOf(chip.getTemperature()));
        chipTemperatureLabel.setTextFill(ThemeGlobalManager.getTheme().getFontColor());
        chipTemperatureLabel.setFont(inter14);
        chipTemperatureLabel.setCache(true);
        chipTemperatureLabel.setCacheShape(true);
        chipTemperatureLabel.setCacheHint(CacheHint.SPEED);
        chipTemperatureContainer.setCenter(chipTemperatureLabel);
        BorderPane chipFrequencyContainer = new BorderPane();
        chipFrequencyContainer.setPrefSize(85, 25);
        chipFrequencyContainer.setStyle("-fx-background-color: " + ThemeGlobalManager.getTheme().rgbToHex(ThemeGlobalManager.getTheme().getFirstLayerColor()) + "; -fx-background-radius: 5;");
        chipFrequencyContainer.setOnMouseClicked(event -> sortingMode = sortingMode != SortingMethods.FREQ_UP ? SortingMethods.FREQ_UP : SortingMethods.FREQ_DOWN);
        chipFrequencyContainer.setCursor(Cursor.HAND);
        container.getChildren().add(chipFrequencyContainer);
        Label chipFrequencyLabel = new Label(String.valueOf(chip.getFrequency()));
        chipFrequencyLabel.setTextFill(ThemeGlobalManager.getTheme().getFontColor());
        chipFrequencyLabel.setFont(inter14);
        chipFrequencyLabel.setCache(true);
        chipFrequencyLabel.setCacheShape(true);
        chipFrequencyLabel.setCacheHint(CacheHint.SPEED);
        chipFrequencyContainer.setCenter(chipFrequencyLabel);
        BorderPane chipPctContainer = new BorderPane();
        chipPctContainer.setPrefSize(85, 25);
        chipPctContainer.setStyle("-fx-background-color: " + ThemeGlobalManager.getTheme().rgbToHex(ThemeGlobalManager.getTheme().getFirstLayerColor()) + "; -fx-background-radius: 5;");
        container.getChildren().add(chipPctContainer);
        Label chipPctLabel = new Label(chip.getPctUsed() + "/" + chip.getPctAvailable());
        chipPctLabel.setTextFill(ThemeGlobalManager.getTheme().getFontColor());
        chipPctLabel.setFont(inter12);
        chipPctLabel.setCache(true);
        chipPctLabel.setCacheShape(true);
        chipPctLabel.setCacheHint(CacheHint.SPEED);
        chipPctContainer.setCenter(chipPctLabel);
        container.setNameLabel(chipNameLabel);
        container.setFrequencyLabel(chipFrequencyLabel);
        container.setTemperatureLabel(chipTemperatureLabel);
        container.setPctLabel(chipPctLabel);
        return container;
    }

    protected double getMinimalChipTemperature(AsicEntity asic) {
        double min = Double.MAX_VALUE;
        for (SlotEntity slot : asic.getSlots()) {
            for (ChipEntity chip : slot.getChips()) {
                if (chip.getTemperature() < min) {
                    min = chip.getTemperature();
                }
            }
        }
        return min;
    }

    protected double getMinimalChipTemperature(SlotEntity slot) {
        double min = Double.MAX_VALUE;
        for (ChipEntity chip : slot.getChips()) {
            if (chip.getTemperature() < min) {
                min = chip.getTemperature();
            }
        }
        return min;
    }

    protected double getMaximalChipTemperature(AsicEntity asic) {
        double max = 0;
        for (SlotEntity slot : asic.getSlots()) {
            for (ChipEntity chip : slot.getChips()) {
                if (chip.getTemperature() > max) {
                    max = chip.getTemperature();
                }
            }
        }
        return max;
    }

    protected double getMaximalChipTemperature(SlotEntity slot) {
        double max = 0;
        for (ChipEntity chip : slot.getChips()) {
            if (chip.getTemperature() > max) {
                max = chip.getTemperature();
            }
        }
        return max;
    }

    private void executeTimelineHandler() {
        Platform.runLater(() -> {
            logger.info("Reloading slots");
            Asic asic = AppDataGlobalManager.getAppData().getAsics().stream().filter(e -> e.getIp().equals(asicIp)).findAny().orElse(null);
            Result result = DataFetchingService.getFetchedAsics().stream().filter(e -> e.getResponse().getIp().equals(asicIp)).findAny().orElse(null);
            if (asic == null) {
                logger.info("Closing scene 'AsicView' due to internal error (asic is null)");
                Stage stage = (Stage) asicsContainer.getScene().getWindow();
                stage.close();
            }
            reloadSlots(result, asic);
        });
    }

    private void reloadSlots(Result result, Asic asic) {
        if (result != null && result.getAsic() != null) {
            AsicEntity asicEntity = result.getAsic();
            for (SlotEntity e : asicEntity.getSlots()) {
                logger.info("Reloading slot " + e.getNumber());
                sortItems(e.getChips());
                boolean viewMapAccepted = true;
                for (Map.Entry<Integer, Boolean> entry : asic.getViewMap().entrySet()) {
                    if (e.getNumber() == entry.getKey()) {
                        viewMapAccepted = entry.getValue();
                        break;
                    }
                }
                if (asic.getLayout() == Layout.LOCAL) {
                    handleSlot(e, getMinimalChipTemperature(e), getMaximalChipTemperature(e), viewMapAccepted);
                } else if (asic.getLayout() == Layout.GLOBAL) {
                    handleSlot(e, getMinimalChipTemperature(asicEntity), getMaximalChipTemperature(asicEntity), viewMapAccepted);
                } else {
                    boolean foundSetting = false;
                    for (ManualLayoutSetting manualLayoutSetting : asic.getManualLayoutSettings()) {
                        if (manualLayoutSetting.getSlot() == e.getNumber()) {
                            handleSlot(e, manualLayoutSetting.getMinimalTemperature(), manualLayoutSetting.getMaximalTemperature(), viewMapAccepted);
                            foundSetting = true;
                            break;
                        }
                    }
                    if (!foundSetting) {
                        handleSlot(e, 80, 120, viewMapAccepted);
                    }
                }
            }
        }
        if (slotsContainer.getChildren().isEmpty()) {
            ipLabel.setText(asicIp + " (соеденение разорвано)");
        } else if (result != null) {
            if (result.getResponse().getRequestTime() > result.getResponse().getPreferredRequestTime()) {
                ipLabel.setText(asicIp + " (большое время запроса " + result.getResponse().getRequestTime() + "ms/" + result.getResponse().getPreferredRequestTime() + "ms)");
            } else {
                ipLabel.setText(asicIp);
            }
        } else {
            ipLabel.setText(asicIp);
        }
    }

    private void handleSlot(SlotEntity slot, double minTemp, double maxTemp, boolean displayable) {
        SlotUI relatedSlot = isExistingSlot(slot);
        if (relatedSlot != null) {
            if (!displayable) {
                for (SlotUI slotUI : slots) {
                    if (slot.getNumber() == slotUI.getData().getNumber()) {
                        slots.remove(slotUI);
                        break;
                    }
                }
                slotsContainer.getChildren().remove(relatedSlot.getSlotContainer());
                return;
            }
            relatedSlot.setData(slot, minTemp, maxTemp);
            return;
        }
        if (!displayable) {
            return;
        }
        BorderPane slotContainer = new BorderPane();
        slotContainer.setPrefWidth(300);
        BorderPane slotDataContainer = new BorderPane();
        slotDataContainer.setPrefWidth(300);
        BorderPane.setMargin(slotDataContainer, new Insets(0, 0, 10, 0));
        slotContainer.setTop(slotDataContainer);
        BorderPane slotNumberContainer = new BorderPane();
        slotNumberContainer.setPrefSize(300, 40);
        slotNumberContainer.setStyle("-fx-background-color: " + ThemeGlobalManager.getTheme().rgbToHex(ThemeGlobalManager.getTheme().getFirstLayerColor()) + "; -fx-background-radius: 8;");
        Label slotNumberLabel = new Label("Слот " + slot.getNumber());
        slotNumberLabel.setTextFill(ThemeGlobalManager.getTheme().getFontColor());
        slotNumberLabel.setFont(FontsGlobalManager.getFont("inter", 16));
        slotNumberLabel.setCache(true);
        slotNumberLabel.setCacheShape(true);
        slotNumberLabel.setCacheHint(CacheHint.SPEED);
        slotNumberContainer.setCenter(slotNumberLabel);
        slotDataContainer.setTop(slotNumberContainer);
        FlowPane additionalInfoContainer = new FlowPane();
        additionalInfoContainer.setPrefSize(300, 40);
        additionalInfoContainer.setHgap(10);
        BorderPane.setMargin(additionalInfoContainer, new Insets(10, 0, 10, 0));
        slotDataContainer.setCenter(additionalInfoContainer);
        additionalInfoContainer.getChildren().add(createAdditionalInfoContainer("Сред. Частота", String.valueOf(slot.getAverageFrequency())));
        additionalInfoContainer.getChildren().add(createAdditionalInfoContainer("Температура", slot.getTemperature() + "℃"));
        additionalInfoContainer.getChildren().add(createAdditionalInfoContainer("Время Работы", getReadableElapsed(slot.getElapsed())));
        FlowPane temperatureRangeContainer = new FlowPane();
        temperatureRangeContainer.setPrefSize(300, 40);
        temperatureRangeContainer.setHgap(10);
        slotDataContainer.setBottom(temperatureRangeContainer);
        temperatureRangeContainer.getChildren().add(createTemperatureRangeInfoContainer(true, getMinimalChipTemperature(slot)));
        temperatureRangeContainer.getChildren().add(createTemperatureRangeInfoContainer(false, getMaximalChipTemperature(slot)));
        VBox colorSettingContainer = new VBox();
        colorSettingContainer.setPrefWidth(300);
        BorderPane.setMargin(colorSettingContainer, new Insets(0, 0, 10, 0));
        colorSettingContainer.setAlignment(Pos.CENTER);
        slotContainer.setCenter(colorSettingContainer);
        ImageView colorPattern = new ImageView(getClass().getResource("/assets/" + ThemeGlobalManager.getTheme().getFxmlFileModifier() + "/color-bar.png").toExternalForm());
        colorPattern.setFitWidth(300);
        colorPattern.setFitHeight(10);
        colorSettingContainer.getChildren().add(colorPattern);
        HBox temperatureMarks = new HBox();
        temperatureMarks.setPrefWidth(300);
        temperatureMarks.setSpacing(56);
        colorSettingContainer.getChildren().add(temperatureMarks);
        double minHigh = minTemp + (maxTemp - minTemp) / 4;
        double mid = minHigh + (maxTemp - minTemp) / 4;
        double maxLow = mid + (maxTemp - minTemp) / 4;
        temperatureMarks.getChildren().add(createTemperatureMark(minTemp));
        temperatureMarks.getChildren().add(createTemperatureMark(minHigh));
        temperatureMarks.getChildren().add(createTemperatureMark(mid));
        temperatureMarks.getChildren().add(createTemperatureMark(maxLow));
        temperatureMarks.getChildren().add(createTemperatureMark(maxTemp));
        AnchorPane chipsContainer = new AnchorPane();
        chipsContainer.setPrefWidth(300);
        slotContainer.setBottom(chipsContainer);
        slots.add(new SlotUI(
                slot,
                minTemp,
                maxTemp,
                slotContainer,
                slotNumberLabel,
                extractAdditionalInfoValue(additionalInfoContainer, 0),
                extractAdditionalInfoValue(additionalInfoContainer, 1),
                extractAdditionalInfoValue(additionalInfoContainer, 2),
                extractTempRangeValue(temperatureRangeContainer, 0),
                extractTempRangeValue(temperatureRangeContainer, 1),
                temperatureMarks,
                chipsContainer,
                this
        ));
        int index = slot.getNumber() >= this.slots.size() || slot.getNumber() < 0 ? this.slots.size() - 1 : slot.getNumber();
        this.slotsContainer.getChildren().add(index, slotContainer);
    }

    private void sortItems(List<ChipEntity> chips) {
        logger.info("Sorting items (sorting mode: '" + sortingMode.toString().toLowerCase() + "')");
        switch (sortingMode) {
            case NAME_UP:
                chips.sort((v1, v2) -> new ChipNameComparator().compare(v1.getName(), v2.getName()));
                break;
            case NAME_DOWN:
                chips.sort((v1, v2) -> new ChipNameComparator().reversed().compare(v1.getName(), v2.getName()));
                break;
            case TEMP_UP:
                chips.sort(Comparator.comparingDouble(ChipEntity::getTemperature));
                break;
            case TEMP_DOWN:
                chips.sort((v1, v2) -> Double.compare(v2.getTemperature(), v1.getTemperature()));
                break;
            case FREQ_UP:
                chips.sort(Comparator.comparingDouble(ChipEntity::getFrequency));
                break;
            case FREQ_DOWN:
                chips.sort((v1, v2) -> Double.compare(v2.getFrequency(), v1.getFrequency()));
                break;
        }
    }

    private SlotUI isExistingSlot(SlotEntity slot) {
        for (SlotUI slotUI : slots) {
            if (slot.getNumber() == slotUI.getData().getNumber()) {
                return slotUI;
            }
        }
        return null;
    }

    private VBox createAdditionalInfoContainer(String additionalInfoTitle, String additionalInfoValue) {
        return createInfoContainer(additionalInfoTitle, additionalInfoValue, 92);
    }

    private VBox createTemperatureRangeInfoContainer(boolean isMin, double value) {
        return createInfoContainer(isMin ? "Мин." : "Макс.", value + "℃", 145);
    }

    private VBox createInfoContainer(String additionalInfoTitle, String additionalInfoValue, int objectWidth) {
        VBox container = new VBox();
        container.setPrefSize(objectWidth, 40);
        container.setAlignment(Pos.CENTER);
        container.setStyle("-fx-background-color: " + ThemeGlobalManager.getTheme().rgbToHex(ThemeGlobalManager.getTheme().getFirstLayerColor()) + "; -fx-background-radius: 8;");
        Label titleLabel = new Label(additionalInfoTitle);
        titleLabel.setTextFill(ThemeGlobalManager.getTheme().getFontColor());
        titleLabel.setFont(FontsGlobalManager.getFont("inter", 8));
        titleLabel.setCache(true);
        titleLabel.setCacheShape(true);
        titleLabel.setCacheHint(CacheHint.SPEED);
        container.getChildren().add(titleLabel);
        Label valueLabel = new Label(additionalInfoValue);
        valueLabel.setTextFill(ThemeGlobalManager.getTheme().getFontColor());
        valueLabel.setFont(inter14);
        valueLabel.setCache(true);
        valueLabel.setCacheShape(true);
        valueLabel.setCacheHint(CacheHint.SPEED);
        container.getChildren().add(valueLabel);
        return container;
    }

    private Label createTemperatureMark(double val) {
        Label label = new Label(String.valueOf(Math.round(val)));
        label.setTextFill(ThemeGlobalManager.getTheme().getFontColor());
        label.setWrapText(true);
        label.setFont(FontsGlobalManager.getFont("inter", 10));
        label.setCache(true);
        label.setCacheShape(true);
        label.setCacheHint(CacheHint.SPEED);
        return label;
    }

    private Label extractAdditionalInfoValue(FlowPane additionalInfoContainer, int i) {
        VBox dataBlock = (VBox) additionalInfoContainer.getChildren().get(i);
        return (Label) dataBlock.getChildren().get(1);
    }

    private Label extractTempRangeValue(FlowPane tempRangeContainer, int i) {
        VBox dataBlock = (VBox) tempRangeContainer.getChildren().get(i);
        return (Label) dataBlock.getChildren().get(1);
    }

    private String classifyColorByTemp(double minTemp, double maxTemp, double temp) {
        if (temp > maxTemp) {
            return hslToHex(0, 100, ThemeGlobalManager.getTheme().getLValue());
        }
        if (temp < minTemp) {
            return hslToHex(230, 100, ThemeGlobalManager.getTheme().getLValue());
        }
        return hslToHex((int) ((temp - maxTemp) * (230) / (minTemp - maxTemp)), 100, ThemeGlobalManager.getTheme().getLValue());
    }

    private String hslToHex(int h, int s, int l) {
        int[] rgb = hslToRgb(h, s, l, 1);
        return ThemeGlobalManager.getTheme().rgbToHex(Color.rgb(rgb[0], rgb[1], rgb[2]));
    }

    private int[] hslToRgb(float h, float s, float l, float alpha) {
        if (s < 0.0f || s > 100.0f) {
            return new int[]{0, 0, 0};
        }
        if (l < 0.0f || l > 100.0f) {
            return new int[]{0, 0, 0};
        }
        if (alpha < 0.0f || alpha > 1.0f) {
            return new int[]{0, 0, 0};
        }
        h = h % 360.0f;
        h /= 360f;
        s /= 100f;
        l /= 100f;
        float q;
        if (l < 0.5) q = l * (1 + s);
        else q = (l + s) - (s * l);
        float p = 2 * l - q;
        int r = Math.round(Math.max(0, hueToRgb(p, q, h + (1.0f / 3.0f)) * 256));
        int g = Math.round(Math.max(0, hueToRgb(p, q, h) * 256));
        int b = Math.round(Math.max(0, hueToRgb(p, q, h - (1.0f / 3.0f)) * 256));
        return new int[]{Math.min(r, 255), Math.min(g, 255), Math.min(b, 255)};
    }

    private float hueToRgb(float p, float q, float h) {
        if (h < 0) h += 1;
        if (h > 1) h -= 1;
        if (6 * h < 1) {
            return p + ((q - p) * 6 * h);
        }
        if (2 * h < 1) {
            return q;
        }
        if (3 * h < 2) {
            return p + ((q - p) * 6 * ((2.0f / 3.0f) - h));
        }
        return p;
    }
}

class SlotUI {
    private SlotEntity data;
    private double minTemp;
    private double maxTemp;
    private final BorderPane slotContainer;
    private final Label slotNameLabel;
    private final Label slotFrequencyLabel;
    private final Label slotTemperatureLabel;
    private final Label slotElapsedLabel;
    private final Label minimalChipTemperatureLabel;
    private final Label maximalChipTemperatureLabel;
    private final HBox temperatureMarksContainer;
    private final AnchorPane chipsContainer;
    private final AsicViewController asicViewController;

    public SlotUI(
            SlotEntity data,
            double minTemp,
            double maxTemp,
            BorderPane slotContainer,
            Label slotNameLabel,
            Label slotFrequencyLabel,
            Label slotTemperatureLabel,
            Label slotElapsedLabel,
            Label minimalChipTemperatureLabel,
            Label maximalChipTemperatureLabel,
            HBox temperatureMarksContainer,
            AnchorPane chipsContainer,
            AsicViewController asicViewController
    ) {
        this.data = data;
        this.minTemp = minTemp;
        this.maxTemp = maxTemp;
        this.slotContainer = slotContainer;
        this.slotNameLabel = slotNameLabel;
        this.slotFrequencyLabel = slotFrequencyLabel;
        this.slotTemperatureLabel = slotTemperatureLabel;
        this.slotElapsedLabel = slotElapsedLabel;
        this.minimalChipTemperatureLabel = minimalChipTemperatureLabel;
        this.maximalChipTemperatureLabel = maximalChipTemperatureLabel;
        this.temperatureMarksContainer = temperatureMarksContainer;
        this.chipsContainer = chipsContainer;
        this.asicViewController = asicViewController;
        setData(data, minTemp, maxTemp);
    }

    public SlotEntity getData() {
        return data;
    }

    public void setData(SlotEntity data) {
        setData(data, minTemp, maxTemp);
    }

    public void setData(SlotEntity data, double minTemp, double maxTemp) {
        this.data = data;
        this.minTemp = minTemp;
        this.maxTemp = maxTemp;
        slotNameLabel.setText("Слот " + data.getNumber());
        slotFrequencyLabel.setText(String.valueOf(data.getAverageFrequency()));
        slotTemperatureLabel.setText(data.getTemperature() + "℃");
        slotElapsedLabel.setText(asicViewController.getReadableElapsed(data.getElapsed()));
        minimalChipTemperatureLabel.setText(asicViewController.getMinimalChipTemperature(data) + "℃");
        maximalChipTemperatureLabel.setText(asicViewController.getMaximalChipTemperature(data) + "℃");
        double minHigh = minTemp + (maxTemp - minTemp) / 4;
        double mid = minHigh + (maxTemp - minTemp) / 4;
        double maxLow = mid + (maxTemp - minTemp) / 4;
        double[] temps = {minTemp, minHigh, mid, maxLow, maxTemp};
        Label[] tempMarksLabels = new Label[5];
        for (int i = 0; i < temperatureMarksContainer.getChildren().size(); i++) {
            tempMarksLabels[i] = (Label) temperatureMarksContainer.getChildren().get(i);
        }
        for (int i = 0; i < tempMarksLabels.length; i++) {
            tempMarksLabels[i].setText(String.valueOf(Math.round(temps[i])));
        }
        List<Chip> chipsToRemove = new ArrayList<>();
        List<Chip> chipsToAdd = new ArrayList<>();
        for (Node node : chipsContainer.getChildren()) {
            Chip chip = (Chip) node;
            ChipEntity relatedChipEntity = null;
            for (ChipEntity chipEntity2 : data.getChips()) {
                if (chipEntity2.getName().equals(chip.getNameLabel().getText())) {
                    relatedChipEntity = chipEntity2;
                    break;
                }
            }
            if (relatedChipEntity == null) {
                chipsToRemove.add(chip);
            }
        }
        List<ChipEntity> chips = data.getChips();
        for (int i = 0; i < chips.size(); i++) {
            ChipEntity chipEntity = chips.get(i);
            Chip relatedChip = null;
            for (Node node : chipsContainer.getChildren()) {
                Chip chip = (Chip) node;
                if (chip.getNameLabel().getText().equals(chipEntity.getName())) {
                    relatedChip = chip;
                    break;
                }
            }
            if (relatedChip == null) {
                Chip newChip = asicViewController.createChip(chipEntity, minTemp, maxTemp);
                newChip.setLayoutY(i * (25 + 5));
                chipsToAdd.add(newChip);
            } else {
                relatedChip.setLayoutY(i * (25 + 5));
                relatedChip.getNameLabel().setText(chipEntity.getName());
                relatedChip.getTemperatureLabel().setText(chipEntity.getTemperature() + "℃");
                relatedChip.getFrequencyLabel().setText(String.valueOf(chipEntity.getFrequency()));
                relatedChip.getPctLabel().setText(chipEntity.getPctUsed() + "/" + chipEntity.getPctAvailable());
            }
        }
        chipsContainer.getChildren().removeAll(chipsToRemove);
        chipsContainer.getChildren().addAll(chipsToAdd);
    }

    public BorderPane getSlotContainer() {
        return slotContainer;
    }

    public Label getSlotNameLabel() {
        return slotNameLabel;
    }

    public Label getSlotFrequencyLabel() {
        return slotFrequencyLabel;
    }

    public Label getSlotTemperatureLabel() {
        return slotTemperatureLabel;
    }

    public Label getSlotElapsedLabel() {
        return slotElapsedLabel;
    }

    public HBox getTemperatureMarksContainer() {
        return temperatureMarksContainer;
    }

    public AnchorPane getChipsContainer() {
        return chipsContainer;
    }

    public double getMinTemp() {
        return minTemp;
    }

    public double getMaxTemp() {
        return maxTemp;
    }

    public AsicViewController getAsicViewController() {
        return asicViewController;
    }

    @Override
    public int hashCode() {
        return Objects.hash(data);
    }
}

class Chip extends HBox {
    private Label nameLabel;
    private Label temperatureLabel;
    private Label frequencyLabel;
    private Label pctLabel;

    public Chip(Label nameLabel, Label temperatureLabel, Label frequencyLabel, Label pctLabel) {
        this.nameLabel = nameLabel;
        this.temperatureLabel = temperatureLabel;
        this.frequencyLabel = frequencyLabel;
        this.pctLabel = pctLabel;
    }

    public Chip() {
    }

    public Label getNameLabel() {
        return nameLabel;
    }

    public void setNameLabel(Label nameLabel) {
        this.nameLabel = nameLabel;
    }

    public Label getTemperatureLabel() {
        return temperatureLabel;
    }

    public void setTemperatureLabel(Label temperatureLabel) {
        this.temperatureLabel = temperatureLabel;
    }

    public Label getFrequencyLabel() {
        return frequencyLabel;
    }

    public void setFrequencyLabel(Label frequencyLabel) {
        this.frequencyLabel = frequencyLabel;
    }

    public Label getPctLabel() {
        return pctLabel;
    }

    public void setPctLabel(Label pctLabel) {
        this.pctLabel = pctLabel;
    }
}

enum SortingMethods {
    NAME_UP,
    NAME_DOWN,
    TEMP_UP,
    TEMP_DOWN,
    FREQ_UP,
    FREQ_DOWN;

    public static SortingMethods getDefault() {
        return NAME_UP;
    }
}
