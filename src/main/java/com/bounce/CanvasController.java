package com.bounce;

import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Sphere;
import javafx.stage.Screen;
import javafx.util.Duration;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

/**
 * Controller for the canvas view
 */
public class CanvasController implements Initializable {
    // Root pane
    @FXML private AnchorPane canvas;

    private TranslateTransition controlPanelVisible;
    private TranslateTransition controlPanelInvisible;
    private boolean controlPanelToggle = false;

    ArrayList<Sphere> sphereList = new ArrayList<>();
    ArrayList<Thread> threadList = new ArrayList<>();

    // Getters to use in conjunction with the "Bridge" class
    public AnchorPane getCanvas() {
        return canvas;
    }

    public ArrayList<Sphere> getSphereList() {
        return sphereList;
    }

    public ArrayList<Thread> getThreadList() {
        return threadList;
    }

    /**
     * Creates a TabPane that will facilitate the functionality of the program to the user
     * This Pane can be "injected" into the canvas and modified dynamically
     * @return the "controlPanel" TabPane
     */
    public TabPane generateControlPanel() {
        // Instantiate control panel and "hardcode" its dimensions to 600x200
        TabPane controlPanel = new TabPane();
        controlPanel.prefHeight(200.0);
        controlPanel.prefWidth(600.0);
        controlPanel.minHeight(200.0);
        controlPanel.minWidth(600.0);
        controlPanel.maxHeight(200.0);
        controlPanel.maxWidth(600.0);
        // Retrieve screen size of the viewport
        Rectangle2D screenSize = Screen.getPrimary().getBounds();
        // Responsively position the control panel to the bottom-center of the screen
        controlPanel.setLayoutY(screenSize.getHeight() - 200.0);
        controlPanel.setLayoutX(screenSize.getWidth() / 2.0 - 300.0);

        controlPanelInvisible = new TranslateTransition(Duration.millis(250), controlPanel);
        controlPanelInvisible.setByY(-200.0);
        controlPanelInvisible.setOnFinished(event -> controlPanelToggle = false);
        controlPanelVisible = new TranslateTransition(Duration.millis(250), controlPanel);
        controlPanelVisible.setByY(200.0);
        controlPanelVisible.setOnFinished(event -> controlPanelToggle = true);

        controlPanel.setId("control-panel");
        // Make individual tabs impossible to close as that functionality is detrimental here
        controlPanel.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        controlPanel.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.H) {
                if (controlPanelToggle) {
                    controlPanelVisible.stop();
                    controlPanelInvisible.play();
                } else {
                    controlPanelInvisible.stop();
                    controlPanelVisible.play();
                }
            }
        });

        // Threads start

        HBox threadsContent = new HBox();
        threadsContent.setPrefSize(600.0,180.0);

        Tab threads = new Tab();
        threads.setText("Threads");
        threads.setContent(threadsContent);

        // Controls start

        ColorPicker colorPicker = new ColorPicker();
        colorPicker.setId("color");

        Button generateSphere = new Button();
        generateSphere.setText("Generate Sphere");
        generateSphere.setMnemonicParsing(false);
        generateSphere.setId("generate");
        generateSphere.setOnAction(actionEvent -> {
            NewSphere sphere = new NewSphere();
            sphere.start();
            Label threadLabel = new Label();
            threadLabel.setStyle("-fx-font-size: 10px; -fx-font-family: Arial; -fx-background-color: #fff");
            threadLabel.setText(sphere.getName());
            threadsContent.getChildren().add(threadLabel);
            threadList.add(sphere);
        });

        HBox controlsContent = new HBox();
        controlsContent.setPrefSize(600.0,180.0);
        controlsContent.getChildren().addAll(generateSphere, colorPicker);

        Tab controls = new Tab();
        controls.setText("Controls");
        controls.setContent(controlsContent);

        // Stats start
        Button exit = new Button();
        exit.setText("Exit");
        exit.setMnemonicParsing(false);
        exit.setId("exit");
        exit.setOnAction(actionEvent -> {
            Platform.exit();
        });

        Button clear = new Button();
        clear.setText("Clear");
        clear.setMnemonicParsing(false);
        clear.setId("clear");
        exit.setLayoutX(50.0);
        exit.setLayoutY(50.0);
        clear.setOnAction(actionEvent -> {
            for (Sphere sphere : sphereList) {
                canvas.getChildren().remove(sphere);
            }
            for (Thread thread : threadList) {
                thread.interrupt();
            }
        });

        AnchorPane statsContent = new AnchorPane();
        statsContent.setPrefSize(600.0,180.0);
        statsContent.getChildren().addAll(exit, clear);

        Tab stats = new Tab();
        stats.setText("Stats");
        stats.setContent(statsContent);


        controlPanel.getTabs().addAll(controls, stats, threads);

        return controlPanel;
    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Bridge.setCanvasController(this);
        canvas.getChildren().add(generateControlPanel());
    }
}
