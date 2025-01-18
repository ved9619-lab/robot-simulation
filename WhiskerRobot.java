package com.example.robotgui;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * A robot with whiskers that chases food and avoids obstacles.
 * This robot has energy that decreases over time and regains energy upon consuming food.
 */
public class WhiskerRobot extends Robot {
    private double whiskerLength; // Length of the robot's whiskers for detection
    private double energy; // Energy level of the robot

    /**
     * Constructs a WhiskerRobot with specified position, size, movement parameters, and whisker length.
     *
     * @param x             The x-coordinate of the robot's center.
     * @param y             The y-coordinate of the robot's center.
     * @param radius        The radius of the robot.
     * @param angle         The initial movement direction in radians.
     * @param speed         The speed of the robot.
     * @param whiskerLength The length of the robot's whiskers.
     */
    public WhiskerRobot(double x, double y, double radius, double angle, double speed, double whiskerLength) {
        super(x, y, radius, angle, speed);
        this.whiskerLength = whiskerLength;
        this.energy = 100; // Initial energy level
    }

    /**
     * Updates the robot's state, including energy management, movement, and interactions with food.
     *
     * @param arena The arena containing all items.
     */
    @Override
    public void update(RobotArena arena) {
        if (energy <= 0) {
            arena.removeItem(this); // Remove the robot if energy is depleted
            return;
        }

        // Reduce energy over time
        energy -= 0.05;

        // Move towards the nearest food item
        ArenaItem nearestFood = findNearestFood(arena);
        if (nearestFood != null) {
            double dx = nearestFood.x - this.x;
            double dy = nearestFood.y - this.y;
            this.angle = Math.atan2(dy, dx); // Adjust angle to move toward food

            // Absorb food if overlapping
            if (this.overlaps(nearestFood)) {
                arena.removeItem(nearestFood); // Remove the food item
                energy = Math.min(energy + 20, 100); // Regain energy, capped at 100
            }
        }

        move();
        avoidObstacles(arena); // Avoid obstacles in the arena
        stayInArenaBounds(arena); // Ensure robot stays within the arena boundaries
    }

    /**
     * Draws the robot, including its whiskers and energy level.
     *
     * @param gc The GraphicsContext used for rendering.
     */
    @Override
    public void draw(GraphicsContext gc) {
        // Draw robot body and wheels
        super.draw(gc);

        // Draw whiskers
        gc.setStroke(Color.RED); // Set whisker color to red
        double whiskerAngle = Math.PI / 8; // Angle between whiskers and the robot's direction
        gc.strokeLine(x, y, x + whiskerLength * Math.cos(angle - whiskerAngle),
                y + whiskerLength * Math.sin(angle - whiskerAngle)); // Left whisker
        gc.strokeLine(x, y, x + whiskerLength * Math.cos(angle + whiskerAngle),
                y + whiskerLength * Math.sin(angle + whiskerAngle)); // Right whisker

        // Draw energy level below the robot
        gc.setFill(Color.BLACK);
        gc.fillText(String.format("Energy: %.1f", energy), x - radius, y + radius + 10);
    }

    /**
     * Finds the nearest food item in the arena.
     *
     * @param arena The arena to search for food.
     * @return The nearest food item, or null if no food is found.
     */
    private ArenaItem findNearestFood(RobotArena arena) {
        ArenaItem nearest = null;
        double nearestDistance = Double.MAX_VALUE;

        for (ArenaItem item : arena.getItems()) {
            if (item instanceof Food) { // Check if the item is a food object
                double distance = Math.sqrt(Math.pow(item.x - this.x, 2) + Math.pow(item.y - this.y, 2));
                if (distance < nearestDistance) { // Update nearest food if this one is closer
                    nearestDistance = distance;
                    nearest = item;
                }
            }
        }
        return nearest;
    }
}
