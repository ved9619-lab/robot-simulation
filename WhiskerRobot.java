package com.example.robotgui;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * A robot with whiskers that chases food and avoids obstacles.
 */
public class WhiskerRobot extends Robot {
    private double whiskerLength;
    private double energy; // Energy level of the prey bot

    public WhiskerRobot(double x, double y, double radius, double angle, double speed, double whiskerLength) {
        super(x, y, radius, angle, speed);
        this.whiskerLength = whiskerLength;
        this.energy = 100; // Initial energy level
    }

    @Override
    public void update(RobotArena arena) {
        if (energy <= 0) {
            arena.removeItem(this); // Remove the bot if energy is zero
            return;
        }

        // Reduce energy over time
        energy -= 0.05;

        // Move towards food
        ArenaItem nearestFood = findNearestFood(arena);
        if (nearestFood != null) {
            double dx = nearestFood.x - this.x;
            double dy = nearestFood.y - this.y;
            this.angle = Math.atan2(dy, dx);

            // Absorb food if overlapping
            if (this.overlaps(nearestFood)) {
                arena.removeItem(nearestFood);
                energy = Math.min(energy + 20, 100); // Regain energy, max 100
            }
        }

        move();
        avoidObstacles(arena);
        stayInArenaBounds(arena);
    }

    @Override
    public void draw(GraphicsContext gc) {
        // Draw robot body and wheels
        super.draw(gc);

        // Draw whiskers
        gc.setStroke(Color.RED);
        double whiskerAngle = Math.PI / 8;
        gc.strokeLine(x, y, x + whiskerLength * Math.cos(angle - whiskerAngle),
                y + whiskerLength * Math.sin(angle - whiskerAngle));
        gc.strokeLine(x, y, x + whiskerLength * Math.cos(angle + whiskerAngle),
                y + whiskerLength * Math.sin(angle + whiskerAngle));

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
            if (item instanceof Food) {
                double distance = Math.sqrt(Math.pow(item.x - this.x, 2) + Math.pow(item.y - this.y, 2));
                if (distance < nearestDistance) {
                    nearestDistance = distance;
                    nearest = item;
                }
            }
        }
        return nearest;
    }
}
