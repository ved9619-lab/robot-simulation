package com.example.robotgui;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * A robot with a beam sensor that detects objects in its path.
 */
public class BeamSensorRobot extends Robot {
    private static final double TURN_ANGLE = Math.PI / 4; // 45 degrees
    private static final double DETECTION_ANGLE = Math.PI / 6; // 30 degrees
    private double sensorRange; // Range of the beam sensor

    public BeamSensorRobot(double x, double y, double radius, double angle, double speed, double sensorRange) {
        super(x, y, radius, angle, speed);
        this.sensorRange = sensorRange;
    }

    @Override
    public void update(RobotArena arena) {
        move();

        ArenaItem detectedItem = detectItemInPath(arena);
        if (detectedItem != null) {
            if (detectedItem instanceof Obstacle) {
                handleDetectedObstacle();
            } else if (detectedItem instanceof Food) {
                moveTowardFood(detectedItem, arena);
            }
        }

        avoidObstacles(arena);
        stayInArenaBounds(arena);
    }

    private void handleDetectedObstacle() {
        angle += TURN_ANGLE; // Turn away from obstacle
    }

    private void moveTowardFood(ArenaItem food, RobotArena arena) {
        double dx = food.x - this.x;
        double dy = food.y - this.y;
        this.angle = Math.atan2(dy, dx);

        // Absorb food if overlapping
        if (this.overlaps(food)) {
            arena.removeItem(food);
        }
    }

    @Override
    public void draw(GraphicsContext gc) {
        super.draw(gc);

        // Draw beam sensor
        gc.setStroke(Color.YELLOW);
        gc.setLineWidth(1);
        gc.strokeLine(x, y, x + sensorRange * Math.cos(angle), y + sensorRange * Math.sin(angle));

        // Optionally alternate beam color for a pulsating effect
        gc.setStroke(Color.color(1, 1, 0, 0.5)); // Semi-transparent yellow
        gc.strokeLine(x, y, x + (sensorRange * 0.8) * Math.cos(angle), y + (sensorRange * 0.8) * Math.sin(angle));

        // Draw sensor range circle
        gc.setStroke(Color.LIGHTGRAY);
        gc.setLineWidth(0.5);
        gc.strokeOval(x - sensorRange, y - sensorRange, sensorRange * 2, sensorRange * 2);
    }

    /**
     * Detects the first item in the robot's path within the sensor range.
     *
     * @param arena The arena to search for items.
     * @return The detected ArenaItem, or null if no item is found.
     */
    private ArenaItem detectItemInPath(RobotArena arena) {
        return arena.getItems().stream()
                .filter(item -> item != this) // Exclude self
                .filter(item -> {
                    double dx = item.x - this.x;
                    double dy = item.y - this.y;
                    double distance = Math.sqrt(dx * dx + dy * dy);
                    return distance <= sensorRange;
                })
                .filter(item -> {
                    double dx = item.x - this.x;
                    double dy = item.y - this.y;
                    double angleToItem = Math.atan2(dy, dx);
                    return Math.abs(angle - angleToItem) < DETECTION_ANGLE;
                })
                .findFirst()
                .orElse(null);
    }

    // Getter for sensorRange
    public double getSensorRange() {
        return sensorRange;
    }

    public void setSensorRange(double sensorRange) {
        this.sensorRange = sensorRange;
    }
}
