package com.egorgoncharov.asicview.appdata;

import com.egorgoncharov.asicview.appdata.xml.AppInfo;
import com.egorgoncharov.asicview.appdata.xml.Asic;
import com.egorgoncharov.asicview.appdata.xml.SortingMethods;
import com.egorgoncharov.asicview.appdata.xml.Theme;

import java.util.List;

public class AppData {
    private final AppInfo appInfo;
    private final List<Asic> asics;
    private Theme theme;
    private SortingMethods sortingMode;

    public AppData(AppInfo appInfo, List<Asic> asics, Theme theme, SortingMethods sortingMode) {
        this.appInfo = appInfo;
        this.asics = asics;
        this.theme = theme;
        this.sortingMode = sortingMode;
    }

    public AppInfo getAppInfo() {
        return appInfo;
    }

    public List<Asic> getAsics() {
        return asics;
    }

    public void setTheme(Theme theme) {
        this.theme = theme;
    }

    public Theme getTheme() {
        return theme;
    }

    public SortingMethods getSortingMode() {
        return sortingMode;
    }

    public void setSortingMode(SortingMethods sortingMode) {
        this.sortingMode = sortingMode;
    }
}
