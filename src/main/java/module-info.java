module com.bounce.bounce {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;


    opens com.bounce to javafx.fxml;
    exports com.bounce;
}