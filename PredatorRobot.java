package com.example.robotgui;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * A predator robot that chases and eats prey bots, increasing in size and slowing down.
 */
public class PredatorRobot extends Robot {
    private double health; // Health level of the predator bot

    /**
     * Constructs a predator robot with the specified attributes.
     *
     * @param x      The x-coordinate of the predator's center.
     * @param y      The y-coordinate of the predator's center.
     * @param radius The radius of the predator robot.
     * @param angle  The initial angle of movement.
     * @param speed  The speed of the predator robot.
     */
    public PredatorRobot(double x, double y, double radius, double angle, double speed) {
        super(x, y, radius, angle, speed);
        this.health = 100; // Initial health level
    }

    /**
     * Updates the predator robot's state.
     * Handles health reduction over time, movement, prey consumption, and obstacle avoidance.
     *
     * @param arena The arena containing all items.
     */
    @Override
    public void update(RobotArena arena) {
        if (health <= 0) {
            arena.removeItem(this); // Remove the bot if health is zero
            return;
        }

        // Reduce health over time
        health -= 0.05;

        // Find the nearest prey bot
        ArenaItem nearestPrey = findNearestPrey(arena);
        if (nearestPrey != null) {
            double dx = nearestPrey.x - this.x;
            double dy = nearestPrey.y - this.y;
            this.angle = Math.atan2(dy, dx); // Adjust angle to move toward the prey

            // Eat prey if overlapping
            if (this.overlaps(nearestPrey)) {
                arena.removeItem(nearestPrey);
                health = Math.min(health + 30, 100); // Regain health, max 100
                this.radius += 2; // Increase size slightly
            }
        }

        move();
        avoidObstacles(arena); // Handle obstacle avoidance
        stayInArenaBounds(arena); // Ensure predator stays within bounds
    }

    /**
     * Draws the predator robot and its health level.
     *
     * @param gc The GraphicsContext used for rendering.
     */
    @Override
    public void draw(GraphicsContext gc) {
        // Draw predator robot with wheels
        super.draw(gc);

        // Draw health level below the robot
        gc.setFill(Color.BLACK);
        gc.fillText(String.format("Health: %.1f", health), x - radius, y + radius + 10);
    }

    /**
     * Finds the nearest prey bot in the arena.
     *
     * @param arena The arena to search for prey.
     * @return The nearest prey bot, or null if no prey is found.
     */
    private ArenaItem findNearestPrey(RobotArena arena) {
        ArenaItem nearest = null;
        double nearestDistance = Double.MAX_VALUE;

        for (ArenaItem item : arena.getItems()) {
            if (item instanceof WhiskerRobot) { // Identify prey bots
                double distance = Math.sqrt(Math.pow(item.x - this.x, 2) + Math.pow(item.y - this.y, 2));
                if (distance < nearestDistance) {
                    nearestDistance = distance;
                    nearest = item;
                }
            }
        }
        return nearest;
    }
}
