package com.bounce;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;

import java.net.URL;
import java.util.ResourceBundle;

public class ControlPanelController implements Initializable {
    @FXML Button close;
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        close.setOnAction(actionEvent -> {
            Platform.exit();
        });
    }
}
