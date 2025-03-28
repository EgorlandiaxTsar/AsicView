package com.egorgoncharov.asicview.service.fetching.data;

import java.util.ArrayList;
import java.util.List;

public class SlotEntity {
    private final List<ChipEntity> chips = new ArrayList<>();
    private String asicIp;
    private int number;
    private double averageFrequency;
    private double temperature;
    private int elapsed;

    public SlotEntity(String asicIp, int number, double averageFrequency, double temperature, int elapsed) {
        this.asicIp = asicIp;
        this.number = number;
        this.averageFrequency = averageFrequency;
        this.temperature = temperature;
        this.elapsed = elapsed;
    }

    public SlotEntity() {
    }

    public List<ChipEntity> getChips() {
        return chips;
    }

    public String getAsicIp() {
        return asicIp;
    }

    public void setAsicIp(String asicIp) {
        this.asicIp = asicIp;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public double getAverageFrequency() {
        return averageFrequency;
    }

    public void setAverageFrequency(double averageFrequency) {
        this.averageFrequency = averageFrequency;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public int getElapsed() {
        return elapsed;
    }

    public void setElapsed(int elapsed) {
        this.elapsed = elapsed;
    }
}
