package com.example.robotgui;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * A predator robot that chases and eats prey bots, increasing in size and slowing down.With predator eyes and fangs.
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
        super(x, y, radius, angle, speed * 1.75); // Increase speed by 50%
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
        avoidCollisions(arena); // Handle predator collision avoidance
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
        // Draw predator robot body
        gc.setFill(Color.DARKRED);
        gc.fillOval(x - radius, y - radius, radius * 2, radius * 2);

        // Draw predator outline
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2);
        gc.strokeOval(x - radius, y - radius, radius * 2, radius * 2);

        // Draw predator eyes
        gc.setFill(Color.YELLOW);
        double eyeSize = radius / 4;
        gc.fillOval(x - radius / 2, y - radius / 3, eyeSize, eyeSize); // Left eye
        gc.fillOval(x + radius / 4, y - radius / 3, eyeSize, eyeSize); // Right eye

        // Add fangs
        gc.setFill(Color.WHITE);
        double fangWidth = radius / 6;
        double fangHeight = radius / 3;
        gc.fillPolygon(new double[]{x - radius / 3, x - fangWidth / 2, x - fangWidth},
                new double[]{y + radius / 2, y + radius / 2 + fangHeight, y + radius / 2}, 3); // Left fang
        gc.fillPolygon(new double[]{x + radius / 3, x + fangWidth / 2, x + fangWidth},
                new double[]{y + radius / 2, y + radius / 2 + fangHeight, y + radius / 2}, 3); // Right fang

        // Draw wheels aligned with movement direction on left and right sides
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(5);
        double wheelLength = 20; // Length of the wheel
        double offsetX = Math.cos(angle + Math.PI / 2) * radius; // X-offset for left/right wheels
        double offsetY = Math.sin(angle + Math.PI / 2) * radius; // Y-offset for left/right wheels
        double perpendicularX = Math.cos(angle) * wheelLength / 2;
        double perpendicularY = Math.sin(angle) * wheelLength / 2;

        // Left wheel
        double wheelXLeftStart = x - offsetX - perpendicularX;
        double wheelYLeftStart = y - offsetY - perpendicularY;
        double wheelXLeftEnd = x - offsetX + perpendicularX;
        double wheelYLeftEnd = y - offsetY + perpendicularY;
        gc.strokeLine(wheelXLeftStart, wheelYLeftStart, wheelXLeftEnd, wheelYLeftEnd);

        // Right wheel
        double wheelXRightStart = x + offsetX - perpendicularX;
        double wheelYRightStart = y + offsetY - perpendicularY;
        double wheelXRightEnd = x + offsetX + perpendicularX;
        double wheelYRightEnd = y + offsetY + perpendicularY;
        gc.strokeLine(wheelXRightStart, wheelYRightStart, wheelXRightEnd, wheelYRightEnd);

        // Draw health level below the robot
        gc.setFill(Color.BLACK);
        gc.fillText(String.format("Health: %.1f", health), x - radius, y + radius + 20);
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

    /**
     * Avoid collisions with other predators in the arena.
     *
     * @param arena The arena containing all items.
     */
    private void avoidCollisions(RobotArena arena) {
        for (ArenaItem item : arena.getItems()) {
            if (item instanceof PredatorRobot && item != this) {
                double dx = this.x - item.x;
                double dy = this.y - item.y;
                double distance = Math.sqrt(dx * dx + dy * dy);

                // If overlapping, adjust position to avoid collision
                if (distance < this.radius + item.radius) {
                    double overlap = (this.radius + item.radius) - distance;
                    double angleAway = Math.atan2(dy, dx);

                    // Move this predator slightly away
                    this.x += Math.cos(angleAway) * overlap / 2;
                    this.y += Math.sin(angleAway) * overlap / 2;

                    // Move the other predator slightly away
                    item.x -= Math.cos(angleAway) * overlap / 2;
                    item.y -= Math.sin(angleAway) * overlap / 2;
                }
            }
        }
    }
}
