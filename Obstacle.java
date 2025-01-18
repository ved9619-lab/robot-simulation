package com.example.robotgui;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.Stop;

/**
 * Represents a static triangular obstacle in the arena.
 * Obstacles do not move but serve as barriers or challenges for robots in the arena.
 */
public class Obstacle extends ArenaItem {

    /**
     * Constructs an obstacle with the specified position and size.
     *
     * @param x      The x-coordinate of the obstacle's center.
     * @param y      The y-coordinate of the obstacle's center.
     * @param radius The radius defining the size of the obstacle.
     */
    public Obstacle(double x, double y, double radius) {
        super(x, y, radius);
    }

    /**
     * Updates the state of the obstacle.
     * Obstacles are stationary, so this method does not change their state.
     *
     * @param arena The arena containing all items.
     */
    @Override
    public void update(RobotArena arena) {
        // Obstacles do not move or update their state.
    }

    /**
     * Draws the obstacle as a triangular shape with a gradient for a 3D effect.
     *
     * @param gc The GraphicsContext used for rendering.
     */
    @Override
    public void draw(GraphicsContext gc) {
        // Create a gradient fill for a 3D effect
        LinearGradient gradient = new LinearGradient(
                0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.DARKGRAY), // Start color of the gradient
                new Stop(1, Color.LIGHTGRAY) // End color of the gradient
        );

        // Set fill and stroke color
        gc.setFill(gradient); // Apply gradient fill to the obstacle
        gc.setStroke(Color.BLACK); // Outline color for the triangle

        // Define the three points of the triangle
        double[] xPoints = {x, x - radius, x + radius}; // X-coordinates of the triangle's vertices
        double[] yPoints = {y - radius, y + radius, y + radius}; // Y-coordinates of the triangle's vertices

        // Draw filled triangle
        gc.fillPolygon(xPoints, yPoints, 3); // Fill the triangle with the gradient

        // Draw triangle border
        gc.strokePolygon(xPoints, yPoints, 3); // Draw the triangle's outline
    }
}
