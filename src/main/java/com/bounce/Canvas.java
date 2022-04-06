package com.bounce;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

/**
 * Entry point of the application
 */
public class Canvas extends Application {

    /**
     * Adds the "control-panel.fxml" view over the main view.
     */
    public static void initializeControlPanel() {
        try {
            FXMLLoader loader = new FXMLLoader(Canvas.class.getResource("control-panel.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = new Stage();
            stage.setScene(scene);
            // Set title & window style
            stage.setTitle("Control Panel");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.UNDECORATED);
            // Retrieve screen size
            Rectangle2D screenSize = Screen.getPrimary().getVisualBounds();
            stage.setResizable(false);
            // Show the stage
            stage.show();
            // Position the stage to the center-bottom of the screen
            // Has to be done after the stage is shown, otherwise the height of the stage is null
            stage.centerOnScreen();
            // Screen height - Height of the stage = Exactly the bottom of the screen
            stage.setY(screenSize.getHeight() - stage.getHeight());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void start(Stage stage) throws IOException {
        // Create the scene and link it with it's corresponding controller
        FXMLLoader fxmlLoader = new FXMLLoader(Canvas.class.getResource("main-view.fxml"));
        Scene canvas = new Scene(fxmlLoader.load());
        // Retrieve screen size
        Rectangle2D screenSize = Screen.getPrimary().getVisualBounds();
        // Non-resizable and maximized window
        stage.setResizable(false);
        stage.setMaximized(true);
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

        // Show the control panel
        initializeControlPanel();
    }

    public static void main(String[] args) {
        launch();
    }
}