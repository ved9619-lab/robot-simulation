package com.example.robotgui;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * Abstract base class for all robot types with wheels.
 * Provides shared functionality for movement, obstacle avoidance, and boundary handling.
 */
public abstract class Robot extends ArenaItem {
    protected double angle; // Movement direction in radians
    protected double speed; // Movement speed

    /**
     * Constructs a robot with the specified position, size, direction, and speed.
     *
     * @param x      The x-coordinate of the robot's center.
     * @param y      The y-coordinate of the robot's center.
     * @param radius The radius of the robot.
     * @param angle  The initial movement direction in radians.
     * @param speed  The movement speed of the robot.
     */
    public Robot(double x, double y, double radius, double angle, double speed) {
        super(x, y, radius);
        this.angle = angle;
        this.speed = speed;
    }

    /**
     * Moves the robot in the current direction based on its speed and angle.
     */
    protected void move() {
        x += speed * Math.cos(angle); // Update x-coordinate
        y += speed * Math.sin(angle); // Update y-coordinate
    }

    /**
     * Handles collisions with arena boundaries by adjusting the robot's angle.
     *
     * @param arena The RobotArena for boundary detection.
     */
    protected void stayInArenaBounds(RobotArena arena) {
        // Reflect angle if the robot hits the left or right boundary
        if (x - radius < 0 || x + radius > arena.getWidth()) {
            angle = Math.PI - angle;
        }
        // Reflect angle if the robot hits the top or bottom boundary
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

                // If close to an obstacle, adjust angle to avoid collision
                if (distance < this.radius + item.radius + 10) {
                    angle += Math.PI / 2; // Turn 90 degrees to avoid the obstacle
                }
            }
        }
    }

    /**
     * Updates the robot's state. Must be implemented by subclasses to define specific behavior.
     *
     * @param arena The RobotArena containing all items.
     */
    @Override
    public abstract void update(RobotArena arena);

    /**
     * Draws the robot with its wheels.
     *
     * @param gc The GraphicsContext used for rendering.
     */
    @Override
    public void draw(GraphicsContext gc) {
        // Draw robot body
        gc.setFill(Color.BLUE);
        gc.fillOval(x - radius, y - radius, radius * 2, radius * 2); // Draw the circular body

        // Draw wheels
        double wheelRadius = radius / 4; // Radius of the wheels
        double wheelOffset = radius * 0.8; // Offset of the wheels from the center

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
