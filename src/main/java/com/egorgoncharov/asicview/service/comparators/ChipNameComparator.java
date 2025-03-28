package com.egorgoncharov.asicview.service.comparators;

import java.util.Comparator;

public class ChipNameComparator implements Comparator<String> {
    @Override
    public int compare(String o1, String o2) {
        return analyzeChipName(o1, o2);
    }

    @Override
    public Comparator<String> reversed() {
        return new ReversedChipNameComparator();
    }

    protected int analyzeChipName(String o1, String o2) {
        if (o1.replaceAll("\\D+", "").isEmpty() || o2.replaceAll("\\D+", "").isEmpty()) return o1.compareTo(o2);
        int o1Id = Integer.parseInt(o1.replaceAll("\\D+", ""));
        int o2Id = Integer.parseInt(o2.replaceAll("\\D+", ""));
        return Integer.compare(o1Id, o2Id);
    }

}
