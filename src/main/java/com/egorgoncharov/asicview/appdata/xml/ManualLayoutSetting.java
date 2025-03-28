package com.egorgoncharov.asicview.appdata.xml;

public class ManualLayoutSetting {
    private final int slot;
    private int minimalTemperature;
    private int maximalTemperature;

    public ManualLayoutSetting(int slot, int minimalTemperature, int maximalTemperature) {
        this.slot = slot;
        this.minimalTemperature = minimalTemperature;
        this.maximalTemperature = maximalTemperature;
        checkValues();
    }

    public void setTemperatures(int minimalTemperature, int maximalTemperature) {
        this.minimalTemperature = minimalTemperature;
        this.maximalTemperature = maximalTemperature;
        checkValues();
    }

    public void resetTemperatures() {
        setTemperatures(-1, -1);
    }

    public int getMinimalTemperature() {
        return minimalTemperature;
    }

    public int getMaximalTemperature() {
        return maximalTemperature;
    }

    public int getSlot() {
        return slot;
    }

    private void checkValues() {
        if (minimalTemperature <= 0) {
            minimalTemperature = -1;
        }
        if (maximalTemperature <= 0) {
            maximalTemperature = -1;
        }
        if (minimalTemperature > maximalTemperature) {
            resetTemperatures();
        }
    }
}
