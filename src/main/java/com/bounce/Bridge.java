package com.bounce;

/**
 * "Bridges" the gap between the "NewSphere" class and the CanvasController.
 * As JavaFX nodes are dynamic private attributes, they cannot be directly accessed outside their class.
 * Using Getters directly cannot be done either.
 */
public class Bridge {
    private static CanvasController canvasController = null;

    // Getter
    public static CanvasController getCanvasController() {
        return canvasController;
    }

    // Setter
    public static void setCanvasController(CanvasController canvasController) {
        Bridge.canvasController = canvasController;
    }
}
