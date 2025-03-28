package com.egorgoncharov.asicview.appdata.xml;

public enum SortingMethods {
    NONE,
    IP_UP,
    IP_DOWN,
    COMMENT_UP,
    COMMENT_DOWN;

    public static SortingMethods getDefault() {
        return NONE;
    }

    public static SortingMethods classifySortingMethod(String sortingMethod) {
        if (sortingMethod.equalsIgnoreCase("ip_up")) {
            return IP_UP;
        } else if (sortingMethod.equalsIgnoreCase("ip_down")) {
            return IP_DOWN;
        } else if (sortingMethod.equalsIgnoreCase("comment_up")) {
            return COMMENT_UP;
        } else if (sortingMethod.equalsIgnoreCase("comment_down")) {
            return COMMENT_DOWN;
        }
        return NONE;
    }
}
