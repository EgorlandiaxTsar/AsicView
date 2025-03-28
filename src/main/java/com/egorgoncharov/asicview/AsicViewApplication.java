package com.egorgoncharov.asicview;

import com.egorgoncharov.asicview.appdata.AppDataGlobalManager;
import com.egorgoncharov.asicview.controllers.AppRootViewController;
import com.egorgoncharov.asicview.service.assets.FontsGlobalManager;
import com.egorgoncharov.asicview.service.fetching.DataFetchingService;
import com.egorgoncharov.asicview.service.theme.ThemeGlobalManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class AsicViewApplication extends Application {
    private static Logger logger;

    static {
        int loggingMode = 0;
        try {
            loggingMode = Integer.parseInt(Files.readAllLines(Path.of("debug/logging-mode.txt")).get(0).replaceAll("\\D+", ""));
        } catch (IndexOutOfBoundsException | NumberFormatException e) {
            System.err.println("Application starting with services errors: debug service failed to set logging mode, using default logging mode (production/0)");
        } catch (IOException | NullPointerException e) {
            System.err.println("Application starting with services errors: debug service failed to load");
        }
        System.setProperty("log4j.configurationFile", loggingMode == 0 ? "debug/log4j2-config_production.xml" : "debug/log4j2-config_debug.xml");
        logger = LogManager.getLogger(AsicViewApplication.class);
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> logger.error("Uncaught exception in thread " + t.getName() + " has been thrown", e));
    }

    @Override
    public void start(Stage stage) throws IOException {
        logger.info("Loading service 'AppDataGlobalManager'");
        AppDataGlobalManager.initialize();
        logger.info("Loading service 'ThemeGlobalManager'");
        ThemeGlobalManager.initialize();
        logger.info("Loading service 'FontsGlobalManager'");
        Map<String, String> fonts = new HashMap<>() {{
            put("inter", getClass().getResource("/assets/fonts/Inter/Inter-Regular.ttf").toExternalForm());
            put("raleway", getClass().getResource("/assets/fonts/Raleway/Raleway-Bold.ttf").toExternalForm());
        }};
        FontsGlobalManager.initialize(fonts, new int[]{8, 10, 11, 12, 13, 14, 15, 16, 18, 24});
        logger.info("Loading service 'DataFetchingService'");
        DataFetchingService fetchingService = new DataFetchingService();
        fetchingService.setName("DataFetchingService");
        fetchingService.start();
        logger.info("Loading scene 'AppRootView'");
        stage.setOnCloseRequest((event) -> {
            logger.info("Application exit requested, terminating services");
            AppDataGlobalManager.saveAppData();
            DataFetchingService.shutdown();
            Platform.exit();
            logger.info("Application exited, terminating JVM");
            System.exit(0);
        });
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/templates/app-root-view_" + ThemeGlobalManager.getTheme().getFxmlFileModifier() + ".fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1280, 800);
        fxmlLoader.<AppRootViewController>getController().setHostServices(getHostServices());
        stage.setMinWidth(850);
        stage.setTitle("Просмотр Азиков");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        if (logger == null) logger = LogManager.getLogger(AsicViewApplication.class);
        logger.info("Application start requested");
        launch();
    }
}