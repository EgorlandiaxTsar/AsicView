package com.egorgoncharov.asicview.appdata.xml;

public enum Theme {
    LIGHT,
    DARK;

    public static Theme classifyTheme(String theme) {
        if (theme.equalsIgnoreCase("Light") || theme.equalsIgnoreCase("L")) {
            return LIGHT;
        } else if (theme.equalsIgnoreCase("Dark") || theme.equalsIgnoreCase("D")) {
            return DARK;
        } else {
            return null;
        }
    }
}
