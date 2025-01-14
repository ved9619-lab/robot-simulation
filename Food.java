package com.example.robotgui;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * Represents a food item in the arena.
 */
public class Food extends ArenaItem {

    public Food(double x, double y, double radius) {
        super(x, y, radius);
    }

    @Override
    public void draw(GraphicsContext gc) {
        gc.setFill(Color.GREEN);
        gc.fillOval(x - radius, y - radius, radius * 2, radius * 2);
    }

    @Override
    public void update(RobotArena arena) {
        // Food items do not move or update state
    }
}
