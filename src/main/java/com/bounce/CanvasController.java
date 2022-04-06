package com.bounce;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Sphere;
import java.net.URL;
import java.util.ResourceBundle;

public class CanvasController implements Initializable {
    @FXML private AnchorPane canvas;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Sphere sphere = new Sphere(100);
        canvas.getChildren().add(sphere);
        sphere.setLayoutX(100);
        sphere.setLayoutY(100);
    }
}
