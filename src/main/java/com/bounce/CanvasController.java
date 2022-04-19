package com.bounce;

import javafx.animation.AnimationTimer;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Sphere;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.awt.Desktop;

/**
 * Controller for the canvas view
 */
public class CanvasController implements Initializable {
    // Root pane
    @FXML private AnchorPane canvas;

    // Transitions and boolean for toggling the control panel's visibility
    private TranslateTransition controlPanelVisible;
    private TranslateTransition controlPanelInvisible;
    private boolean controlPanelToggle = false;

    // Variable to store the database connection
    private static Connection connection = Database.getConnection();

    // Session counters to keep track of events for statistics tab
    private int sessionSphereCount = 0;
    private int sphereCollisionCount = 0;
    private int wallCollisionCount = 0;
    // Global counters to keep track of events for statistics tab
    private int globalSphereCount = 0;
    private int globalSphereCollisionCount = 0;
    private int globalWallCollisionCount = 0;

    // ArrayLists for sphere, thread and animation manipulation down the line
    ArrayList<Sphere> sphereList = new ArrayList<>();
    ArrayList<Thread> threadList = new ArrayList<>();
    ArrayList<AnimationTimer> animationList = new ArrayList<>();

    // Getters and setters to use in conjunction with the "Bridge" class
    // Getters
    public AnchorPane getCanvas() {
        return canvas;
    }

    public int getSessionSphereCount() {
        return sessionSphereCount;
    }

    public int getSphereCollisionCount() {
        return sphereCollisionCount;
    }

    public int getWallCollisionCount() {
        return wallCollisionCount;
    }

    public ArrayList<Sphere> getSphereList() {
        return sphereList;
    }

    public ArrayList<Thread> getThreadList() {
        return threadList;
    }

    public ArrayList<AnimationTimer> getAnimationList() {
        return animationList;
    }
    // Setters
    public void setSphereCollisionCount(int sphereCollisionCount) {
        this.sphereCollisionCount = sphereCollisionCount;
    }

    public void setWallCollisionCount(int wallCollisionCount) {
        this.wallCollisionCount = wallCollisionCount;
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
        // Add all tabs to control panel
        controlPanel.getTabs().addAll(generateInfoTab(), generateNewSphereTab(), generateStatsTab(), generateOptionsTab());

        return controlPanel;
    }

    /**
     * Function to define basic properties of HBox elements.
     * The "rows" in our tabs will be contained within HBox nodes.
     * This function customizes any given amount of HBoxes at once.
     * Avoids needless repetition.
     * @param styleClass CSS class that will apply to all HBox arguments
     * @param position Position enum that wil apply to all HBox arguments
     * @param height Height in pixels of all HBox elements
     * @param width Width in pixels of all HBox elements
     * @param args Any given amount of HBox nodes to be customized by the function
     */
    public void customizeBasicHBox(String styleClass, Pos position, double height, double width, HBox... args) {
        for (HBox section : args) {
            section.getStyleClass().add(styleClass);
            section.setAlignment(position);
            section.setPrefHeight(height);
            section.setPrefWidth(width);
        }
    }

    /**
     * Generate the GUI elements of the "Info" Tab.
     * @return the "Info" Tab
     */
    public Tab generateInfoTab() {
        // New tab
        Tab info = new Tab();
        info.setText("Info");
        // Content container for ScrollPane
        VBox infoContainer = new VBox();
        infoContainer.setPrefWidth(600.0);
        // Container for all options tab nodes
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setPrefSize(600.0, 180.0);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setContent(infoContainer);

        // Instantiating and customizing sections
        HBox infoSectionA = new HBox();
        customizeBasicHBox(null, Pos.CENTER, 20.0, 600.0, infoSectionA);

        HBox infoSectionB = new HBox();
        HBox infoSectionC = new HBox();
        HBox infoSectionD = new HBox();
        HBox infoSectionE = new HBox();
        HBox infoSectionF = new HBox();
        HBox infoSectionG = new HBox();
        customizeBasicHBox("info-container", Pos.CENTER_LEFT, 0.0, 600.0, infoSectionB, infoSectionC, infoSectionD, infoSectionE, infoSectionF, infoSectionG);

        // Text elements for the tab
        Text header = new Text("Bouncing Spheres");
        header.setId("header");

        Text description = new Text("This app facilitates (very) rudimentary multi-threaded ball physics simulations.");
        description.setId("description");

        Text howToUse = new Text("How to use:");
        howToUse.setId("howToUse");

        Text generalInformation = new Text(
                "Press the \"H\" key to show/hide this panel. " +
                "Set sphere size, color, position and vector in \"New Sphere\"."
        );
        generalInformation.wrappingWidthProperty().set(570);
        // CSS style class
        generalInformation.getStyleClass().add("normal-text");

        Text vector = new Text(
                    "The vector is the initial movement pattern. " +
                    "Negative numbers designate backward movement, and positive numbers forward movement. " +
                    "Higher values will produce a higher velocity."
        );
        vector.wrappingWidthProperty().set(570);
        vector.getStyleClass().add("normal-text");

        Text initialPosition = new Text(
                        "Your display resolution is detected as " + (int) Screen.getPrimary().getVisualBounds().getMaxY() + "x" + (int) Screen.getPrimary().getVisualBounds().getMaxX() + ". " +
                        "Generating spheres outside of those bounds will force spheres " +
                        "to the nearest coordinate on screen." +
                        "Drag the coordinates picker (+) to pinpoint a particular coordinate."
        );
        initialPosition.wrappingWidthProperty().set(570);
        initialPosition.getStyleClass().add("normal-text");

        Text cText = new Text("Not enough? ");
        cText.getStyleClass().add("normal-text");

        // Contact link that should open browser
        Hyperlink contact = new Hyperlink("Get in touch");
        contact.getStyleClass().add("normal-text");
        contact.setOnAction(actionEvent -> {
            try {
                Desktop.getDesktop().browse(new URI("https://github.com/Roman-Octavian"));
            } catch (IOException | URISyntaxException e) {
                e.printStackTrace();
            }
        });

        // Add all nodes to row containers
        infoSectionA.getChildren().add(header);
        infoSectionB.getChildren().add(description);
        infoSectionC.getChildren().add(howToUse);
        infoSectionD.getChildren().add(generalInformation);
        infoSectionE.getChildren().add(vector);
        infoSectionF.getChildren().add(initialPosition);
        infoSectionG.getChildren().addAll(cText, contact);

        // Add all rows to ScrollPane content
        infoContainer.getChildren().addAll(infoSectionA, infoSectionB, infoSectionC, infoSectionD, infoSectionE, infoSectionF, infoSectionG);
        // Set content of the tab
        info.setContent(scrollPane);

        return info;
    }

    /**
     * Generate the GUI elements of the "New Sphere" Tab.
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
        // Second row of the container
        HBox newSphereSectionB = new HBox();
        // Third row of the container
        HBox newSphereSectionC = new HBox();

        customizeBasicHBox(null, Pos.CENTER, 60.0, 600.0, newSphereSectionA, newSphereSectionB);
        customizeBasicHBox(null, Pos.CENTER, 40.0, 600.0, newSphereSectionC);

        /* ----------------------SECTION A---------------------- */
        // First half of the first row
        HBox sectionAInitialPosition = new HBox();
        sectionAInitialPosition.setSpacing(10.0);
        // Second half of the first row
        HBox sectionASizeSlider = new HBox();
        sectionASizeSlider.setSpacing(10.0);

        customizeBasicHBox(null, Pos.CENTER, 60.0, 300.0, sectionAInitialPosition, sectionASizeSlider);

        // Label for size slider node
        Label initialPositionLabel = new Label();
        initialPositionLabel.setText("Position");

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

        /* Button that will add cursor coordinates to position fields on mouse drag release
        Draws lines on all four axis on drag, to display movement nicely */
        Button coordsPicker = new Button("+");
        coordsPicker.setId("coordsPicker");
        // Line from leftmost side to mouse cursor
        Line xLeft = new Line();
        xLeft.getStyleClass().add("line");
        // Line from rightmost side to mouse cursor
        Line xRight = new Line();
        xRight.getStyleClass().add("line");
        // Line from topmost side to mouse cursor
        Line yTop = new Line();
        yTop.getStyleClass().add("line");
        // Line from bottommost side to mouse cursor
        Line yBottom = new Line();
        yBottom.getStyleClass().add("line");

        // Change cursor to open hand on hover
        coordsPicker.setOnMouseEntered(mouseEvent -> canvas.setCursor(Cursor.OPEN_HAND));
        coordsPicker.setOnMouseExited(mouseEvent -> canvas.setCursor(Cursor.DEFAULT));
        // Immediately upon mouse press, add lines to canvas
        coordsPicker.setOnMousePressed(mouseEvent -> {
            canvas.getChildren().add(xLeft);
            canvas.getChildren().add(xRight);
            canvas.getChildren().add(yTop);
            canvas.getChildren().add(yBottom);
        });
        // As we are dragging the mouse along, draw the lines from the edges of the screen to the cursor
        coordsPicker.setOnMouseDragged(mouseEvent -> {
            // Change cursor to cross-hair on drag
            canvas.setCursor(Cursor.CROSSHAIR);

            xLeft.setStartX(Screen.getPrimary().getBounds().getMinX());
            xLeft.setEndX(mouseEvent.getScreenX());
            xLeft.setStartY(mouseEvent.getScreenY());
            xLeft.setEndY(mouseEvent.getScreenY());

            xRight.setStartX(Screen.getPrimary().getBounds().getMaxX());
            xRight.setEndX(mouseEvent.getScreenX());
            xRight.setStartY(mouseEvent.getScreenY());
            xRight.setEndY(mouseEvent.getScreenY());

            yTop.setStartX(mouseEvent.getScreenX());
            yTop.setEndX(mouseEvent.getScreenX());
            yTop.setStartY(Screen.getPrimary().getBounds().getMinY());
            yTop.setEndY(mouseEvent.getScreenY());

            yBottom.setStartX(mouseEvent.getScreenX());
            yBottom.setEndX(mouseEvent.getScreenX());
            yBottom.setStartY(Screen.getPrimary().getBounds().getMaxY());
            yBottom.setEndY(mouseEvent.getScreenY());
        });
        // Make lines visible as we enter the drag
        coordsPicker.setOnMouseDragEntered(mouseDragEvent -> {
            xLeft.setVisible(true);
            xRight.setVisible(true);
            yTop.setVisible(true);
            yBottom.setVisible(true);
        });
        // Make lines invisible as we leave the drag
        coordsPicker.setOnMouseDragExited(mouseDragEvent -> {
            xLeft.setVisible(false);
            xRight.setVisible(false);
            yTop.setVisible(false);
            yBottom.setVisible(false);
        });
        /* When mouse is released, first, get cursor coordinates and add them to the position fields
        second, reset the lines, and remove them from the canvas */
        coordsPicker.setOnMouseReleased(mouseEvent -> {
            // Take cursor coordinates and add them to position fields
            xPosField.setText(String.valueOf((int) mouseEvent.getScreenX()));
            yPosField.setText(String.valueOf((int) mouseEvent.getScreenY()));

            // Reset lines
            resetLines(xLeft, xRight, yTop, yBottom);
            // Reset cursor
            canvas.setCursor(Cursor.DEFAULT);
        });

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

        sectionAInitialPosition.getChildren().addAll(initialPositionLabel, coordsPicker, xPosField, xLabel, yPosField, yLabel);
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
                "Random", "0",
                "1", "2", "3", "4", "5", "6", "7", "8", "9", "10"
        );

        // ValueFactories for spinner values
        SpinnerValueFactory<String> xValueFactory = new SpinnerValueFactory.ListSpinnerValueFactory<>(values);
        SpinnerValueFactory<String> yValueFactory = new SpinnerValueFactory.ListSpinnerValueFactory<>(values);

        // Spinner for X coordinate
        Spinner<String> xSpinner = new Spinner<>();
        xSpinner.setId("initialVectorX");
        xSpinner.setPrefSize(70.0, 25.0);
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
            // Create a new thread and start it
            NewSphere sphere = new NewSphere();
            sphere.start();
            //Add thread to threadList
            Bridge.getCanvasController().getThreadList().add(sphere);
            // Increment the session sphere generation count by one
            sessionSphereCount += 1;
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

    /**
     * Reset any given amount of lines back to their initial state
     * Resets dimensions and removes lines from canvas
     * @param line lines to be reset
     */
    public void resetLines(Line... line) {

        for (Line l : line) {
            l.setStartX(0);
            l.setStartY(0);
            l.setEndX(0);
            l.setEndY(0);
            canvas.getChildren().remove(l);
        }
    }

    /**
     * Generate the GUI elements of the "Options" Tab.
     * @return the "Options" Tab
     */
    public Tab generateOptionsTab() {
        // New tab
        Tab options = new Tab();
        options.setText("Options");
        // Container for all options tab nodes
        VBox optionsContainer = new VBox();
        optionsContainer.setPrefSize(600.0, 180.0);

        // First row of the container
        HBox optionsSectionA = new HBox();
        // Second row of the container
        HBox optionsSectionB = new HBox();
        // Third row of the container
        HBox optionsSectionC = new HBox();

        customizeBasicHBox(null, Pos.CENTER_LEFT, 60.0, 600.0, optionsSectionA);
        customizeBasicHBox(null, Pos.CENTER_LEFT, 55.0, 600.0, optionsSectionB, optionsSectionC);

        /* ----------------------SECTION A---------------------- */
        // Region to space nodes out
        Region spacingRegionA = new Region();
        spacingRegionA.setPrefSize(50.0, 55.0);

        // Toggle buttons to turn sound on or off. Off by default because FX MediaPlayer is very laggy.
        ToggleGroup toggleGroup = new ToggleGroup();
        ToggleButton soundOn = new ToggleButton();
        soundOn.setText("ON");
        soundOn.setId("soundOn");
        soundOn.setPrefWidth(50.0);
        soundOn.setMnemonicParsing(false);
        ToggleButton soundOff = new ToggleButton();
        soundOff.setText("OFF");
        soundOff.setPrefWidth(50.0);
        soundOff.setMnemonicParsing(false);
        toggleGroup.getToggles().addAll(soundOn, soundOff);
        toggleGroup.selectToggle(soundOff);

        // Label for toggle buttons
        Label soundLabel = new Label();
        soundLabel.setText("Sound");
        // Warning label for toggle buttons
        Label soundWarning = new Label();
        soundWarning.setText("Use with caution. Could be laggy and/or loud!");

        HBox.setMargin(soundLabel, new Insets(0, 0, 0, 20.0));
        HBox.setMargin(soundWarning, new Insets(0, 0, 0, 20.0));
        optionsSectionA.getChildren().addAll(spacingRegionA, soundOn, soundOff, soundLabel, soundWarning);
        /* ----------------------SECTION A---------------------- */

        /* ----------------------SECTION B---------------------- */
        // Region to space nodes out
        Region spacingRegionB = new Region();
        spacingRegionB.setPrefSize(50.0, 55.0);

        // Remove all Spheres Button
        Button clear = new Button();
        clear.setText("Clear Spheres");
        clear.setPrefWidth(100.0);
        clear.setMnemonicParsing(false);
        clear.setId("clear");
        clear.setOnAction(actionEvent -> {

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "This action cannot be undone. Proceed?", ButtonType.YES, ButtonType.NO);
            alert.setHeaderText("Warning!");
            alert.setTitle("Canvas Erasure");
            Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
            alertStage.getIcons().add(new Image("file:src/main/resources/assets/icon.png"));
            alert.showAndWait();

            if (alert.getResult() == ButtonType.YES) {
                // Iterate through all animations; stop all of them
                for (AnimationTimer animationTimer : animationList) {
                    animationTimer.stop();
                }
                /* Iterate through Sphere threads; interrupt all of them.
                stop() is deprecated, but interrupting them should free up resources
                once the garbage collector gets to it */
                for (Thread thread : threadList) {
                    thread.interrupt();
                }
                // Remove all instances of "Sphere" nodes from the canvas
                canvas.getChildren().removeIf(node -> node instanceof Sphere);
                // Clear all the ArrayLists
                threadList.clear();
                animationList.clear();
                sphereList.clear();
            }
        });

        Label removeLabel = new Label();
        removeLabel.setText("Remove all the spheres");

        HBox.setMargin(removeLabel, new Insets(0, 0, 0, 20.0));

        optionsSectionB.getChildren().addAll(spacingRegionB, clear, removeLabel);
        /* ----------------------SECTION B---------------------- */

        /* ----------------------SECTION C---------------------- */
        // Region to space nodes out
        Region spacingRegionC = new Region();
        spacingRegionC.setPrefSize(50.0, 55.0);

        // Terminate app Button. Needed since the app does not have the default ones available
        Button exit = new Button();
        exit.setText("Exit");
        exit.setMnemonicParsing(false);
        exit.setId("exit");
        exit.setPrefWidth(100.0);
        exit.setOnAction(actionEvent -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Terminating Application. Proceed?", ButtonType.YES, ButtonType.NO);
            alert.setHeaderText("Confirmation:");
            alert.setTitle("Bouncing Spheres");
            Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
            alertStage.getIcons().add(new Image("file:src/main/resources/assets/icon.png"));
            alert.showAndWait();

            if (alert.getResult() == ButtonType.YES) {
                // Update global values
                connection = Database.getConnection();
                if (connection != null) {
                    Database.updateSphereCount(connection);
                    Database.updateSphereCollisionCount(connection);
                    Database.updateWallCollisionCount(connection);
                }
                Database.closeConnection();
                Platform.exit();
            }
        });

        Label exitLabel = new Label();
        exitLabel.setText("Terminate the Application");

        HBox.setMargin(exitLabel, new Insets(0, 0, 0, 20.0));

        optionsSectionC.getChildren().addAll(spacingRegionC, exit, exitLabel);
        /* ----------------------SECTION C---------------------- */

        optionsContainer.getChildren().addAll(optionsSectionA, optionsSectionB, optionsSectionC);
        options.setContent(optionsContainer);

        return options;
    }

    /**
     * Generate the GUI elements of the "Stats" Tab.
     * @return the "Stats" Tab
     */
    public Tab generateStatsTab() {
        Tab stats = new Tab();
        stats.setText("Stats");

        VBox statsContainer = new VBox();
        statsContainer.setPrefSize(600.0, 180.0);
        ScrollPane statsContent = new ScrollPane();
        statsContent.setContent(statsContainer);
        statsContent.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        statsContent.setPrefSize(600.0, 180.0);

        // Divisions between session and global statistics
        VBox sessionDiv = new VBox();
        sessionDiv.setId("session");
        VBox globalDiv = new VBox();
        globalDiv.setId("global");

        // Sections for each row of the tab
        HBox statsSectionA = new HBox();
        HBox statsSectionB = new HBox();
        HBox statsSectionC = new HBox();
        HBox statsSectionD = new HBox();
        HBox statsSectionE = new HBox();
        HBox statsSectionF = new HBox();
        HBox statsSectionG = new HBox();
        HBox statsSectionH = new HBox();

        customizeBasicHBox(null, Pos.CENTER, 0.0, 600.0, statsSectionA);
        customizeBasicHBox("info-container", Pos.CENTER_LEFT, 0.0, 600.0, statsSectionB, statsSectionC, statsSectionD, statsSectionE, statsSectionF, statsSectionG, statsSectionH);

        // Local (session) counters (Not retrieved from DB)
        Text runningText = new Text("Spheres Currently Running: " + sphereList.size());
        runningText.getStyleClass().add("normal-text");

        Text sphereSessionText = new Text("Total Spheres This Session: " + sessionSphereCount);
        sphereSessionText.getStyleClass().add("normal-text");

        Text sphereCollisionSessionText = new Text("Sphere-to-Sphere Collisions This Session: " + sphereCollisionCount);
        sphereCollisionSessionText.getStyleClass().add("normal-text");

        Text wallCollisionSessionText = new Text("Sphere-to-Wall Collisions This Session: " + wallCollisionCount);
        wallCollisionSessionText.getStyleClass().add("normal-text");

        // Global (remote) counters

        // Retrieve global sphere count from DB
        retrieveGlobalValues();

        Text globalSphereText = new Text("Total Spheres Globally: " + globalSphereCount);
        globalSphereText.getStyleClass().add("normal-text");

        Text globalSphereCollisionText = new Text("Sphere-to-Sphere Collisions Globally: " + globalSphereCollisionCount);
        globalSphereCollisionText.getStyleClass().add("normal-text");

        Text globalWallCollisionText = new Text("Sphere-to-Wall Collisions Globally: " + globalWallCollisionCount);
        globalWallCollisionText.getStyleClass().add("normal-text");
        // Refresh button
        Button refresh = new Button("Refresh");
        refresh.setId("refresh");
        refresh.setOnAction(actionEvent -> {
            // Update session values
            runningText.setText("Spheres Currently Running: " + sphereList.size());
            sphereSessionText.setText("Total Spheres This Session: " + sessionSphereCount);
            sphereCollisionSessionText.setText("Sphere-to-Sphere Collisions This Session: " + sphereCollisionCount);
            wallCollisionSessionText.setText("Sphere-to-Wall Collisions This Session: " + wallCollisionCount);

            /* Update global values. Doesn't actually interact with the database
            This has been done to prevent 2 second freeze on button press, and limit the amount of
            database questions. Values are actually only updated remotely on application exit,
            thus limiting the amount of questions to less than 10 per session */
            globalSphereText.setText("Total Spheres Globally: " + (globalSphereCount + sessionSphereCount));
            globalSphereCollisionText.setText("Sphere-to-Sphere Collisions Globally: " + (globalSphereCollisionCount + sphereCollisionCount));
            globalWallCollisionText.setText("Sphere-to-Wall Collisions Globally: " + (globalWallCollisionCount + wallCollisionCount));
        });
        // Refresh stats on selection
        stats.setOnSelectionChanged(event -> refresh.fire());

        Region spacingRegionA = new Region();
        spacingRegionA.setMaxHeight(10.0);

        statsSectionA.getChildren().add(refresh);
        statsSectionB.getChildren().add(runningText);
        statsSectionC.getChildren().add(sphereSessionText);
        statsSectionD.getChildren().add(sphereCollisionSessionText);
        statsSectionE.getChildren().add(wallCollisionSessionText);
        statsSectionF.getChildren().add(globalSphereText);
        statsSectionG.getChildren().add(globalSphereCollisionText);
        statsSectionH.getChildren().add(globalWallCollisionText);

        sessionDiv.getChildren().addAll(statsSectionB, statsSectionC, statsSectionD, statsSectionE);
        globalDiv.getChildren().addAll(statsSectionF, statsSectionG, statsSectionH);
        statsContainer.getChildren().addAll(statsSectionA, sessionDiv, spacingRegionA, globalDiv);
        stats.setContent(statsContent);

        return stats;
    }

    /**
     * Retrieves the amount of spheres and collisions stored in the database
     */
    public void retrieveGlobalValues() {
        if (connection != null) {
            globalSphereCount = Database.retrieveSphereCount(connection);
            globalSphereCollisionCount = Database.retrieveSphereCollisionCount(connection);
            globalWallCollisionCount = Database.retrieveWallCollisionCount(connection);
        } else {
            globalSphereCount = -1;
            globalSphereCollisionCount = -1;
            globalWallCollisionCount = -1;
        }
    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Bridge.setCanvasController(this);
        canvas.getChildren().add(generateControlPanel());
    }
}