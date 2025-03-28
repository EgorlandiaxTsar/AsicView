package com.egorgoncharov.asicview.service.comparators;

import java.util.Comparator;

public class IpAddressComparator implements Comparator<String> {
    @Override
    public int compare(String o1, String o2) {
        return ipLevelCompare(o1.split("\\."), o2.split("\\."), 0);
    }

    @Override
    public Comparator<String> reversed() {
        return new ReversedIpAddressComparator();
    }

    protected int ipLevelCompare(String[] o1, String[] o2, int level) {
        if (Integer.parseInt(o1[level]) > Integer.parseInt(o2[level])) {
            return -1;
        } else if (Integer.parseInt(o1[level]) < Integer.parseInt(o2[level])) {
            return 1;
        } else {
            if (level < 3) {
                return ipLevelCompare(o1, o2, level + 1);
            }
        }
        return 0;
    }
}
