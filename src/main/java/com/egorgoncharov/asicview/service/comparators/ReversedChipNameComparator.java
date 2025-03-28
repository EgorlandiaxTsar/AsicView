package com.egorgoncharov.asicview.service.comparators;

import java.util.Comparator;

public class ReversedChipNameComparator extends ChipNameComparator implements Comparator<String> {
    @Override
    protected int analyzeChipName(String o1, String o2) {
        int result = super.analyzeChipName(o1, o2);
        if (result == 0) return result;
        return result == 1 ? -1 : 1;
    }
}
