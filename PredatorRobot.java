package com.example.robotgui;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * A predator robot that chases and eats prey bots, with black wheels and obstacle avoidance.
 */
public class PredatorRobot extends Robot {
    private double detectionRange;

    public PredatorRobot(double x, double y, double radius, double angle, double speed) {
        super(x, y, radius, angle, speed);
        this.detectionRange = radius * 2.5; // Detection range for obstacles
    }

    @Override
    public void update(RobotArena arena) {
        // Avoid collisions with obstacles
        avoidObstacles(arena);

        // Find the nearest prey (WhiskerRobot)
        WhiskerRobot nearestPrey = findNearestPrey(arena);

        if (nearestPrey != null) {
            // Calculate direction toward the nearest prey
            double dx = nearestPrey.x - this.x;
            double dy = nearestPrey.y - this.y;
            this.angle = Math.atan2(dy, dx); // Update angle to point toward the prey

            // Check if predator overlaps with the prey (eats it)
            if (this.overlaps(nearestPrey)) {
                arena.removeItem(nearestPrey); // Remove the prey from the arena (eaten)
                this.radius += 2; // Increase predator's size slightly
                this.speed = Math.max(0.5, this.speed * 0.9); // Reduce speed slightly, with a minimum limit
            }
        }

        // Continue normal movement and collision handling
        super.update(arena);
    }

    /**
     * Avoids collisions with obstacles by changing direction smoothly.
     *
     * @param arena The RobotArena to check for obstacles.
     */
    private void avoidObstacles(RobotArena arena) {
        for (ArenaItem item : arena.getItems()) {
            if (item instanceof Obstacle) {
                double distance = Math.sqrt(Math.pow(item.x - this.x, 2) + Math.pow(item.y - this.y, 2));

                // Check if an obstacle is within the detection range
                if (distance < detectionRange) {
                    // Calculate direction away from the obstacle
                    double dx = item.x - this.x;
                    double dy = item.y - this.y;
                    double obstacleAngle = Math.atan2(dy, dx);

                    // Smoothly adjust the angle to avoid the obstacle
                    this.angle += (obstacleAngle > this.angle ? -0.1 : 0.1);
                    break;
                }
            }
        }
    }

    @Override
    public void draw(GraphicsContext gc) {
        // Draw predator body with a black outline
        gc.setFill(Color.CRIMSON);
        gc.fillOval(x - radius, y - radius, radius * 2, radius * 2);
        gc.setStroke(Color.BLACK);
        gc.strokeOval(x - radius, y - radius, radius * 2, radius * 2); // Border outline

        // Draw black wheels
        double wheelRadius = radius / 3;
        double wheelOffset = radius * 0.8;

        // Left wheel
        drawWheel(gc, x - wheelOffset * Math.cos(angle + Math.PI / 2), y - wheelOffset * Math.sin(angle + Math.PI / 2), wheelRadius);

        // Right wheel
        drawWheel(gc, x - wheelOffset * Math.cos(angle - Math.PI / 2), y - wheelOffset * Math.sin(angle - Math.PI / 2), wheelRadius);
    }

    /**
     * Finds the nearest prey (WhiskerRobot) in the arena.
     *
     * @param arena The RobotArena to search for prey.
     * @return The nearest prey, or null if no prey is found.
     */
    private WhiskerRobot findNearestPrey(RobotArena arena) {
        WhiskerRobot nearestPrey = null;
        double nearestDistance = Double.MAX_VALUE;

        for (ArenaItem item : arena.getItems()) {
            if (item instanceof WhiskerRobot) { // Only target prey (WhiskerRobot)
                double distance = Math.sqrt(Math.pow(item.x - this.x, 2) + Math.pow(item.y - this.y, 2));
                if (distance < nearestDistance) {
                    nearestDistance = distance;
                    nearestPrey = (WhiskerRobot) item;
                }
            }
        }

        return nearestPrey;
    }

    /**
     * Draws a wheel with a black color and border.
     */
    private void drawWheel(GraphicsContext gc, double wheelX, double wheelY, double wheelRadius) {
        // Draw main wheel
        gc.setFill(Color.BLACK);
        gc.fillOval(wheelX - wheelRadius, wheelY - wheelRadius, wheelRadius * 2, wheelRadius * 2);

        // Draw wheel border
        gc.setStroke(Color.BLACK);
        gc.strokeOval(wheelX - wheelRadius, wheelY - wheelRadius, wheelRadius * 2, wheelRadius * 2);
    }
}
