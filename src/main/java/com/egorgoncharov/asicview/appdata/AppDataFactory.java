package com.egorgoncharov.asicview.appdata;

import com.egorgoncharov.asicview.appdata.xml.*;
import javafx.application.Platform;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class AppDataFactory {
    private static final Logger logger = LogManager.getLogger(AppDataFactory.class);
    private final String datasourcePath;

    public AppDataFactory(String datasourcePath, boolean initIfNotFound) {
        logger.info("Creating 'AppData' object");
        this.datasourcePath = datasourcePath;
        if (initIfNotFound) {
            File file = new File(datasourcePath);
            if (!file.exists()) {
                try {
                    logger.warn("'AppData' object configuration file not found");
                    initDatasource();
                } catch (IOException e) {
                    logger.fatal("Failed to create 'AppData' object configuration file", e);
                    Platform.exit();
                    System.exit(1);
                }
            }
        }
    }

    public AppDataFactory(String datasourcePath) {
        this(datasourcePath, true);
    }

    public AppData getAppData() {
        List<Asic> asics = new ArrayList<>();
        Theme theme;
        SortingMethods sortingMode;
        InputStream inputStream;
        try {
            inputStream = new FileInputStream(datasourcePath);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
        InputSource is = new InputSource(reader);
        is.setEncoding("UTF-8");
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        AppInfo appInfo;
        try {
            dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(is);
            doc.getDocumentElement().normalize();
            appInfo = new AppInfo(
                    doc.getElementsByTagName("Name").item(0).getTextContent(),
                    doc.getElementsByTagName("Version").item(0).getTextContent()
            );
            NodeList asicsNode = doc.getElementsByTagName("Asic");
            for (int i = 0; i < asicsNode.getLength(); i++) {
                Node asicNode = asicsNode.item(i);
                if (asicNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element asicElement = (Element) asicNode;
                    List<ManualLayoutSetting> manualLayoutSettings = new ArrayList<>();
                    NodeList manualLayoutSettingsNode = asicElement.getElementsByTagName("ManualLayoutSetting");
                    for (int j = 0; j < manualLayoutSettingsNode.getLength(); j++) {
                        Node manualLayoutSettingNode = manualLayoutSettingsNode.item(j);
                        if (manualLayoutSettingNode.getNodeType() == Node.ELEMENT_NODE) {
                            Element manualLayoutSettingElement = (Element) manualLayoutSettingNode;
                            manualLayoutSettings.add(new ManualLayoutSetting(
                                    Integer.parseInt(manualLayoutSettingElement.getElementsByTagName("Slot").item(0).getTextContent()),
                                    Integer.parseInt(manualLayoutSettingElement.getElementsByTagName("MinimalTemperature").item(0).getTextContent()),
                                    Integer.parseInt(manualLayoutSettingElement.getElementsByTagName("MaximalTemperature").item(0).getTextContent())
                            ));
                        }
                    }
                    asics.add(new Asic(
                            asicElement.getElementsByTagName("Ip").item(0).getTextContent(),
                            asicElement.getElementsByTagName("Comment").item(0).getTextContent(),
                            Integer.parseInt(asicElement.getElementsByTagName("Position").item(0).getTextContent()),
                            Integer.parseInt(asicElement.getElementsByTagName("Refresh").item(0).getTextContent()),
                            Layout.classifyLayout(asicElement.getElementsByTagName("Layout").item(0).getTextContent()),
                            manualLayoutSettings,
                            asicElement.getElementsByTagName("Username").item(0).getTextContent(),
                            asicElement.getElementsByTagName("Password").item(0).getTextContent(),
                            false
                    ));
                }
            }
            theme = Theme.classifyTheme(doc.getElementsByTagName("Theme").item(0).getTextContent());
            sortingMode = SortingMethods.classifySortingMethod(doc.getElementsByTagName("SortingMode").item(0).getTextContent());
        } catch (ParserConfigurationException | SAXException | IOException e) {
            logger.fatal("Failed to read 'AppData' object configuration file", e);
            Platform.exit();
            System.exit(1);
            return null;
        }
        return new AppData(appInfo, asics, theme, sortingMode);
    }

    public void saveAppData(AppData datasource) {
        logger.info("Updating 'AppData' object configuration file (source: " + datasource.hashCode() + ")");
        String appInfo = "\t<AppInfo>\n\t\t<Name>" + datasource.getAppInfo().getName() + "</Name>\n\t\t<Version>" + datasource.getAppInfo().getVersion() + "</Version>\n\t</AppInfo>\n";
        StringBuilder asics = new StringBuilder("\t<Asics>\n");
        for (Asic asicInfo : datasource.getAsics()) {
            StringBuilder asic = new StringBuilder("\t\t<Asic>\n\t\t\t<Ip>" + asicInfo.getIp() + "</Ip>\n\t\t\t<Comment>" + asicInfo.getComment() + "</Comment>\n\t\t\t<Position>" + asicInfo.getPosition() + "</Position>\n\t\t\t<Refresh>" + asicInfo.getRefreshInterval() + "</Refresh>\n\t\t\t<Layout>" + asicInfo.getLayout().toString() + "</Layout>\n\t\t\t<Authorization>\n\t\t\t\t<Username>" + asicInfo.getUsername() + "</Username>\n\t\t\t\t<Password>" + asicInfo.getPassword() + "</Password>\n\t\t\t</Authorization>\n\t\t\t<ManualLayoutSettings>\n");
            for (ManualLayoutSetting manualLayoutSettingInfo : asicInfo.getManualLayoutSettings()) {
                asic.append("\t\t\t\t<ManualLayoutSetting>\n\t\t\t\t\t<Slot>").append(manualLayoutSettingInfo.getSlot()).append("</Slot>\n\t\t\t\t\t<MinimalTemperature>").append(manualLayoutSettingInfo.getMinimalTemperature()).append("</MinimalTemperature>\n\t\t\t\t\t<MaximalTemperature>").append(manualLayoutSettingInfo.getMaximalTemperature()).append("</MaximalTemperature>\n\t\t\t\t</ManualLayoutSetting>\n");
            }
            asic.append("\t\t\t</ManualLayoutSettings>\n\t\t</Asic>\n");
            asics.append(asic);
        }
        asics.append("\t</Asics>\n");
        String xmlText = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n<AppData>\n" + appInfo + asics + "\t<Theme>" + datasource.getTheme().toString() + "</Theme>\n\t<SortingMode>" + datasource.getSortingMode() + "</SortingMode>\n</AppData>";
        try {
            new FileWriter(datasourcePath).close();
            FileWriter writer = new FileWriter(datasourcePath, true);
            writer.write(xmlText);
            writer.close();
            logger.info("'AppData' object configuration file updated");
        } catch (IOException e) {
            logger.error("'AppData' object configuration file update failed", e);
        }
    }

    private void initDatasource() throws IOException {
        logger.info("Creating 'AppData' object configuration file in " + datasourcePath);
        File datasource = new File(datasourcePath);
        if (datasource.exists()) {
            logger.fatal("Internal application error: failed to create 'AppData' object configuration file in " + datasourcePath);
            Platform.exit();
            System.exit(1);
        }
        boolean created = datasource.createNewFile();
        if (created) {
            logger.info("'AppData' object configuration file created");
        } else {
            logger.fatal("Internal application error: failed to create 'AppData' object configuration file in " + datasourcePath);
            Platform.exit();
            System.exit(1);
        }
        saveAppData(new AppData(
                new AppInfo(
                        "AsicView",
                        "1.2"
                ),
                new ArrayList<>(),
                Theme.LIGHT,
                SortingMethods.NONE
        ));
    }

    public String getDatasourcePath() {
        return datasourcePath;
    }
}
