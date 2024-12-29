package com.example.robotgui;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * Static obstacle in the arena.
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
        gc.setFill(Color.RED);
        gc.fillOval(x - radius, y - radius, radius * 2, radius * 2);
    }
}
