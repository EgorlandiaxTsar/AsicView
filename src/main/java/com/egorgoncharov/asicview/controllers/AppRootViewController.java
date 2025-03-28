package com.egorgoncharov.asicview.controllers;

import com.egorgoncharov.asicview.appdata.AppDataGlobalManager;
import com.egorgoncharov.asicview.appdata.xml.Asic;
import com.egorgoncharov.asicview.appdata.xml.SortingMethods;
import com.egorgoncharov.asicview.appdata.xml.Theme;
import com.egorgoncharov.asicview.service.assets.FontsGlobalManager;
import com.egorgoncharov.asicview.service.comparators.IpAddressComparator;
import com.egorgoncharov.asicview.service.fetching.DataFetchingService;
import com.egorgoncharov.asicview.service.fetching.Result;
import com.egorgoncharov.asicview.service.theme.ThemeGlobalManager;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.HostServices;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class AppRootViewController {
    private static final Logger logger = LogManager.getLogger(AppRootViewController.class);
    @FXML
    private AnchorPane asicsContainer;
    @FXML
    private ScrollPane asicContainerScrollPane;
    @FXML
    private HBox nameSort;
    @FXML
    private HBox commentSort;
    @FXML
    private Label userLabel;
    @FXML
    private Label myAsicsLabel;
    @FXML
    private Label nameSortLabel;
    @FXML
    private Label commentSortLabel;
    @FXML
    private Label instruments;
    @FXML
    private ImageView nameSortImage;
    @FXML
    private ImageView commentSortImage;
    private HostServices hostServices;
    private SortingMethods sortingMethod = SortingMethods.getDefault();

    @FXML
    void openAddAsicModal() {
        logger.info("Loading scene 'AddAsicView'");
        Stage stage = new Stage();
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/templates/add-asic-view_" + ThemeGlobalManager.getTheme().getFxmlFileModifier() + ".fxml"));
        Scene scene;
        try {
            scene = new Scene(fxmlLoader.load());
        } catch (IOException e) {
            logger.error("Failed to load scene 'AddAsicView'", e);
            return;
        }
        fxmlLoader.<AddAsicViewController>getController().setRequiredInfo(asicsContainer, this, scene);
        stage.setWidth(450);
        stage.setResizable(false);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setAlwaysOnTop(true);
        stage.setTitle("Добавление Азика");
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
            logger.error("Failed to load scene 'SimpleModalView'", e);
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
        asicContainerScrollPane.getContent().setOnScroll(scrollEvent -> {
            double deltaY = scrollEvent.getDeltaY() * 0.0025;
            asicContainerScrollPane.setVvalue(asicContainerScrollPane.getVvalue() - deltaY);
        });
        userLabel.setFont(FontsGlobalManager.getFont("inter", 15));
        myAsicsLabel.setFont(FontsGlobalManager.getFont("raleway", 24));
        nameSortLabel.setFont(FontsGlobalManager.getFont("inter", 16));
        commentSortLabel.setFont(FontsGlobalManager.getFont("inter", 16));
        instruments.setFont(FontsGlobalManager.getFont("inter", 16));
        userLabel.setText(System.getProperty("user.name") + "/" + AppDataGlobalManager.getAppData().getAppInfo().getName() + "-" + AppDataGlobalManager.getAppData().getAppInfo().getVersion());
        nameSort.setOnMouseClicked(event -> {
            sortingMethod = sortingMethod != SortingMethods.IP_UP ? SortingMethods.IP_UP : SortingMethods.IP_DOWN;
            sortItems();
            updateSortIcons();
            AppDataGlobalManager.getAppData().setSortingMode(sortingMethod);
        });
        commentSort.setOnMouseClicked(event -> {
            sortingMethod = sortingMethod != SortingMethods.COMMENT_UP ? SortingMethods.COMMENT_UP : SortingMethods.COMMENT_DOWN;
            sortItems();
            updateSortIcons();
            AppDataGlobalManager.getAppData().setSortingMode(sortingMethod);
        });
        AppDataGlobalManager.getAppData().getAsics().forEach(asic -> addAsic(asic.getIp(), asic.getComment(), asic.getPosition(), asic.isOpened()));
        asicContainerScrollPane.widthProperty().addListener(event -> {
            asicsContainer.getChildren().forEach(e -> {
                AsicBodyWrapper child = (AsicBodyWrapper) e;
                child.getAsicCommentWrapper().setPrefWidth(asicContainerScrollPane.getWidth() - 500 - 20);
            });
        });
        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(1000), event -> executeTimelineHandler()));
        timeline.setOnFinished(event -> timeline.play());
        timeline.play();
        sortingMethod = AppDataGlobalManager.getAppData().getSortingMode();
        sortItems();
        updateSortIcons();
    }

    public void setHostServices(HostServices hostServices) {
        this.hostServices = hostServices;
    }

    protected void sortItems() {
        sortItems(sortingMethod);
    }

    protected void sortItems(SortingMethods sortingMethod) {
        logger.info("Sorting items (sorting mode: '" + sortingMethod.toString().toLowerCase() + "')");
        if (sortingMethod == SortingMethods.NONE) return;
        switch (sortingMethod) {
            case IP_UP:
                AppDataGlobalManager.getAppData().getAsics().sort((v1, v2) -> new IpAddressComparator().compare(v1.getIp(), v2.getIp()));
                break;
            case IP_DOWN:
                AppDataGlobalManager.getAppData().getAsics().sort((v1, v2) -> new IpAddressComparator().reversed().compare(v2.getIp(), v1.getIp()));
                break;
            case COMMENT_UP:
                AppDataGlobalManager.getAppData().getAsics().sort(Comparator.comparing(Asic::getComment));
                break;
            case COMMENT_DOWN:
                AppDataGlobalManager.getAppData().getAsics().sort((v1, v2) -> v2.getComment().compareTo(v1.getComment()));
                break;
        }
        List<Asic> asics = AppDataGlobalManager.getAppData().getAsics();
        for (int i = 0; i < asics.size(); i++) {
            asics.get(i).setPosition(i);
        }
        asicsContainer.getChildren().clear();
        AppDataGlobalManager.getAppData().getAsics().forEach(asic -> addAsic(asic.getIp(), asic.getComment(), asic.getPosition(), asic.isOpened()));
    }

    protected void updateSortIcons() {
        logger.info("Updating sorting icons (sorting icons pattern: '" + sortingMethod.toString().toLowerCase() + "')");
        String ipSortIcon = "/assets/images/" + ThemeGlobalManager.getTheme().getFxmlFileModifier() + "/";
        String commentSortIcon = "/assets/images/" + ThemeGlobalManager.getTheme().getFxmlFileModifier() + "/";
        switch (sortingMethod) {
            case IP_UP:
                ipSortIcon += "arrow-up-narrow-wide.png";
                commentSortIcon += "arrow-up-down.png";
                break;
            case IP_DOWN:
                ipSortIcon += "arrow-down-wide-narrow.png";
                commentSortIcon += "arrow-up-down.png";
                break;
            case COMMENT_UP:
                ipSortIcon += "arrow-up-down.png";
                commentSortIcon += "arrow-up-narrow-wide.png";
                break;
            case COMMENT_DOWN:
                ipSortIcon += "arrow-up-down.png";
                commentSortIcon += "arrow-down-wide-narrow.png";
                break;
            default:
                ipSortIcon += "arrow-up-down.png";
                commentSortIcon += "arrow-up-down.png";
                break;
        }
        nameSortImage.setImage(new Image(getClass().getResource(ipSortIcon).toExternalForm()));
        commentSortImage.setImage(new Image(getClass().getResource(commentSortIcon).toExternalForm()));
    }

    @SuppressWarnings("CssUnknownTarget")
    protected void addAsic(String asicIp, String comment, int elementPosition, boolean isOpened) {
        if (comment == null) {
            comment = "Без комментария";
        }
        if (comment.isEmpty()) {
            comment = "Без комментария";
        }
        BorderPane asicCommentWrapper = new BorderPane();
        AsicBodyWrapper asicBodyWrapper = new AsicBodyWrapper(asicIp, isOpened, asicCommentWrapper);
        asicBodyWrapper.setPrefSize(Region.USE_COMPUTED_SIZE, 40);
        BorderPane asicBody = new BorderPane();
        asicBody.setPrefSize(Region.USE_COMPUTED_SIZE, 40);
        BorderPane asicIpWrapper = new BorderPane();
        asicIpWrapper.setPrefSize(250, 40);
        asicIpWrapper.setCursor(Cursor.HAND);
        asicIpWrapper.setStyle("-fx-background-color: " + ThemeGlobalManager.getTheme().rgbToHex(ThemeGlobalManager.getTheme().getFirstLayerColor()) + "; -fx-background-radius: 8px;");
        AtomicReference<Stage> oldStage = new AtomicReference<>();
        asicIpWrapper.setOnMouseClicked(event -> oldStage.set(openAsicView(asicIp, oldStage.get(), asicBodyWrapper)));
        BorderPane popUpContainer = new BorderPane();
        popUpContainer.setPrefWidth(300);
        popUpContainer.setBorder(new Border(new BorderStroke(ThemeGlobalManager.getTheme().getFontColor(), BorderStrokeStyle.SOLID, new CornerRadii(8), new BorderWidths(1))));
        popUpContainer.setLayoutY(elementPosition * 50 + 20);
        popUpContainer.setLayoutX(260);
        Label popUpLabel = new Label();
        popUpLabel.setTextFill(ThemeGlobalManager.getTheme().getFontColor());
        popUpLabel.setWrapText(true);
        popUpLabel.setPadding(new Insets(15, 5, 15, 5));
        popUpLabel.setFont(FontsGlobalManager.getFont("inter", 12));
        popUpLabel.setAlignment(Pos.CENTER);
        popUpContainer.setCenter(popUpLabel);
        asicIpWrapper.setOnMouseEntered(event -> {
            if (asicIpWrapper.getStyle().contains(ThemeGlobalManager.getTheme().rgbToHex(ThemeGlobalManager.getTheme().getErrorColor()))) {
                for (Result asic : DataFetchingService.getFetchedAsics()) {
                    if (asic == null) continue;
                    if (!asic.getResponse().getIp().equals(asicIp)) continue;
                    popUpContainer.setStyle("-fx-background-color: " + ThemeGlobalManager.getTheme().rgbToHex(ThemeGlobalManager.getTheme().getErrorColor()) + "; -fx-background-radius: 8;");
                    popUpLabel.setText("Азик недоступен. Информация об ошибке: " + asic.getResponse().getStatusCode() + ", " + asic.getResponse().getMessage());
                    break;
                }
                asicsContainer.getChildren().add(popUpContainer);
            } else if (asicIpWrapper.getStyle().contains(ThemeGlobalManager.getTheme().rgbToHex(ThemeGlobalManager.getTheme().getWarningColor()))) {
                for (Result asic : DataFetchingService.getFetchedAsics()) {
                    if (asic == null) continue;
                    if (!asic.getAsic().getIp().equals(asicIp)) continue;
                    popUpContainer.setStyle("-fx-background-color: " + ThemeGlobalManager.getTheme().rgbToHex(ThemeGlobalManager.getTheme().getWarningColor()) + "; -fx-background-radius: 8;");
                    popUpLabel.setText("Время запроса к азику превышает интервал обновления данных (" + asic.getResponse().getRequestTime() + "ms/" + asic.getResponse().getPreferredRequestTime() + "ms)");
                    break;
                }
                asicsContainer.getChildren().add(popUpContainer);
            }
        });
        asicIpWrapper.setOnMouseExited(event -> asicsContainer.getChildren().remove(popUpContainer));
        Label asicIpLabel = new Label(asicIp);
        asicIpLabel.setTextFill(ThemeGlobalManager.getTheme().getFontColor());
        asicIpLabel.setFont(FontsGlobalManager.getFont("inter", 13));
        asicIpLabel.setTextAlignment(TextAlignment.CENTER);
        asicIpWrapper.setCenter(asicIpLabel);
        asicBody.setLeft(asicIpWrapper);
        asicCommentWrapper.setPrefSize(asicContainerScrollPane.getWidth() - 500 - 20, 40);
        BorderPane.setMargin(asicCommentWrapper, new Insets(0, 10, 0, 10));
        asicCommentWrapper.setStyle("-fx-background-color: " + ThemeGlobalManager.getTheme().rgbToHex(ThemeGlobalManager.getTheme().getFirstLayerColor()) + "; -fx-background-radius: 8px;");
        asicCommentWrapper.setCursor(Cursor.HAND);
        Label commentLabel = new Label(comment);
        commentLabel.prefWidthProperty().bind(asicCommentWrapper.prefWidthProperty());
        commentLabel.setTextFill(ThemeGlobalManager.getTheme().getFontColor());
        commentLabel.setPrefHeight(40);
        commentLabel.setTextOverrun(OverrunStyle.ELLIPSIS);
        commentLabel.setPadding(new Insets(0, 0, 0, 20));
        commentLabel.setFont(FontsGlobalManager.getFont("inter", 13));
        commentLabel.setTextAlignment(TextAlignment.LEFT);
        asicCommentWrapper.setLeft(commentLabel);
        BorderPane commentPopUpContainer = new BorderPane();
        commentPopUpContainer.setPrefWidth(300);
        commentPopUpContainer.setBorder(new Border(new BorderStroke(ThemeGlobalManager.getTheme().getFontColor(), BorderStrokeStyle.SOLID, new CornerRadii(8), new BorderWidths(1))));
        commentPopUpContainer.setLayoutY(elementPosition * 50 + 20);
        commentPopUpContainer.setLayoutX(500);
        Label commentPopUpLabel = new Label();
        commentPopUpLabel.setTextFill(ThemeGlobalManager.getTheme().getFontColor());
        commentPopUpLabel.setWrapText(true);
        commentPopUpLabel.setPadding(new Insets(15, 5, 15, 5));
        commentPopUpLabel.setFont(FontsGlobalManager.getFont("inter", 12));
        commentPopUpLabel.setAlignment(Pos.CENTER);
        commentPopUpContainer.setCenter(commentPopUpLabel);
        AtomicBoolean isCommentPopupShown = new AtomicBoolean(false);
        asicCommentWrapper.setOnMouseClicked(event -> {
            if (!isCommentPopupShown.get()) {
                commentPopUpContainer.setLayoutX(event.getX() + 320);
                commentPopUpContainer.setStyle("-fx-background-color: " + ThemeGlobalManager.getTheme().rgbToHex(ThemeGlobalManager.getTheme().getSecondLayerColor()) + "; -fx-background-radius: 8;");
                commentPopUpLabel.setText(commentLabel.getText());
                asicsContainer.getChildren().add(commentPopUpContainer);
                isCommentPopupShown.set(true);
            }
        });
        asicCommentWrapper.setOnMouseExited(event -> {
            asicsContainer.getChildren().remove(commentPopUpContainer);
            isCommentPopupShown.set(false);
        });
        asicBody.setCenter(asicCommentWrapper);
        FlowPane toolsWrapper = new FlowPane();
        toolsWrapper.setStyle("-fx-background-color: " + ThemeGlobalManager.getTheme().rgbToHex(ThemeGlobalManager.getTheme().getFirstLayerColor()) + "; -fx-background-radius: 8px;");
        toolsWrapper.setAlignment(Pos.CENTER);
        toolsWrapper.setPrefSize(250, 40);
        Button[] tools = new Button[]{
                new Button(),
                new Button(),
                new Button(),
                new Button()
        };
        Arrays.stream(tools).forEach(tool -> {
            tool.setPrefSize(24, 24);
            tool.setCursor(Cursor.HAND);
            FlowPane.setMargin(tool, new Insets(0, 15, 0, 0));
        });
        tools[0].setStyle("-fx-background-color: " + ThemeGlobalManager.getTheme().rgbToHex(ThemeGlobalManager.getTheme().getFirstLayerColor()) + "; -fx-background-repeat: stretch; -fx-background-size: 24 24; -fx-background-position: center center; -fx-background-image: url('" + getClass().getResource("/assets/images/" + ThemeGlobalManager.getTheme().getFxmlFileModifier() + "/move-vertical.png").toExternalForm() + "');");
        tools[1].setStyle("-fx-background-color: " + ThemeGlobalManager.getTheme().rgbToHex(ThemeGlobalManager.getTheme().getFirstLayerColor()) + "; -fx-background-repeat: stretch; -fx-background-size: 24 24; -fx-background-position: center center; -fx-background-image: url('" + getClass().getResource("/assets/images/" + ThemeGlobalManager.getTheme().getFxmlFileModifier() + "/cog.png").toExternalForm() + "');");
        tools[2].setStyle("-fx-background-color: " + ThemeGlobalManager.getTheme().rgbToHex(ThemeGlobalManager.getTheme().getFirstLayerColor()) + "; -fx-background-repeat: stretch; -fx-background-size: 24 24; -fx-background-position: center center; -fx-background-image: url('" + getClass().getResource("/assets/images/" + ThemeGlobalManager.getTheme().getFxmlFileModifier() + "/external-link.png").toExternalForm() + "');");
        tools[3].setStyle("-fx-background-color: " + ThemeGlobalManager.getTheme().rgbToHex(ThemeGlobalManager.getTheme().getFirstLayerColor()) + "; -fx-background-repeat: stretch; -fx-background-size: 24 24; -fx-background-position: center center; -fx-background-image: url('" + getClass().getResource("/assets/images/" + ThemeGlobalManager.getTheme().getFxmlFileModifier() + "/trash-2.png").toExternalForm() + "');");
        tools[0].setOnMousePressed((event) -> {
            asicIpWrapper.setStyle("-fx-background-color: " + ThemeGlobalManager.getTheme().rgbToHex(ThemeGlobalManager.getTheme().getFontColor()) + "; -fx-background-radius: 8px;");
            asicIpLabel.setTextFill(ThemeGlobalManager.getTheme().getFirstLayerColor());
        });
        tools[0].setOnMouseReleased((event) -> {
            asicIpWrapper.setStyle("-fx-background-color: " + ThemeGlobalManager.getTheme().rgbToHex(ThemeGlobalManager.getTheme().getFirstLayerColor()) + "; -fx-background-radius: 8px;");
            asicIpLabel.setTextFill(ThemeGlobalManager.getTheme().getFontColor());
            sortingMethod = SortingMethods.NONE;
            AppDataGlobalManager.getAppData().setSortingMode(sortingMethod);
            updateSortIcons();

        });
        tools[0].setOnMouseDragged((event) -> {
            for (Node child : asicsContainer.getChildren()) {
                if (child.getLayoutY() >= (asicBodyWrapper.getLayoutY() + event.getY()) - 15 && child.getLayoutY() <= (asicBodyWrapper.getLayoutY() + event.getY()) + 15 && child != asicBodyWrapper) {
                    swapAsicsPosition(asicIp, (int) (child.getLayoutY() / 50), ((AsicBodyWrapper) child).getAsicIp(), (int) (asicBodyWrapper.getLayoutY() / 50));
                    double childNewLayoutY = asicBodyWrapper.getLayoutY();
                    asicBodyWrapper.setLayoutY(child.getLayoutY());
                    child.setLayoutY(childNewLayoutY);
                    break;
                }
            }
        });
        tools[1].setOnMouseClicked((event) -> {
            logger.info("Loading scene 'AsicParametersView'");
            Stage stage = new Stage();
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/templates/asic-parameters-view_" + ThemeGlobalManager.getTheme().getFxmlFileModifier() + ".fxml"));
            Scene scene;
            try {
                scene = new Scene(fxmlLoader.load());
            } catch (IOException e) {
                logger.error("Failed to load scene 'AsicParametersView'", e);
                return;
            }
            fxmlLoader.<AsicParametersViewController>getController().setRequiredInfo(asicIp, asicsContainer, this, scene);
            stage.setMinWidth(465);
            stage.setMinHeight(500);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setAlwaysOnTop(true);
            stage.setTitle("Параметры азика " + asicIp);
            stage.setScene(scene);
            stage.show();
        });
        tools[2].setOnMouseClicked((event) -> hostServices.showDocument("http://" + asicIp + "/cgi-bin/luci"));
        tools[3].setOnMouseClicked((event) -> {
            Asic asicToRemove = null;
            for (Asic asic : AppDataGlobalManager.getAppData().getAsics()) {
                if (asic.getIp().equals(asicIp)) {
                    asicToRemove = asic;
                }
            }
            for (Asic asic : AppDataGlobalManager.getAppData().getAsics()) {
                //noinspection DataFlowIssue
                if (asic.getPosition() > asicToRemove.getPosition()) {
                    asic.setPosition(asic.getPosition() - 1);
                }
            }
            AppDataGlobalManager.getAppData().getAsics().remove(asicToRemove);
            AppDataGlobalManager.saveAppData();
            asicsContainer.getChildren().clear();
            AppDataGlobalManager.getAppData().getAsics().forEach(asic -> addAsic(asic.getIp(), asic.getComment(), asic.getPosition(), asic.isOpened()));
        });
        FlowPane.setMargin(tools[3], new Insets(0, 0, 0, 0));
        Arrays.stream(tools).forEach(toolsWrapper.getChildren()::add);
        asicBody.setRight(toolsWrapper);
        asicBodyWrapper.setTop(asicBody);
        asicBodyWrapper.setLayoutY(elementPosition * 50);
        asicsContainer.getChildren().add(asicBodyWrapper);
    }

    protected void swapAsicsPosition(String asicIp1, int asicRequiredPosition1, String asicIp2, int requiredAsicPosition2) {
        AppDataGlobalManager.getAppData().getAsics().forEach(asic -> {
            if (asic.getIp().equalsIgnoreCase(asicIp1)) {
                asic.setPosition(asicRequiredPosition1);
            }
            if (asic.getIp().equalsIgnoreCase(asicIp2)) {
                asic.setPosition(requiredAsicPosition2);
            }
        });
    }

    private Stage openAsicView(String asicIp, Stage oldStage, AsicBodyWrapper asicBodyWrapper) {
        logger.info("Loading scene 'AsicView'");
        if (oldStage != null) oldStage.close();
        asicBodyWrapper.setOpened(true);
        Stage asicViewStage = new Stage();
        AppDataGlobalManager.getAppData().getAsics().stream().filter(asic -> asic.getIp().equals(asicIp)).findFirst().ifPresent(currentAsic -> currentAsic.setOpened(true));
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/templates/asic-view_" + ThemeGlobalManager.getTheme().getFxmlFileModifier() + ".fxml"));
        Scene scene;
        try {
            scene = new Scene(fxmlLoader.load());
        } catch (IOException e) {
            logger.error("Failed to load scene 'AsicView'", e);
            return null;
        }
        Timeline timeline = fxmlLoader.<AsicViewController>getController().setRequiredInfo(asicIp, asicsContainer, this);
        asicViewStage.setOnCloseRequest(e -> {
            asicBodyWrapper.setOpened(false);
            timeline.stop();
            AppDataGlobalManager.getAppData().getAsics().stream().filter(asic -> asic.getIp().equals(asicIp)).findFirst().ifPresent(currentAsic -> currentAsic.setOpened(false));
        });
        asicViewStage.setWidth(1280);
        asicViewStage.setTitle("Обзор азика " + asicIp);
        asicViewStage.setScene(scene);
        asicViewStage.show();
        asicViewStage.requestFocus();
        return asicViewStage;
    }

    private void executeTimelineHandler() {
        logger.info("Updating ASIC statuses");
        for (Result asic : DataFetchingService.getFetchedAsics()) {
            if (asic == null) continue;
            for (Node child : asicsContainer.getChildren()) {
                if (!(child instanceof AsicBodyWrapper)) continue;
                AsicBodyWrapper castedChild = (AsicBodyWrapper) child;
                if (asic.getResponse().getIp().equals(castedChild.getAsicIp())) {
                    BorderPane container = (BorderPane) castedChild.getChildren().get(0);
                    BorderPane asicIpContainer = (BorderPane) container.getChildren().get(0);
                    if (!asicIpContainer.getStyle().contains(ThemeGlobalManager.getTheme().rgbToHex(ThemeGlobalManager.getTheme().getFontColor()))) {
                        if (!asic.getResponse().isSuccessful()) {
                            asicIpContainer.setStyle("-fx-background-color: " + ThemeGlobalManager.getTheme().rgbToHex(ThemeGlobalManager.getTheme().getErrorColor()) + "; -fx-background-radius: 8;");
                        } else if (!asic.getResponse().isTimingSuccessful()) {
                            asicIpContainer.setStyle("-fx-background-color: " + ThemeGlobalManager.getTheme().rgbToHex(ThemeGlobalManager.getTheme().getWarningColor()) + "; -fx-background-radius: 8;");
                        } else if (castedChild.isOpened()) {
                            asicIpContainer.setStyle("-fx-background-color: " + ThemeGlobalManager.getTheme().rgbToHex(ThemeGlobalManager.getTheme().getMainColor()) + "; -fx-background-radius: 8;");
                        } else {
                            asicIpContainer.setStyle("-fx-background-color: " + ThemeGlobalManager.getTheme().rgbToHex(ThemeGlobalManager.getTheme().getFirstLayerColor()) + "; -fx-background-radius: 8;");
                        }
                    }
                }
            }
        }
    }
}

class AsicBodyWrapper extends BorderPane {
    private final String asicIp;
    private final BorderPane asicCommentWrapper;
    private boolean isOpened;

    public AsicBodyWrapper(String asicIp, boolean isOpened, BorderPane asicCommentWrapper) {
        this.asicIp = asicIp;
        this.isOpened = isOpened;
        this.asicCommentWrapper = asicCommentWrapper;
    }

    public String getAsicIp() {
        return asicIp;
    }

    public boolean isOpened() {
        return isOpened;
    }

    public void setOpened(boolean opened) {
        isOpened = opened;
    }

    public BorderPane getAsicCommentWrapper() {
        return asicCommentWrapper;
    }
}

