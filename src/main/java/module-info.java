module com.bounce {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires java.desktop;
    requires java.sql;


    opens com.bounce to javafx.fxml;
    exports com.bounce;
}