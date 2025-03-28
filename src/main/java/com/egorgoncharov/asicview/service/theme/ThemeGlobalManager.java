package com.egorgoncharov.asicview.service.theme;

import com.egorgoncharov.asicview.appdata.AppDataGlobalManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ThemeGlobalManager {
    private static final Logger logger = LogManager.getLogger(ThemeGlobalManager.class);
    private static Theme theme;

    public static void initialize() {
        logger.info("Loading preferred theme");
        com.egorgoncharov.asicview.appdata.xml.Theme themeType = AppDataGlobalManager.getAppData().getTheme();
        if (themeType == com.egorgoncharov.asicview.appdata.xml.Theme.LIGHT) {
            theme = new LightTheme();
            logger.info("Theme 'LightTheme' loaded");
        } else if (themeType == com.egorgoncharov.asicview.appdata.xml.Theme.DARK) {
            theme = new DarkTheme();
            logger.info("Theme 'DarkTheme' loaded");
        }

    }

    public static Theme getTheme() {
        return theme;
    }
}
