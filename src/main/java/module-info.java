module com.bounce.bounce {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.bounce to javafx.fxml;
    exports com.bounce;
}