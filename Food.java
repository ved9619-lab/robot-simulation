package com.example.robotgui;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * Represents a food item in the arena.
 * Food items are stationary and provide health/ energy score benefits to robots that collect them.
 */
public class Food extends ArenaItem {

    /**
     * Constructs a food item with the specified position and size.
     * @param x      The x-coordinate of the food's center.
     * @param y      The y-coordinate of the food's center.
     * @param radius The radius of the food item.
     */
    public Food(double x, double y, double radius) {
        super(x, y, radius);
    }

    /**
     * Draws the food item on the canvas.
     * The food is represented as a green circle.
     * @param gc The GraphicsContext used for rendering.
     */
    @Override
    public void draw(GraphicsContext gc) {
        gc.setFill(Color.GREEN); // Set the color to green for food
        gc.fillOval(x - radius, y - radius, radius * 2, radius * 2); // Draw the food as a circle
    }

    /**
     * Updates the state of the food item.
     * Food items do not have any behavior, so this method is intentionally left blank.
     * @param arena The arena containing all items.
     */
    @Override
    public void update(RobotArena arena) {
        // Food items are stationary and do not update their state.
    }
}
