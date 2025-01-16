package com.example.robotgui;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * A robot with a beam sensor that detects objects in its path.
 */
public class BeamSensorRobot extends Robot {
    private double sensorRange; // Range of the beam sensor

    public BeamSensorRobot(double x, double y, double radius, double angle, double speed, double sensorRange) {
        super(x, y, radius, angle, speed);
        this.sensorRange = sensorRange;
    }

    @Override
    public void update(RobotArena arena) {
        // Move the robot
        move();

        // Detect obstacles or food in its path
        ArenaItem detectedItem = detectItemInPath(arena);
        if (detectedItem != null) {
            if (detectedItem instanceof Obstacle) {
                // Turn away from the obstacle
                angle += Math.PI / 4; // Turn 45 degrees
            } else if (detectedItem instanceof Food) {
                // Move towards food
                double dx = detectedItem.x - this.x;
                double dy = detectedItem.y - this.y;
                this.angle = Math.atan2(dy, dx);

                // Absorb food if overlapping
                if (this.overlaps(detectedItem)) {
                    arena.removeItem(detectedItem);
                }
            }
        }

        avoidObstacles(arena);
        stayInArenaBounds(arena);
    }

    @Override
    public void draw(GraphicsContext gc) {
        // Draw robot body and wheels
        super.draw(gc);

        // Draw beam sensor
        gc.setStroke(Color.YELLOW);
        gc.setLineWidth(1);
        gc.strokeLine(x, y, x + sensorRange * Math.cos(angle), y + sensorRange * Math.sin(angle));

        // Draw sensor range indicator (optional)
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
        for (ArenaItem item : arena.getItems()) {
            if (item != this) { // Exclude self
                double dx = item.x - this.x;
                double dy = item.y - this.y;
                double distance = Math.sqrt(dx * dx + dy * dy);
                double angleToItem = Math.atan2(dy, dx);

                if (distance <= sensorRange && Math.abs(angle - angleToItem) < Math.PI / 6) { // Within range and narrow angle
                    return item;
                }
            }
        }
        return null;
    }
}
