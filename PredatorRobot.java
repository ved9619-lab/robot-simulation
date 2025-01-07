package com.example.robotgui;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * A predator robot that chases and eats other robots, increasing in size and slowing down.
 */
public class PredatorRobot extends Robot {

    public PredatorRobot(double x, double y, double radius, double angle, double speed) {
        super(x, y, radius, angle, speed);
    }

    @Override
    public void update(RobotArena arena) {
        // Find the nearest robot
        ArenaItem nearestRobot = findNearestRobot(arena);

        if (nearestRobot != null) {
            double dx = nearestRobot.x - this.x;
            double dy = nearestRobot.y - this.y;
            this.angle = Math.atan2(dy, dx); // Update angle to point toward the nearest robot

            // Check if predator overlaps with the nearest robot
            if (this.overlaps(nearestRobot)) {
                arena.removeItem(nearestRobot); // Remove the eaten robot from the arena
                this.radius += 5; // Increase size of predator
                this.speed = Math.max(0.2, this.speed * 0.8); // Significantly reduce speed with a minimum limit
            }
        }

        // Move the predator robot
        move();

        // Ensure the robot stays inside the arena
        stayInArenaBounds(arena);

        // Handle collisions with obstacles
        handleCollisions(arena);
    }

    @Override
    public void draw(GraphicsContext gc) {
        // Draw predator robot with wheels
        super.draw(gc);

        // Indicate predator with a red border
        gc.setStroke(Color.RED);
        gc.strokeOval(x - radius, y - radius, radius * 2, radius * 2);
    }

    /**
     * Finds the nearest normal robot in the arena.
     *
     * @param arena The arena to search for robots.
     * @return The nearest robot, or null if no robots are found.
     */
    private ArenaItem findNearestRobot(RobotArena arena) {
        ArenaItem nearest = null;
        double nearestDistance = Double.MAX_VALUE;

        for (ArenaItem item : arena.getItems()) {
            if (item instanceof WhiskerRobot) { // Only chase normal robots
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
     * Ensures the predator stays within the arena boundaries.
     *
     * @param arena The arena to keep the robot inside.
     */
    private void stayInArenaBounds(RobotArena arena) {
        if (x - radius < 0) {
            x = radius;
            angle = Math.PI - angle;
        }
        if (x + radius > arena.getWidth()) {
            x = arena.getWidth() - radius;
            angle = Math.PI - angle;
        }
        if (y - radius < 0) {
            y = radius;
            angle = -angle;
        }
        if (y + radius > arena.getHeight()) {
            y = arena.getHeight() - radius;
            angle = -angle;
        }
    }
}
