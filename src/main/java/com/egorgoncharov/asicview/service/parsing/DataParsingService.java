package com.egorgoncharov.asicview.service.parsing;

import com.egorgoncharov.asicview.service.fetching.data.AsicEntity;
import com.egorgoncharov.asicview.service.fetching.data.ChipEntity;
import com.egorgoncharov.asicview.service.fetching.data.SlotEntity;
import com.egorgoncharov.asicview.service.parsing.exception.MachineDataParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.List;

public class DataParsingService {
    private static final Logger logger = LogManager.getLogger(DataParsingService.class);
    private final String text;

    public static DataParsingService fromDoc(Element doc) {
        Element textarea = doc.getElementById("syslog");
        //noinspection ConstantConditions
        return new DataParsingService(textarea.text());
    }

    public DataParsingService(String text) {
        this.text = text.trim();
    }

    public AsicEntity parse(String ip) throws MachineDataParseException {
        logger.info("Parsing ASIC data (target ip: '" + ip + "')");
        try {
            AsicEntity asic = new AsicEntity(ip);
            List<String> slotsRawData = new ArrayList<>(List.of(text.split("\\(")));
            slotsRawData.remove(0);
            String[] readyToParsingSlotsRawData = new String[slotsRawData.size()];
            for (int i = 0; i < slotsRawData.size(); i++) {
                readyToParsingSlotsRawData[i] = slotsRawData.get(i).substring(0, slotsRawData.get(i).length() - 1);
            }
            for (String s : readyToParsingSlotsRawData) {
                SlotEntity asicSlot = new SlotEntity();
                asicSlot.setAsicIp(ip);
                List<String> data = new ArrayList<>(List.of(s.split("\n")));
                data.removeIf(d -> !d.startsWith("   ["));
                for (String dataCell : data) {
                    String key = dataCell.split(" => ")[0].trim();
                    String value = dataCell.split(" => ")[1].trim();
                    switch (key) {
                        case "[Elapsed]":
                            asicSlot.setElapsed(Integer.parseInt(value));
                            break;
                        case "[slot]":
                            asicSlot.setNumber(Integer.parseInt(value));
                            break;
                        case "[freqs_avg]":
                            asicSlot.setAverageFrequency(Double.parseDouble(value));
                            break;
                        case "[temp]":
                            asicSlot.setTemperature(Double.parseDouble(value));
                            break;
                    }
                    if (key.startsWith("[chip") && key.endsWith(" ]")) {
                        ChipEntity asicSlotChip = new ChipEntity();
                        asicSlotChip.setName(key.replaceAll("\\[", "").replaceAll("]", "").trim());
                        asicSlotChip.setAsicIp(ip);
                        value = value.replaceAll(":\\s+", ":").replaceAll("/\\s+", "/");
                        String[] chipData = value.trim().split(" ");
                        for (String chipDataCell : chipData) {
                            if (chipDataCell.isEmpty()) {
                                continue;
                            }
                            String chipDataKey = chipDataCell.split(":")[0];
                            String chipDataValue = chipDataCell.split(":")[1];
                            switch (chipDataKey) {
                                case "freq":
                                    asicSlotChip.setFrequency(Double.parseDouble(chipDataValue));
                                    break;
                                case "temp":
                                    asicSlotChip.setTemperature(Double.parseDouble(chipDataValue));
                                    break;
                                case "pct":
                                    asicSlotChip.setPctUsed(chipDataValue.split("/")[0].trim().replaceAll("%", ""));
                                    asicSlotChip.setPctAvailable(chipDataValue.split("/")[1].trim().replaceAll("%", ""));
                                    break;
                            }
                        }
                        asicSlot.getChips().add(asicSlotChip);
                    }
                }
                asic.getSlots().add(asicSlot);
            }
            return asic;
        } catch (RuntimeException e) {
            throw new MachineDataParseException("Failed to parse data", e);
        }
    }
}
