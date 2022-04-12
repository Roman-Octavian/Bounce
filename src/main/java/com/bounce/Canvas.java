package com.bounce;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.util.Objects;

/**
 * Entry point of the application
 */
public class Canvas extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        // Create the scene and link it with it's corresponding controller
        FXMLLoader fxmlLoader = new FXMLLoader(Canvas.class.getResource("canvas.fxml"));
        Scene canvas = new Scene(fxmlLoader.load());
        // Link canvas scene to css stylesheet
        canvas.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/com/bounce/styles.css")).toExternalForm());
        // Non-resizable and maximized window
        stage.setResizable(false);
        stage.setMaximized(true);
        // Retrieve logical screen size
        Rectangle2D screenSize = Screen.getPrimary().getVisualBounds();
        // Size window to fit entire screen, center it for good measure
        stage.setWidth(screenSize.getWidth());
        stage.setHeight(screenSize.getHeight());
        stage.centerOnScreen();
        // Achieve transparency
        canvas.setFill(null);
        stage.initStyle(StageStyle.TRANSPARENT);
        // Set title & icon
        stage.setTitle("Bouncing Spheres");
        Image icon = new Image("file:src/main/resources/assets/icon.png");
        stage.getIcons().add(icon);
        // Show the scene
        stage.setScene(canvas);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}