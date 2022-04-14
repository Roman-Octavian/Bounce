module com.bounce.bounce {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires java.desktop;


    opens com.bounce to javafx.fxml;
    exports com.bounce;
}