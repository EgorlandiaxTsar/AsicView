package com.egorgoncharov.asicview.service.theme;

import javafx.scene.paint.Color;

public class DarkTheme implements Theme {
    @Override
    public String getFxmlFileModifier() {
        return "dark";
    }

    @Override
    public Color getFontColor() {
        return Color.rgb(255, 255, 255);
    }

    @Override
    public Color getBackgroundColor() {
        return Color.rgb(17, 17, 17);
    }

    @Override
    public Color getFirstLayerColor() {
        return Color.rgb(34, 34, 34);
    }

    @Override
    public Color getSecondLayerColor() {
        return Color.rgb(51, 51, 51);
    }

    @Override
    public Color getThirdLayerColor() {
        return Color.rgb(102, 102, 102);
    }

    @Override
    public Color getMainColor() {
        return Color.rgb(0, 97, 128);
    }

    @Override
    public Color getWarningColor() {
        return Color.rgb(128, 115, 0);
    }

    @Override
    public Color getErrorColor() {
        return Color.rgb(128, 0, 0);
    }

    @Override
    public int getLValue() {
        return 40;
    }
}
