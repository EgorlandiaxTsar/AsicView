package com.egorgoncharov.asicview.service.fetching.data;

public class ChipEntity {
    private String asicIp;
    private int slotNumber;
    private String name;
    private double frequency;
    private double temperature;
    private String pctUsed;
    private String pctAvailable;

    public ChipEntity(String asicIp, int slotNumber, String name, double frequency, double temperature, String pctUsed, String pctAvailable) {
        this.asicIp = asicIp;
        this.slotNumber = slotNumber;
        this.name = name;
        this.frequency = frequency;
        this.temperature = temperature;
        this.pctUsed = pctUsed;
        this.pctAvailable = pctAvailable;
    }

    public ChipEntity() {
    }

    public String getAsicIp() {
        return asicIp;
    }

    public void setAsicIp(String asicIp) {
        this.asicIp = asicIp;
    }

    public int getSlotNumber() {
        return slotNumber;
    }

    public void setSlotNumber(int slotNumber) {
        this.slotNumber = slotNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getFrequency() {
        return frequency;
    }

    public void setFrequency(double frequency) {
        this.frequency = frequency;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public String getPctUsed() {
        return pctUsed;
    }

    public void setPctUsed(String pctUsed) {
        this.pctUsed = pctUsed;
    }

    public String getPctAvailable() {
        return pctAvailable;
    }

    public void setPctAvailable(String pctAvailable) {
        this.pctAvailable = pctAvailable;
    }
}
