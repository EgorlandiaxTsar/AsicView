package com.egorgoncharov.asicview.appdata.xml;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Asic {
    private final String ip;
    private final List<ManualLayoutSetting> manualLayoutSettings;
    private String comment;
    private int position;
    private int refreshInterval;
    private Layout layout;
    private String username;
    private String password;
    private Map<Integer, Boolean> viewMap = new HashMap<>();
    private boolean opened;

    public Asic(String ip, String comment, int position, int refreshInterval, Layout layout, List<ManualLayoutSetting> manualLayoutSettings, String username, String password, boolean opened) {
        this.ip = ip;
        this.manualLayoutSettings = manualLayoutSettings;
        this.comment = comment;
        this.position = position;
        this.refreshInterval = refreshInterval;
        this.layout = layout;
        this.username = username;
        this.password = password;
        this.opened = opened;
    }

    public String getIp() {
        return ip;
    }

    public List<ManualLayoutSetting> getManualLayoutSettings() {
        return manualLayoutSettings;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public int getRefreshInterval() {
        return refreshInterval;
    }

    public void setRefreshInterval(int refreshInterval) {
        this.refreshInterval = refreshInterval;
    }

    public Layout getLayout() {
        return layout;
    }

    public void setLayout(Layout layout) {
        this.layout = layout;
    }

    public Map<Integer, Boolean> getViewMap() {
        return viewMap;
    }

    public void setViewMap(Map<Integer, Boolean> viewMap) {
        this.viewMap = viewMap;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isOpened() {
        return opened;
    }

    public void setOpened(boolean opened) {
        this.opened = opened;
    }
}
