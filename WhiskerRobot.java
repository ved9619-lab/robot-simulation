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
    private static final double SAFETY_MARGIN = 5.0; // Margin to avoid close collisions

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

        // Check if either whisker detects an obstacle or object
        if (isWhiskerTouching(arena, -Math.PI / 8) || isWhiskerTouching(arena, Math.PI / 8)) {
            angle += Math.PI / 2; // Immediately change direction by turning 90 degrees
        } else {
            // Reduce energy over time
            energy -= 0.05;

            // Move towards the nearest food item
            ArenaItem nearestFood = findNearestFood(arena);
            if (nearestFood != null) {
                double dx = nearestFood.x - this.x;
                double dy = nearestFood.y - this.y;
                this.angle = Math.atan2(dy, dx); // Adjust angle to move toward food

                // Check and absorb food if overlapping
                if (this.isOverlapping(nearestFood)) {
                    arena.removeItem(nearestFood); // Remove the food item
                    energy = Math.min(energy + 20, 100); // Regain energy, capped at 100
                }
            }
        }

        move(); // Continue movement
        stayInArenaBounds(arena); // Ensure robot stays within the arena boundaries
    }

    /**
     * Ensures the robot stays within the boundaries of the arena.
     *
     * @param arena The arena containing all items.
     */
    public void stayInArenaBounds(RobotArena arena) {
        double arenaWidth = arena.getWidth();
        double arenaHeight = arena.getHeight();

        // Check horizontal bounds
        if (x - radius < 0) {
            x = radius; // Prevent going out of the left boundary
            angle = Math.PI - angle; // Reflect angle horizontally
        } else if (x + radius > arenaWidth) {
            x = arenaWidth - radius; // Prevent going out of the right boundary
            angle = Math.PI - angle; // Reflect angle horizontally
        }

        // Check vertical bounds
        if (y - radius < 0) {
            y = radius; // Prevent going out of the top boundary
            angle = -angle; // Reflect angle vertically
        } else if (y + radius > arenaHeight) {
            y = arenaHeight - radius; // Prevent going out of the bottom boundary
            angle = -angle; // Reflect angle vertically
        }
    }

    /**
     * Draws the robot, including its whiskers and energy level.
     *
     * @param gc The GraphicsContext used for rendering.
     */
    @Override
    public void draw(GraphicsContext gc) {
        // Draw robot body
        gc.setFill(Color.TURQUOISE); // Use a distinct color for WhiskerRobot
        gc.fillOval(x - radius, y - radius, radius * 2, radius * 2);

        // Draw robot outline
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2);
        gc.strokeOval(x - radius, y - radius, radius * 2, radius * 2);

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
        double wheelYRightEnd = y + perpendicularY + offsetY;
        gc.strokeLine(wheelXRightStart, wheelYRightStart, wheelXRightEnd, wheelYRightEnd);

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

    /**
     * Checks if the whisker is touching any other arena item or the arena walls.
     *
     * @param arena         The arena containing all items.
     * @param whiskerOffset The angle offset for the whisker (-Math.PI / 8 for left, Math.PI / 8 for right).
     * @return True if the whisker is touching any arena item or the walls, false otherwise.
     */
    private boolean isWhiskerTouching(RobotArena arena, double whiskerOffset) {
        double precisionStep = 2.0; // Distance between sampled points along the whisker for higher precision
        for (double length = 0; length <= whiskerLength; length += precisionStep) {
            // Calculate the position of the current point along the whisker
            double whiskerX = x + length * Math.cos(angle + whiskerOffset);
            double whiskerY = y + length * Math.sin(angle + whiskerOffset);

            // Check collision with arena walls
            if (whiskerX - SAFETY_MARGIN < 0 || whiskerX + SAFETY_MARGIN > arena.getWidth() ||
                    whiskerY - SAFETY_MARGIN < 0 || whiskerY + SAFETY_MARGIN > arena.getHeight()) {
                return true; // Collision with wall detected
            }

            // Check collision with other items in the arena
            for (ArenaItem item : arena.getItems()) {
                if (item == this) continue; // Skip self-collision check

                // Check if the current point on the whisker overlaps with the item
                double dx = whiskerX - item.x;
                double dy = whiskerY - item.y;
                double distance = Math.sqrt(dx * dx + dy * dy);

                if (distance < item.radius + SAFETY_MARGIN) { // Include safety margin
                    return true; // Collision with item detected
                }
            }
        }
        return false; // No collisions detected
    }

    /**
     * Checks if the robot is overlapping with the given item.
     *
     * @param item The item to check for overlap.
     * @return True if the robot overlaps with the item, false otherwise.
     */
    private boolean isOverlapping(ArenaItem item) {
        double dx = this.x - item.x;
        double dy = this.y - item.y;
        double distance = Math.sqrt(dx * dx + dy * dy);
        return distance < (this.radius + item.radius);
    }
}
