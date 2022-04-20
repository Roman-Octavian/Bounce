package com.bounce;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Sphere;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import java.io.File;

import java.util.Objects;
import java.util.Random;

/**
 * Class responsible for generating new spheres
 * Should not be named plain "Sphere" to avoid ambiguity with javafx.scene.shape.Sphere
 * Each sphere runs on its own thread
 * Includes the logic behind the rudimentary physics simulation.
 */
public class NewSphere extends Thread {

    // Retrieving the ColorPicker from the Control Panel with its ID, to color the Sphere
    ColorPicker color = (ColorPicker) Bridge.getCanvasController().getCanvas().getScene().lookup("#color");
    // Retrieving the size Slider from the Control Panel with its ID, to set the radius of the Sphere
    Slider size = (Slider) Bridge.getCanvasController().getCanvas().getScene().lookup("#size");
    // Retrieving the initialX TextField from the Control Panel with its ID, to set the initial position X of the Sphere
    TextField initialX = (TextField) Bridge.getCanvasController().getCanvas().getScene().lookup("#initialX");
    // Retrieving the initialY TextField from the Control Panel with its ID, to set the initial position Y of the Sphere
    TextField initialY = (TextField) Bridge.getCanvasController().getCanvas().getScene().lookup("#initialY");
    // Retrieving the X vector value from the Control Panel with its ID, to set the vector of the Sphere
    @SuppressWarnings("unchecked")
    Spinner<String> vectorX = (Spinner<String>) Bridge.getCanvasController().getCanvas().getScene().lookup("#initialVectorX");
    // Retrieving the Y vector value from the Control Panel with its ID, to set the vector of the Sphere
    @SuppressWarnings("unchecked")
    Spinner<String> vectorY = (Spinner<String>) Bridge.getCanvasController().getCanvas().getScene().lookup("#initialVectorY");
    // Retrieving the initialY TextField from the Control Panel with its ID, to set the initial position Y of the Sphere
    ToggleButton soundOn = (ToggleButton) Bridge.getCanvasController().getCanvas().getScene().lookup("#soundOn");


    // Instantiate a new JavaFX sphere object with a given radius
    Sphere sphere = new Sphere(size.getValue());

    // Random for blank values inside the control panel
    Random random = new Random();

    /**
     * Retrieves the values of the X,Y vector which determines the direction and speed of the sphere
     * Values are taken as integers, if any, from the spinners inside the control panel
     * If the "random" options are selected, then the value of the X or Y coordinate gets randomized between 1 and 10
     * @return an array with speed and direction "X" at index 0 and "Y" at index 1
     */
    private int[] getInitialSpeedAndDirection() {
        int x;
        int y;
        int[] result = new int[2];

        if (Objects.equals(vectorX.getValue(), "Random")) {
            x = random.nextInt(1,10);
        } else {
            x = Integer.parseInt(vectorX.getValue());
        }

        if (Objects.equals(vectorY.getValue(), "Random")) {
            y = random.nextInt(1,10);
        } else {
            y = Integer.parseInt(vectorY.getValue());
        }
        result[0] = x;
        result[1] = y;
        return result;
    }

    /**
     * Establish the initial position of a newly generated sphere.
     * Uses the values inserted into the "Initial Position" "X" and "Y" fields to position the sphere object.
     * If no values are provided, the position is randomized in accordance to the screen size.
     * @return an array with the initial X at index 0 and Y at index 1
     */
    private int[] getInitialCoords() {
        int x;
        int y;
        int[] result = new int[2];

        if (initialX.getText().equals("")) {
            x = random.nextInt((int) sphere.getRadius(), (int) Bridge.getCanvasController().getCanvas().getLayoutBounds().getMaxX());
        } else {
            x = Integer.parseInt(initialX.getText());
        }

        if (initialY.getText().equals("")) {
            y = random.nextInt((int) sphere.getRadius(), (int) Bridge.getCanvasController().getCanvas().getLayoutBounds().getMaxY());
        } else {
            y = Integer.parseInt(initialY.getText());
        }
        result[0] = x;
        result[1] = y;
        return result;
    }

    /**
     * Creates a context menu if sphere is right-clicked.
     * Currently, the menu only allows for deletion, but more functionality could be implemented.
     * @param sphere Sphere for which the context menu is supposed to be generated.
     * @param timer AnimationTimer to stop in case of sphere deletion.
     */
    private void setContextMenu(Sphere sphere, AnimationTimer timer) {
        ContextMenu menu = new ContextMenu();
        MenuItem delete = new MenuItem("Delete");
        delete.setOnAction(actionEvent -> {
            Bridge.getCanvasController().getSphereList().remove(sphere);
            Bridge.getCanvasController().getAnimationList().remove(timer);
            timer.stop();
            Bridge.getCanvasController().getCanvas().getChildren().remove(sphere);
        });
        menu.getItems().add(delete);
        sphere.setOnContextMenuRequested(contextMenuEvent -> {
            if (!menu.isShowing()) {
                menu.show(Bridge.getCanvasController().getCanvas(), sphere.getLayoutX(), sphere.getLayoutY());
            }
        });
    }

    /* This is the speed and direction in pixels per frame at which the sphere moves.
    We're assigning the values from the vector to two variables for clarity down the line */
    int[] vector = getInitialSpeedAndDirection();
    double directionX = vector[0];
    double directionY = vector[1];

    /**
     * Method to run a NewSphere thread
     */
    public void run() {
        /* Platform.runLater is necessary to use multi-threading in JavaFX. The framework does not normally allow direct Node
        manipulation on another thread that is not the one built into and designated for JavaFX. The threads must somehow "interact" with the JavaFX thread.
        Not using this when trying to run a new Thread results in: 'Exception in thread "Thread-3" java.lang.IllegalStateException: Not on FX application thread;'
        Thread-3 would be where we create the first Sphere. It starts at three because the other threads are being used by Java and FX.
        I was unable to find a workaround for Platform.runLater() */
        Platform.runLater(() -> {
            // Add sphere to the canvas
            Bridge.getCanvasController().getCanvas().getChildren().add(sphere);
            // Add sphere to a sphere ArrayList for future manipulation
            Bridge.getCanvasController().getSphereList().add(sphere);
            // Set the initial position of the Sphere on the axis
            sphere.setLayoutX(getInitialCoords()[0]);
            sphere.setLayoutY(getInitialCoords()[1]);
            // Create a material to color the Sphere, and take the color from the retrieved ColorPicker above
            PhongMaterial material = new PhongMaterial();
            material.setDiffuseColor(color.getValue());
            sphere.setMaterial(material);
            // Creating a new animation and starting it. JavaFX tries to run at least at 60 FPS but frames are not guaranteed — your mileage may vary.
            AnimationTimer timer = new Animation();
            timer.start();
            // Add animation to an animation ArrayList for future manipulation
            Bridge.getCanvasController().getAnimationList().add(timer);
            // Add context menu on right-click to sphere. Takes timer as argument too because it stops animation in case of deletion
            setContextMenu(sphere, timer);
            /* Set the sphere as last in the ObservableList of nodes in FX.
            This tries to prevent spheres from visually appearing on top of the control panel.
            Some very rare collisions under the controller have a small visual glitch where they appear for a fraction of a second, only saw it twice */
            sphere.toBack();
        });
    }

    /**
     * Inner class that will handle what happens during the animation
     */
    private class Animation extends AnimationTimer {
        // Whatever is inside the handle method of this AnimationTimer will run once per frame.
        @Override
        public void handle(long l) {
            // Directly running all the source code inside handle is not recommended. A separate method is more appropriate.
            doHandle();
        }
        /**
         * Handles the logic behind the behaviour of a Sphere at any given frame
         */
        private void doHandle() {
            // If the sphere has somehow managed to break out of bounds, force it back on the screen
            handleOutOfBounds();

            /* Four booleans representing each of the borders of the display. Should, in theory, work on any device.
            If the sphere is in contact, or beyond any of the bounds, its corresponding boolean will be true */
            boolean leftEdge = sphere.getLayoutX() <= (Bridge.getCanvasController().getCanvas().getLayoutBounds().getMinX() + sphere.getRadius());
            boolean rightEdge = sphere.getLayoutX() >= (Bridge.getCanvasController().getCanvas().getLayoutBounds().getMaxX() - sphere.getRadius());
            boolean lowerEdge = sphere.getLayoutY() >= (Bridge.getCanvasController().getCanvas().getLayoutBounds().getMaxY() - sphere.getRadius());
            boolean upperEdge = sphere.getLayoutY() <= (Bridge.getCanvasController().getCanvas().getLayoutBounds().getMinY() + sphere.getRadius());

            /* Invert the direction of the sphere if any of the bounds is being touched.
            Creates a "bouncing" effect */
            if (leftEdge || rightEdge) {
                directionX *= -1;
                // Play sound on impact. Off by default
                playSound("src/main/resources/assets/wall-collision.wav");
                // Update (local) session sphere-to-wall collision count
                Bridge.getCanvasController().setWallCollisionCount(Bridge.getCanvasController().getWallCollisionCount() + 1);
            }
            if (upperEdge || lowerEdge) {
                directionY *= -1;
                playSound("src/main/resources/assets/wall-collision.wav");
                Bridge.getCanvasController().setWallCollisionCount(Bridge.getCanvasController().getWallCollisionCount() + 1);
            }

            // If multiple spheres get into contact, prevent overlap and (hopefully) cause them to bounce
            handleCollision();

            // Lastly, move the sphere depending on its position and direction vector
            sphere.setLayoutX(sphere.getLayoutX() + directionX);
            sphere.setLayoutY(sphere.getLayoutY() + directionY);
        }

        /**
         * Push the sphere back to the screen if it somehow gets out of bounds */
        private void handleOutOfBounds() {
            if (sphere.getLayoutX() < Bridge.getCanvasController().getCanvas().getLayoutBounds().getMinX() + sphere.getRadius()) {
                sphere.setLayoutX(Bridge.getCanvasController().getCanvas().getLayoutBounds().getMinX() + sphere.getRadius());
            } else if (sphere.getLayoutX() > Bridge.getCanvasController().getCanvas().getLayoutBounds().getMaxX() - sphere.getRadius()) {
                sphere.setLayoutX(Bridge.getCanvasController().getCanvas().getLayoutBounds().getMaxX() - sphere.getRadius());
            } else if (sphere.getLayoutY() < Bridge.getCanvasController().getCanvas().getLayoutBounds().getMinY() + sphere.getRadius()) {
                sphere.setLayoutY(Bridge.getCanvasController().getCanvas().getLayoutBounds().getMinY() + sphere.getRadius());
            } else if (sphere.getLayoutY() > Bridge.getCanvasController().getCanvas().getLayoutBounds().getMaxY() - sphere.getRadius()) {
                sphere.setLayoutY(Bridge.getCanvasController().getCanvas().getLayoutBounds().getMaxY() - sphere.getRadius());
            }
        }

        /** This loop handles Sphere collision.
        * It iterates over the ArrayList of Spheres stored in the CanvasController.
        * Said ArrayList contains all the Spheres currently running on the screen */
        private void handleCollision() {
            for (Sphere otherSphere : Bridge.getCanvasController().getSphereList()) {
                // If the sphere is not itself
                if (sphere != otherSphere) {
                    // If there is a collision with the sphere over which we are iterating
                    if (checkCollision(sphere.getLayoutX(), sphere.getLayoutY(), sphere.getRadius(), otherSphere.getLayoutX(), otherSphere.getLayoutY(), otherSphere.getRadius())) {
                        /* This will handle potential overlap between spheres. Only runs if the two given spheres have somehow managed to go through one another,
                        which unfortunately happens quite often, as they travel a given amount of pixels each frame, and thus each frame has a chance to have them overlap at some point
                        Will forcefully "push" each sphere back from one another as to fix the overlap */
                        if (retrieveOverlap(sphere.getLayoutX(), sphere.getLayoutY(), sphere.getRadius(), otherSphere.getLayoutX(), otherSphere.getLayoutY(), otherSphere.getRadius()) < 0) {
                            // Distance between the center of each sphere in pixels
                            double distance = retrieveDistance(sphere.getLayoutX(), sphere.getLayoutY(), otherSphere.getLayoutX(), otherSphere.getLayoutY());
                            // Overlap between the spheres in pixels
                            double overlap = retrieveOverlap(sphere.getLayoutX(), sphere.getLayoutY(), sphere.getRadius(), otherSphere.getLayoutX(), otherSphere.getLayoutY(), otherSphere.getRadius());
                            // Forcefully push them back following the vector of their collision
                            sphere.setLayoutX(sphere.getLayoutX() - (overlap * (sphere.getLayoutX() - otherSphere.getLayoutX()) / distance));
                            sphere.setLayoutY(sphere.getLayoutY() - (overlap * (sphere.getLayoutY() - otherSphere.getLayoutY()) / distance));
                            otherSphere.setLayoutX(otherSphere.getLayoutX() + (overlap * (sphere.getLayoutX() - otherSphere.getLayoutX()) / distance));
                            otherSphere.setLayoutY(otherSphere.getLayoutY() + (overlap * (sphere.getLayoutY() - otherSphere.getLayoutY()) / distance));
                        }
                        // Finally, invert the direction vector to create a bounce effect
                        directionX *= -1;
                        directionY *= -1;

                        // Play sound on impact. Off by default
                        playSound("src/main/resources/assets/sphere-collision.wav");
                        // Update session sphere-on-sphere collision count
                        Bridge.getCanvasController().setSphereCollisionCount(Bridge.getCanvasController().getSphereCollisionCount() + 1);
                    }
                }
            }
        }

        /**
         * Checks for collision between two given spheres
         * @param sphereX Position of the first sphere centre on the X-axis
         * @param sphereY Position of the first sphere centre on the Y-axis
         * @param sphereRadius Radius of the first sphere
         * @param otherSphereX Position of the second sphere centre on the X-axis
         * @param otherSphereY Position of the second sphere centre on the Y-axis
         * @param otherSphereRadius Radius of the second sphere
         * @return true if the spheres are colliding, false otherwise
         */
        private boolean checkCollision(double sphereX, double sphereY, double sphereRadius,
                                       double otherSphereX, double otherSphereY, double otherSphereRadius) {
            // dx = vertical distance between sphere and other sphere
            // dy = horizontal distance between sphere and other sphere
            double dx = otherSphereX - sphereX;
            double dy = otherSphereY - sphereY;
            // d = distance between the centre of each sphere; Pythagoras' Theorem
            double d = Math.sqrt((dy * dy) + (dx * dx));
            // return true if the distance between the spheres is lower than their radius, false if not
            return (d <= (sphereRadius + otherSphereRadius));
        }

        /**
         * Retrieve the distance between the centres of two given spheres
         * @param sphereX Position of the first sphere on the X-axis
         * @param sphereY Position of the first sphere on the Y-axis
         * @param otherSphereX Position of the second sphere centre on the X-axis
         * @param otherSphereY Position of the second sphere centre on the Y-axis
         * @return the distance between the two centres, in pixels
         */
        private double retrieveDistance(double sphereX, double sphereY,
                                       double otherSphereX, double otherSphereY) {
            // dx = vertical distance between sphere and other sphere
            // dy = horizontal distance between sphere and other sphere
            double dx = otherSphereX - sphereX;
            double dy = otherSphereY - sphereY;
            // return distance between centres with Pythagoras' Theorem
            return Math.sqrt((dy * dy) + (dx * dx));
        }

        /**
         * Retrieve how much overlap, if any, there is between two given spheres
         * @param sphereX Position of the first sphere on the X-axis
         * @param sphereY Position of the first sphere on the Y-axis
         * @param sphereRadius Radius of the first sphere
         * @param otherSphereX Position of the second sphere centre on the X-axis
         * @param otherSphereY Position of the second sphere centre on the Y-axis
         * @param otherSphereRadius Radius of the second sphere
         * @return the amount of overlap for a sphere with another one in pixels
         */
        private double retrieveOverlap(double sphereX, double sphereY, double sphereRadius,
                                                double otherSphereX, double otherSphereY, double otherSphereRadius) {
            // dx = vertical distance between sphere and other sphere
            // dy = horizontal distance between sphere and other sphere
            double dx = otherSphereX - sphereX;
            double dy = otherSphereY - sphereY;
            // d = distance between the center of each sphere; Pythagoras' Theorem
            double d = Math.sqrt((dy * dy) + (dx * dx));
            // return overlap
            return (d - sphereRadius - otherSphereRadius) * 0.5;
        }
        /* It may seem as if the three methods above could be combined into one, and that is true,
        but I think it's cleaner this way, and, moreover, only the ones that are needed will be called;
        Having a do-everything method may hinder performance, which already, in of itself, is not that great */

        /**
         * Plays an audio if the sound toggle is on.
         * Used to make sound on sphere collisions with walls and other spheres
         * @param url path to the sound file that is to be played
         */
        private void playSound(String url) {
            if (soundOn.isSelected()) {
                // Sound for wall collision. Off by default, since the FX MediaPlayer is very bloated and drastically drops FPS
                // Weird, but after wrapping everything in "if" statements the performance has vastly improved (???)
                // Maybe I was loading the audios every frame * the number of balls by mistake
                Media sound = new Media(new File(url).toURI().toString());
                MediaPlayer mediaPlayer = new MediaPlayer(sound);
                mediaPlayer.play();
            }
        }
    }
}