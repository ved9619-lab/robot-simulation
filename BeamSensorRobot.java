package com.example.robotgui;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * A robot with a beam sensor that detects objects in its path.
 * This robot can sense obstacles, food, walls, and other robots within a defined sensor range,
 * and adjust its behavior based on detected items.
 */
public class BeamSensorRobot extends Robot {
    // Constants
    private static final double TURN_ANGLE = Math.PI / 4; // 45 degrees
    private static final double DETECTION_ANGLE = Math.PI / 6; // 30 degrees

    // Sensor range of the robot (distance the beam can detect objects)
    private double sensorRange; // Range of the beam sensor

    // Energy level of the robot
    private double energy;

    /**
     * Constructs a BeamSensorRobot with specified position, size, and movement attributes.
     * @param x           X-coordinate of the robot.
     * @param y           Y-coordinate of the robot.
     * @param radius      Radius of the robot.
     * @param angle       Initial movement angle of the robot (in radians).
     * @param speed       Speed of the robot.
     * @param sensorRange Range of the beam sensor.
     * @param energy      Initial energy level of the robot.
     */
    public BeamSensorRobot(double x, double y, double radius, double angle, double speed, double sensorRange, double energy) {
        super(x, y, radius, angle, speed);
        this.sensorRange = sensorRange;
        this.energy = energy;
    }

    /**
     * Updates the robot's position and behavior based on sensor detection.
     * If the energy level drops to 0, the robot is removed from the arena.
     *
     * @param arena The RobotArena that contains all items and walls.
     */
    @Override
    public void update(RobotArena arena) {

        if (energy <= 0) {
            arena.removeItem(this); // Remove robot from the arena when energy is depleted
            return;
        }

        move(); // Move in the current direction
        energy -= 0.05; // Reduce energy gradually with each update

        // Detect items in the robot's path
        ArenaItem detectedItem = detectItemInPath(arena);
        if (detectedItem != null) {
            if (detectedItem instanceof Obstacle || detectedItem instanceof Robot) {
                handleDetectedObstacle();
            } else if (detectedItem instanceof Food) {
                moveTowardFood(detectedItem, arena);
            }
        } else if (detectWallInPath(arena)) {
            handleDetectedObstacle(); // Turn away if a wall is detected
        }

        stayInArenaBounds(arena);
    }

    /**
     * Handles behavior when an obstacle, another robot, or a wall is detected.
     * The robot turns away by a predefined angle.
     */
    private void handleDetectedObstacle() {
        angle += TURN_ANGLE; // Turn away from obstacle, robot, or wall
        energy -= 0.5; // Turning consumes additional energy
    }

    /**
     * Moves the robot toward a detected food item.
     * If the robot overlaps the food, the food is absorbed (removed from the arena), and energy is replenished.
     * @param food  The detected food item.
     * @param arena The RobotArena that contains all items.
     */
    private void moveTowardFood(ArenaItem food, RobotArena arena) {
        double dx = food.x - this.x;
        double dy = food.y - this.y;
        this.angle = Math.atan2(dy, dx);

        // Absorb food if overlapping
        if (this.overlaps(food)) {
            arena.removeItem(food);
            energy += 10; // Replenish energy upon consuming food
        }
    }

    /**
     * Draws the robot, including its beam sensor, sensor range, and energy level.
     * @param gc The GraphicsContext used for rendering.
     */
    @Override
    public void draw(GraphicsContext gc) {
        if (energy <= 0) {
            return; // Do not draw the robot if it is "dead"
        }

        // Draw robot body
        gc.setFill(Color.DARKBLUE);
        gc.fillOval(x - radius, y - radius, radius * 2, radius * 2);

        // Draw robot border
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2);
        gc.strokeOval(x - radius, y - radius, radius * 2, radius * 2);

        // Draw wheels
        gc.setFill(Color.GRAY);
        gc.fillOval(x - radius * 0.6, y - radius * 1.2, radius * 0.4, radius * 0.4); // Top wheel
        gc.fillOval(x + radius * 0.2, y - radius * 1.2, radius * 0.4, radius * 0.4); // Top-right wheel
        gc.fillOval(x - radius * 0.6, y + radius * 0.8, radius * 0.4, radius * 0.4); // Bottom wheel
        gc.fillOval(x + radius * 0.2, y + radius * 0.8, radius * 0.4, radius * 0.4); // Bottom-right wheel

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

        // Draw energy level
        gc.setFill(Color.GREEN);
        gc.fillRect(x - radius, y - radius - 10, (energy / 100) * radius * 2, 5);
        gc.setStroke(Color.BLACK);
        gc.strokeRect(x - radius, y - radius - 10, radius * 2, 5);
    }

    /**
     * Detects the first item in the robot's path within the sensor range.
     * @param arena The arena to search for items.
     * @return The detected ArenaItem, or null if no item is found.
     */
    private ArenaItem detectItemInPath(RobotArena arena) {
        return arena.getItems().stream()
                .filter(item -> item != this) // Exclude self
                .filter(item -> {
                    // Check if item is within sensor range
                    double dx = item.x - this.x;
                    double dy = item.y - this.y;
                    double distance = Math.sqrt(dx * dx + dy * dy);
                    return distance <= sensorRange;
                })
                .filter(item -> {
                    // Check if item is within the detection angle
                    double dx = item.x - this.x;
                    double dy = item.y - this.y;
                    double angleToItem = Math.atan2(dy, dx);
                    double angleDifference = Math.abs(angle - angleToItem);
                    return angleDifference < DETECTION_ANGLE || angleDifference > 2 * Math.PI - DETECTION_ANGLE;
                })
                .findFirst()
                .orElse(null); // Return the first detected item or null
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
     * @param x1 Start x of the first line.
     * @param y1 Start y of the first line.
     * @param x2 End x of the first line.
     * @param y2 End y of the first line.
     * @param x3 Start x of the second line.
     * @param y3 Start y of the second line.
     * @param x4 End x of the second line.
     * @param y4 End y of the second line.
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

    // Getter and setter for sensorRange
    public double getSensorRange() {
        return sensorRange;
    }

    public void setSensorRange(double sensorRange) {
        this.sensorRange = sensorRange;
    }

    // Getter and setter for energy
    public double getEnergy() {
        return energy;
    }

    public void setEnergy(double energy) {
        this.energy = energy;
    }
}
