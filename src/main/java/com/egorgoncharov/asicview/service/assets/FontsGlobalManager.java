package com.egorgoncharov.asicview.service.assets;

import javafx.scene.text.Font;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class FontsGlobalManager {
    private static final Logger logger = LogManager.getLogger(FontsGlobalManager.class);
    private static final Map<String, Font> loadedFonts = new HashMap<>();

    public static void initialize(Map<String, String> fonts, int[] sizes) {
        logger.info("Loading given fonts");
        fonts.forEach((name, path) -> Arrays.stream(sizes).forEach(size -> {
            loadedFonts.put(name + "_" + size, Font.loadFont(path, size));
            logger.info("Font '" + name + "', " + size + " loaded");
        }));
    }

    public static Font getFont(String name, int size) {
        logger.info("Searching for font '" + name + "', " + size);
        for (Map.Entry<String, Font> font : loadedFonts.entrySet()) {
            if ((font.getKey().equals(name + "_" + size) && font.getValue().getSize() == size)) {
                logger.info("Font found (target font: '" + name + "', " + size + ")");
                return font.getValue();
            }
        }
        logger.warn("Font not found (target font: '" + name + "', " + size + ")");
        return null;
    }
}
