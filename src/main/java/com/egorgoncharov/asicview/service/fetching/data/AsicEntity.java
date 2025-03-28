package com.egorgoncharov.asicview.service.fetching.data;

import java.util.ArrayList;
import java.util.List;

public class AsicEntity {
    private final List<SlotEntity> slots = new ArrayList<>();
    private String ip;

    public AsicEntity(String ip) {
        this.ip = ip;
    }

    public List<SlotEntity> getSlots() {
        return slots;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }
}
