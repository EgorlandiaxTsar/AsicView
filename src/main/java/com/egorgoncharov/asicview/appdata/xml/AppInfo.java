package com.egorgoncharov.asicview.appdata.xml;

public class AppInfo {
    private final String name;
    private final String version;
    private String homeDir;

    public AppInfo(String name, String version) {
        this.name = name;
        this.version = version;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public String getHomeDir() {
        return homeDir;
    }

    public void setHomeDir(String homeDir) {
        this.homeDir = homeDir;
    }
}
