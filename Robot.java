package com.example.robotgui;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * Abstract base class for all robot types with wheels.
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
     * Handles collisions with arena boundaries.
     *
     * @param arena The RobotArena for boundary detection.
     */
    protected void stayInArenaBounds(RobotArena arena) {
        if (x - radius < 0 || x + radius > arena.getWidth()) {
            angle = Math.PI - angle;
        }
        if (y - radius < 0 || y + radius > arena.getHeight()) {
            angle = -angle;
        }
    }

    /**
     * Avoids nearby obstacles by changing direction when close to an obstacle.
     *
     * @param arena The RobotArena for obstacle detection.
     */
    protected void avoidObstacles(RobotArena arena) {
        for (ArenaItem item : arena.getItems()) {
            if (item instanceof Obstacle) {
                double dx = item.x - this.x;
                double dy = item.y - this.y;
                double distance = Math.sqrt(dx * dx + dy * dy);

                if (distance < this.radius + item.radius + 10) { // If close to an obstacle
                    angle += Math.PI / 2; // Turn 90 degrees to avoid the obstacle
                }
            }
        }
    }

    @Override
    public abstract void update(RobotArena arena);

    @Override
    public void draw(GraphicsContext gc) {
        // Draw robot body
        gc.setFill(Color.BLUE);
        gc.fillOval(x - radius, y - radius, radius * 2, radius * 2);

        // Draw wheels
        double wheelRadius = radius / 4;
        double wheelOffset = radius * 0.8;

        gc.setFill(Color.BLACK);
        // Left wheel
        gc.fillOval(x - wheelOffset * Math.cos(angle + Math.PI / 2) - wheelRadius,
                y - wheelOffset * Math.sin(angle + Math.PI / 2) - wheelRadius,
                wheelRadius * 2, wheelRadius * 2);
        // Right wheel
        gc.fillOval(x - wheelOffset * Math.cos(angle - Math.PI / 2) - wheelRadius,
                y - wheelOffset * Math.sin(angle - Math.PI / 2) - wheelRadius,
                wheelRadius * 2, wheelRadius * 2);
    }
}
