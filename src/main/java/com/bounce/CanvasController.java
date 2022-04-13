package com.bounce;

import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Sphere;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
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
        // Create transitions to show/hide control panel
        controlPanelInvisible = new TranslateTransition(Duration.millis(250), controlPanel);
        controlPanelInvisible.setByY(-200.0);
        controlPanelInvisible.setOnFinished(event -> controlPanelToggle = false);
        controlPanelVisible = new TranslateTransition(Duration.millis(250), controlPanel);
        controlPanelVisible.setByY(200.0);
        controlPanelVisible.setOnFinished(event -> controlPanelToggle = true);

        // Show hide control panel on event action pressing key "H"
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
        // ID for styling
        controlPanel.setId("control-panel");
        // Make individual tabs impossible to close as that functionality is detrimental here
        controlPanel.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // Threads start
        HBox threadsContent = new HBox();
        threadsContent.setPrefSize(600.0,180.0);

        Tab threads = new Tab();
        threads.setText("Threads");
        threads.setContent(threadsContent);

        /* ----------------------TABS---------------------- */
        // Info Tab
        Tab info = new Tab();

        /* ----------------------TABS---------------------- */



        // Stats start
        Button exit = new Button();
        exit.setText("Exit");
        exit.setMnemonicParsing(false);
        exit.setId("exit");
        exit.setOnAction(actionEvent -> Platform.exit());

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


        controlPanel.getTabs().addAll(info, generateNewSphereTab(), stats, threads);

        return controlPanel;
    }

    /**
     * Generate the GUI elements of the "New Sphere" Tab.
     * Tedious. but dynamic.
     * @return the "New Sphere" Tab
     */
    public Tab generateNewSphereTab() {
        /* ===================NEW SPHERE START=================== */
        // New Sphere Tab
        Tab newSphere = new Tab();
        newSphere.setText("New Sphere");

        // Container for all New Sphere tab nodes
        VBox newSphereContainer = new VBox();
        newSphereContainer.setPrefSize(600.0, 180.0);

        // First row of the container
        HBox newSphereSectionA = new HBox();
        newSphereSectionA.setAlignment(Pos.CENTER);
        newSphereSectionA.setPrefSize(600.0, 60.0);

        // Second row of the container
        HBox newSphereSectionB = new HBox();
        newSphereSectionB.setAlignment(Pos.CENTER);
        newSphereSectionB.setPrefSize(600.0,60.0);

        // Third row of the container
        HBox newSphereSectionC = new HBox();
        newSphereSectionC.setAlignment(Pos.CENTER);
        newSphereSectionC.setPrefSize(600.0,40.0);

        /* ----------------------SECTION A---------------------- */

        // First half of the first row
        HBox sectionAInitialPosition = new HBox();
        sectionAInitialPosition.setPrefSize(300.0,60.0);
        sectionAInitialPosition.setSpacing(10.0);
        sectionAInitialPosition.setAlignment(Pos.CENTER);

        // Second half of the first row
        HBox sectionASizeSlider = new HBox();
        sectionASizeSlider.setPrefSize(300.0,60.0);
        sectionASizeSlider.setSpacing(10.0);
        sectionASizeSlider.setAlignment(Pos.CENTER);

        // Label for size slider node
        Label initialPositionLabel = new Label();
        initialPositionLabel.setText("Initial Position");

        // X coordinate position field
        TextField xPosField = new TextField();
        xPosField.setPromptText("Random");
        xPosField.setPrefSize(70.0,25.0);
        xPosField.setId("initialX");
        xPosField.textProperty().addListener((observableValue, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                xPosField.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
        Label xLabel = new Label();
        xLabel.setText("X");

        // Y coordinate position field
        TextField yPosField = new TextField();
        yPosField.setPromptText("Random");
        yPosField.setPrefSize(70.0,25.0);
        yPosField.setId("initialY");
        yPosField.textProperty().addListener((observableValue, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                yPosField.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
        Label yLabel = new Label();
        yLabel.setText("Y");

        // Label for size slider node
        Label sizeLabel = new Label();
        sizeLabel.setText("Size (25)");

        // Slider for size to determine sphere radius
        Slider sizeSlider = new Slider();
        sizeSlider.setId("size");
        sizeSlider.setPrefWidth(210.0);
        sizeSlider.setMin(10.0);
        sizeSlider.setMax(100.0);
        sizeSlider.setShowTickLabels(true);
        sizeSlider.setShowTickMarks(true);
        sizeSlider.setValue(25.0);
        sizeSlider.valueProperty().addListener((
                observableValue, oldValue, newValue) -> sizeLabel.textProperty().setValue("Size (" + newValue.intValue() + ")"));

        sectionAInitialPosition.getChildren().addAll(initialPositionLabel, xPosField, xLabel, yPosField, yLabel);
        sectionASizeSlider.getChildren().addAll(sizeLabel, sizeSlider);

        newSphereSectionA.getChildren().addAll(sectionAInitialPosition, sectionASizeSlider);

        /* ----------------------SECTION A---------------------- */

        /* ----------------------SECTION B---------------------- */
        // First half of the second row
        HBox sectionBSpeedAndDirection = new HBox();
        sectionBSpeedAndDirection.setPrefSize(300.0,60.0);
        sectionBSpeedAndDirection.setSpacing(10.0);
        sectionBSpeedAndDirection.setAlignment(Pos.CENTER);

        // Second half of the second row
        HBox sectionBColorPicker = new HBox();
        sectionBColorPicker.setPrefSize(300.0,60.0);
        sectionBColorPicker.setSpacing(10.0);
        sectionBColorPicker.setAlignment(Pos.CENTER_LEFT);

        // Label for speed and direction nodes
        Label speedAndDirectionLabel = new Label();
        speedAndDirectionLabel.setText("Initial Vector");
        speedAndDirectionLabel.setPrefWidth(75.0);

        // List of values to be stored inside Spinner nodes for Speed and Direction vector
        ObservableList<String> values = FXCollections.observableArrayList(
                "-10", "-9", "-8", "-7", "-6", "-5", "-4", "-3", "-2", "-1",
                "0", "Random",
                "1", "2", "3", "4", "5", "6", "7", "8", "9", "10"
        );
        // Spinner for X coordinate
        Spinner<String> xSpinner = new Spinner<>();
        xSpinner.setId("initialVectorX");
        xSpinner.setPrefSize(70.0, 25.0);
        SpinnerValueFactory<String> xValueFactory = new SpinnerValueFactory.ListSpinnerValueFactory<>(values);
        xValueFactory.setValue("Random");
        xSpinner.getEditor().setFont(Font.font(Font.getDefault().getName(), FontWeight.LIGHT, 9));
        xSpinner.setValueFactory(xValueFactory);
        // Label for X Spinner
        Label xSpinnerLabel = new Label();
        xSpinnerLabel.setText("X");

        // Spinner for Y coordinate
        Spinner<String> ySpinner = new Spinner<>();
        ySpinner.setId("initialVectorY");
        ySpinner.setPrefSize(70.0, 25.0);
        SpinnerValueFactory<String> yValueFactory = new SpinnerValueFactory.ListSpinnerValueFactory<>(values);
        yValueFactory.setValue("Random");
        ySpinner.getEditor().setFont(Font.font(Font.getDefault().getName(), FontWeight.LIGHT, 9));
        ySpinner.setValueFactory(yValueFactory);
        // Label for y Spinner
        Label ySpinnerLabel = new Label();
        ySpinnerLabel.setText("Y");

        // Region to space out the second half of the row
        Region colorRegion = new Region();
        colorRegion.setPrefWidth(10.0);

        // Label for colorPicker node
        Label colorLabel = new Label();
        colorLabel.setText("Colour");
        colorLabel.setPrefWidth(40.0);

        // colorPicker to choose sphere color
        ColorPicker colorPicker = new ColorPicker();
        colorPicker.setId("color");
        colorPicker.setPrefWidth(120.0);

        // Add all nodes to either first or second half of the row
        sectionBSpeedAndDirection.getChildren().addAll(speedAndDirectionLabel, xSpinner, xSpinnerLabel, ySpinner, ySpinnerLabel);
        sectionBColorPicker.getChildren().addAll(colorRegion, colorLabel, colorPicker);

        // Add both halves to the entire row
        newSphereSectionB.getChildren().addAll(sectionBSpeedAndDirection, sectionBColorPicker);

        /* ----------------------SECTION B---------------------- */

        /* ----------------------SECTION C---------------------- */
        // Whole of the third row
        HBox sectionCButtons = new HBox();
        sectionCButtons.setPrefSize(600.0,60.0);
        sectionCButtons.setSpacing(10.0);
        sectionCButtons.setAlignment(Pos.CENTER);

        // Button to reset the fields to their default values
        Button resetValues = new Button();
        resetValues.setText("Reset Fields");
        resetValues.setId("reset");
        resetValues.setPrefWidth(120.0);
        resetValues.setMnemonicParsing(false);
        resetValues.setOnAction(actionEvent -> {
            xPosField.setText("");
            yPosField.setText("");
            sizeSlider.setValue(25.0);
            xValueFactory.setValue("Random");
            yValueFactory.setValue("Random");
            colorPicker.setValue(Color.WHITE);
        });

        // Region to space out both button in this row
        Region spacingRegion = new Region();
        spacingRegion.setPrefWidth(120.0);

        // Button to generate a new sphere in accordance to all fields
        Button generateSphere = new Button();
        generateSphere.setText("Generate Sphere");
        generateSphere.setId("generate");
        generateSphere.setMnemonicParsing(false);
        generateSphere.setPrefWidth(120.0);
        generateSphere.setOnAction(actionEvent -> {
            NewSphere sphere = new NewSphere();
            sphere.start();
            /* Label threadLabel = new Label();
            threadLabel.setStyle("-fx-font-size: 10px; -fx-font-family: Arial; -fx-background-color: #fff");
            threadLabel.setText(sphere.getName());

            threadsContent.getChildren().add(threadLabel);
            threadList.add(sphere); */
        });
        // Add both buttons and the spacing region to the row
        sectionCButtons.getChildren().addAll(resetValues, spacingRegion, generateSphere);
        // Add the row the section
        newSphereSectionC.getChildren().add(sectionCButtons);

        /* ----------------------SECTION C---------------------- */
        // Add all three sections (rows) to the newSphere outer container
        newSphereContainer.getChildren().addAll(newSphereSectionA, newSphereSectionB, newSphereSectionC);
        // Set content tab to the outer container
        newSphere.setContent(newSphereContainer);
        return newSphere;
        /* ===================NEW SPHERE END=================== */
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Bridge.setCanvasController(this);
        canvas.getChildren().add(generateControlPanel());
    }
}
