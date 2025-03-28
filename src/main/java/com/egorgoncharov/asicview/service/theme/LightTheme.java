package com.egorgoncharov.asicview.service.theme;

import javafx.scene.paint.Color;

public class LightTheme implements Theme {
    @Override
    public String getFxmlFileModifier() {
        return "light";
    }

    @Override
    public Color getFontColor() {
        return Color.rgb(0, 0, 0);
    }

    @Override
    public Color getBackgroundColor() {
        return Color.rgb(255, 255, 255);
    }

    @Override
    public Color getFirstLayerColor() {
        return Color.rgb(243, 243, 243);
    }

    @Override
    public Color getSecondLayerColor() {
        return Color.rgb(223, 223, 223);
    }

    @Override
    public Color getThirdLayerColor() {
        return Color.rgb(204, 204, 204);
    }

    @Override
    public Color getMainColor() {
        return Color.rgb(152, 230, 255);
    }

    @Override
    public Color getWarningColor() {
        return Color.rgb(255, 245, 152);
    }

    @Override
    public Color getErrorColor() {
        return Color.rgb(255, 152, 152);
    }

    @Override
    public int getLValue() {
        return 80;
    }
}
