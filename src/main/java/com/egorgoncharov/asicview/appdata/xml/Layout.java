package com.egorgoncharov.asicview.appdata.xml;

public enum Layout {
    GLOBAL,
    LOCAL,
    MANUAL;

    public static Layout classifyLayout(String layout) {
        if (layout.equalsIgnoreCase("Global") || layout.equalsIgnoreCase("G")) {
            return GLOBAL;
        } else if (layout.equalsIgnoreCase("Local") || layout.equalsIgnoreCase("L")) {
            return LOCAL;
        } else if (layout.equalsIgnoreCase("Manual") || layout.equalsIgnoreCase("M")) {
            return MANUAL;
        } else {
            return null;
        }
    }
}
