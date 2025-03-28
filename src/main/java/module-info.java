module com.egorgoncharov.asicview {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.jsoup;
    requires java.xml;
    requires java.net.http;
    requires org.apache.logging.log4j;
    requires org.apache.logging.log4j.core;

    exports com.egorgoncharov.asicview;
    exports com.egorgoncharov.asicview.controllers;
    opens com.egorgoncharov.asicview to javafx.fxml;
    opens com.egorgoncharov.asicview.controllers to javafx.fxml;
    exports com.egorgoncharov.asicview.service.theme;
    opens com.egorgoncharov.asicview.service.theme to javafx.fxml;
}