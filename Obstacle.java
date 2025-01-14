package com.example.robotgui;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.Stop;

/**
 * Static triangular obstacle in the arena.
 */
public class Obstacle extends ArenaItem {

    public Obstacle(double x, double y, double radius) {
        super(x, y, radius);
    }

    @Override
    public void update(RobotArena arena) {
        // Obstacles do not move
    }

    @Override
    public void draw(GraphicsContext gc) {
        // Create a gradient fill for a 3D effect
        LinearGradient gradient = new LinearGradient(
                0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.DARKGRAY),
                new Stop(1, Color.LIGHTGRAY)
        );

        // Set fill and stroke color
        gc.setFill(gradient);
        gc.setStroke(Color.BLACK);

        // Define the three points of the triangle
        double[] xPoints = {x, x - radius, x + radius};
        double[] yPoints = {y - radius, y + radius, y + radius};

        // Draw filled triangle
        gc.fillPolygon(xPoints, yPoints, 3);

        // Draw triangle border
        gc.strokePolygon(xPoints, yPoints, 3);
    }
}
