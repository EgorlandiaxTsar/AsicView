package com.egorgoncharov.asicview.appdata;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class AppDataGlobalManager {
    private static AppDataFactory appDataFactory;
    private static AppData appData;
    private static boolean initialized = false;

    public static void initialize() {
        if (!initialized) initialized = true;
        File programDataDir = new File("");
        if (!programDataDir.exists()) {
            try {
                Files.createDirectories(Path.of(programDataDir.toString()));
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        }
        appDataFactory = new AppDataFactory("app-data.xml");
        loadAppData();
        appData.getAppInfo().setHomeDir("/resources/");
    }

    public static void loadAppData() {
        if (!initialized) return;
        appData = appDataFactory.getAppData();
    }

    public static void saveAppData() {
        if (!initialized) return;
        appDataFactory.saveAppData(appData);
    }

    public static AppData getAppData() {
        return appData;
    }

    public static boolean isInitialized() {
        return initialized;
    }
}
