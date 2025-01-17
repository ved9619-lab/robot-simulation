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
        } else if (detectWallInPath(arena)) {
            handleDetectedObstacle(); // Turn away if a wall is detected
        }

        stayInArenaBounds(arena);
    }

    private void handleDetectedObstacle() {
        angle += TURN_ANGLE; // Turn away from obstacle or wall
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
                    double angleDifference = Math.abs(angle - angleToItem);
                    return angleDifference < DETECTION_ANGLE || angleDifference > 2 * Math.PI - DETECTION_ANGLE;
                })
                .findFirst()
                .orElse(null);
    }

    /**
     * Detects if the beam intersects with any of the walls.
     *
     * @param arena The arena to check for walls.
     * @return True if a wall is detected within the sensor range, false otherwise.
     */
    private boolean detectWallInPath(RobotArena arena) {
        double arenaWidth = arena.getWidth();
        double arenaHeight = arena.getHeight();

        // Calculate beam endpoint
        double beamEndX = x + sensorRange * Math.cos(angle);
        double beamEndY = y + sensorRange * Math.sin(angle);

        // Check for intersection with each wall
        return intersectsLine(x, y, beamEndX, beamEndY, 0, 0, arenaWidth, 0) || // Top wall
                intersectsLine(x, y, beamEndX, beamEndY, 0, 0, 0, arenaHeight) || // Left wall
                intersectsLine(x, y, beamEndX, beamEndY, arenaWidth, 0, arenaWidth, arenaHeight) || // Right wall
                intersectsLine(x, y, beamEndX, beamEndY, 0, arenaHeight, arenaWidth, arenaHeight); // Bottom wall
    }

    /**
     * Checks if a line segment intersects another line segment.
     *
     * @param x1 Start x of first line
     * @param y1 Start y of first line
     * @param x2 End x of first line
     * @param y2 End y of first line
     * @param x3 Start x of second line
     * @param y3 Start y of second line
     * @param x4 End x of second line
     * @param y4 End y of second line
     * @return True if the lines intersect, false otherwise.
     */
    private boolean intersectsLine(double x1, double y1, double x2, double y2,
                                   double x3, double y3, double x4, double y4) {
        double denom = (y4 - y3) * (x2 - x1) - (x4 - x3) * (y2 - y1);
        if (denom == 0) {
            return false; // Parallel lines
        }

        double ua = ((x4 - x3) * (y1 - y3) - (y4 - y3) * (x1 - x3)) / denom;
        double ub = ((x2 - x1) * (y1 - y3) - (y2 - y1) * (x1 - x3)) / denom;

        return ua >= 0 && ua <= 1 && ub >= 0 && ub <= 1;
    }

    // Getter for sensorRange
    public double getSensorRange() {
        return sensorRange;
    }

    public void setSensorRange(double sensorRange) {
        this.sensorRange = sensorRange;
    }
}
