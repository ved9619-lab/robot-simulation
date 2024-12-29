package com.example.robotgui;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * Abstract base class for all robot types.
 */
public abstract class Robot extends ArenaItem {
    protected double angle; // Movement direction in radians
    protected double speed; // Movement speed

    public Robot(double x, double y, double radius, double angle, double speed) {
        super(x, y, radius);
        this.angle = angle;
        this.speed = speed;
    }

    /**
     * Moves the robot in the current direction.
     */
    protected void move() {
        x += speed * Math.cos(angle);
        y += speed * Math.sin(angle);
    }

    /**
     * Handles collisions with arena boundaries and other objects.
     *
     * @param arena The RobotArena for collision detection.
     */
    protected void handleCollisions(RobotArena arena) {
        if (x - radius < 0 || x + radius > arena.getWidth()) angle = Math.PI - angle;
        if (y - radius < 0 || y + radius > arena.getHeight()) angle = -angle;

        for (ArenaItem item : arena.getItems()) {
            if (item != this && this.overlaps(item)) {
                angle += Math.PI / 2; // Change direction on collision
            }
        }
    }

    @Override
    public void update(RobotArena arena) {
        move();
        handleCollisions(arena);
    }

    @Override
    public void draw(GraphicsContext gc) {
        gc.setFill(Color.BLUE);
        gc.fillOval(x - radius, y - radius, radius * 2, radius * 2);
    }
}