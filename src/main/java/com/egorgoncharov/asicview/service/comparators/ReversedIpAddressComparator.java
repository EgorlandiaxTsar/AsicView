package com.egorgoncharov.asicview.service.comparators;

import java.util.Comparator;

public class ReversedIpAddressComparator extends IpAddressComparator implements Comparator<String> {
    @Override
    protected int ipLevelCompare(String[] o1, String[] o2, int level) {
        int result = super.ipLevelCompare(o1, o2, level);
        if (result == 0) return result;
        return result == 1 ? -1 : 1;
    }
}
