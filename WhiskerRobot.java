package com.example.robotgui;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * A robot with whiskers acting as sensors and black wheels.
 */
public class WhiskerRobot extends Robot {
    private double whiskerLength;
    private double detectionRange;
    private double time; // Keeps track of time for beam oscillation

    public WhiskerRobot(double x, double y, double radius, double angle, double speed, double whiskerLength) {
        super(x, y, radius, angle, speed);
        this.whiskerLength = whiskerLength;
        this.detectionRange = radius * 3;
        this.time = 0;
    }

    @Override
    public void update(RobotArena arena) {
        time += 0.1; // Increment time for oscillation calculation

        // Avoid collisions with obstacles and other bots
        avoidObstacles(arena);
        avoidOtherBots(arena);

        // Find the nearest food
        Food nearestFood = findNearestFood(arena);

        if (nearestFood != null) {
            // Calculate direction toward the nearest food
            double dx = nearestFood.x - this.x;
            double dy = nearestFood.y - this.y;
            double targetAngle = Math.atan2(dy, dx);

            // Apply beam-like oscillation around the target angle
            double oscillation = Math.sin(time) * 0.3; // Oscillate angle by ±0.3 radians
            this.angle = targetAngle + oscillation;

            // Check if the robot overlaps with the food (absorbs it)
            double distanceToFood = Math.sqrt(Math.pow(nearestFood.x - this.x, 2) + Math.pow(nearestFood.y - this.y, 2));
            if (distanceToFood < this.radius + nearestFood.radius) {
                arena.removeItem(nearestFood); // Remove the food from the arena
                this.speed += 0.1; // Increase speed slightly as a reward
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

    /**
     * Avoids collisions with other prey bots by changing direction smoothly.
     *
     * @param arena The RobotArena to check for other bots.
     */
    private void avoidOtherBots(RobotArena arena) {
        for (ArenaItem item : arena.getItems()) {
            if (item != this && item instanceof WhiskerRobot) {
                double distance = Math.sqrt(Math.pow(item.x - this.x, 2) + Math.pow(item.y - this.y, 2));

                // Check if another bot is within the collision range
                if (distance < this.radius * 3) { // Increased detection range to 3 times the radius
                    // Calculate direction away from the other bot
                    double dx = item.x - this.x;
                    double dy = item.y - this.y;
                    double botAngle = Math.atan2(dy, dx);

                    // If very close, change direction sharply and add randomness
                    if (distance < this.radius * 1.5) {
                        this.angle += Math.PI + (Math.random() * 0.5 - 0.25); // Turn around with randomness
                        this.x -= Math.cos(botAngle) * 3;
                        this.y -= Math.sin(botAngle) * 3;
                    } else {
                        // Smoothly adjust the angle to avoid the other bot
                        this.angle += (botAngle > this.angle ? -0.1 : 0.1);
                    }
                    break;
                }
            }
        }
    }

    /**
     * Finds the nearest unclaimed food item in the arena.
     *
     * @param arena The RobotArena to search for food.
     * @return The nearest unclaimed food item, or null if no unclaimed food is found.
     */
    private Food findNearestFood(RobotArena arena) {
        Food nearestFood = null;
        double nearestDistance = Double.MAX_VALUE;

        for (ArenaItem item : arena.getItems()) {
            if (item instanceof Food) { // Check for food items only
                double distance = Math.sqrt(Math.pow(item.x - this.x, 2) + Math.pow(item.y - this.y, 2));
                if (distance < nearestDistance) {
                    nearestDistance = distance;
                    nearestFood = (Food) item;
                }
            }
        }

        return nearestFood;
    }

    @Override
    public void draw(GraphicsContext gc) {
        // Draw robot body with a black outline
        gc.setFill(Color.DODGERBLUE);
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

        // Draw whiskers (ultrasonic sensors)
        gc.setStroke(Color.RED);
        double whiskerAngle = Math.PI / 8;
        gc.strokeLine(x, y, x + whiskerLength * Math.cos(angle - whiskerAngle),
                y + whiskerLength * Math.sin(angle - whiskerAngle)); // Left whisker
        gc.strokeLine(x, y, x + whiskerLength * Math.cos(angle + whiskerAngle),
                y + whiskerLength * Math.sin(angle + whiskerAngle)); // Right whisker
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
