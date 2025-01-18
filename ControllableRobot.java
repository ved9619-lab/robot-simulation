package com.example.robotgui;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * A controllable robot that can move, interact with items in the arena, and display health and score.
 * Features include:
 * - Movement in all four directions within arena bounds.
 * - Health and score tracking.
 */
public class ControllableRobot extends ArenaItem {
    private double speed; // Movement speed of the robot
    private int health; // Health of the robot (0 to 100)
    private int score; // Score tracking for interactions

    /**
     * Constructs a controllable robot with the given attributes.
     * @param x      Initial x-coordinate of the robot.
     * @param y      Initial y-coordinate of the robot.
     * @param radius Radius of the robot.
     * @param speed  Movement speed of the robot.
     */
    public ControllableRobot(double x, double y, double radius, double speed) {
        super(x, y, radius);
        this.speed = speed;
        this.health = 100; // Initial health
        this.score = 0; // Initial score
    }

    /**
     * Moves the robot up, ensuring it stays within the arena bounds.
     */
    public void moveUp() {
        this.y = Math.max(this.radius, this.y - speed); // Move up while staying within bounds
    }

    /**
     * Moves the robot down, ensuring it stays within the arena bounds.
     * @param maxHeight The maximum height of the arena.
     */
    public void moveDown(double maxHeight) {
        this.y = Math.min(maxHeight - this.radius, this.y + speed); // Move down while staying within bounds
    }

    /**
     * Moves the robot left, ensuring it stays within the arena bounds.
     */
    public void moveLeft() {
        this.x = Math.max(this.radius, this.x - speed); // Move left while staying within bounds
    }

    /**
     * Moves the robot right, ensuring it stays within the arena bounds.
     * @param maxWidth The maximum width of the arena.
     */
    public void moveRight(double maxWidth) {
        this.x = Math.min(maxWidth - this.radius, this.x + speed); // Move right while staying within bounds
    }

    /**
     * Gets the current health of the robot.
     * @return The health of the robot.
     */
    public int getHealth() {
        return health;
    }

    /**
     * Reduces the robot's health by a specified amount.
     * @param amount The amount to reduce health by.
     */
    public void reduceHealth(int amount) {
        this.health = Math.max(0, this.health - amount); // Prevent health from dropping below 0
    }

    /**
     * Increases the robot's health by a specified amount, capped at 100.
     *
     * @param amount The amount to increase health by.
     */
    public void increaseHealth(int amount) {
        this.health = Math.min(100, this.health + amount); // Cap health at 100
    }

    /**
     * Gets the current score of the robot.
     *
     * @return The score of the robot.
     */
    public int getScore() {
        return score;
    }

    /**
     * Increases the robot's score by a specified amount.
     *
     * @param amount The amount to increase the score by.
     */
    public void increaseScore(int amount) {
        this.score += amount;
    }

    /**
     * Gets the movement speed of the robot.
     * @return The speed of the robot.
     */
    public double getSpeed() {
        return speed;
    }

    /**
     * Updates the robot's state, handling interactions with other items in the arena.
     * @param arena The arena containing all items.
     */
    @Override
    public void update(RobotArena arena) {
        for (ArenaItem item : arena.getItems()) {
            if (item != this) {
                if (item instanceof Food && isCollidingWith(item)) {
                    arena.removeItem(item); // Remove the food from the arena
                    increaseHealth(10); // Increase health by 10 when food is eaten
                    increaseScore(5); // Increase score by 5
                } else if (isCollidingWith(item)) {
                    reduceHealth(10); // Reduce health by 10 on collision
                }
            }
        }

        if (health <= 0) {
            arena.removeItem(this); // Remove the robot if health is 0
        }
    }

    /**
     * Checks if this robot is colliding with another item.
     * @param other The other arena item to check for collision.
     * @return True if the items are colliding, false otherwise.
     */
    private boolean isCollidingWith(ArenaItem other) {
        double dx = this.x - other.x;
        double dy = this.y - other.y;
        double distance = Math.sqrt(dx * dx + dy * dy);
        return distance < this.radius + other.radius;
    }

    /**
     * Draws the robot, including its health bar and score.
     * @param gc The graphics context used for rendering.
     */
    @Override
    public void draw(GraphicsContext gc) {
        if (health > 0) {
            // Body of the robot
            gc.setFill(Color.BLUEVIOLET);
            gc.fillOval(x - radius, y - radius, radius * 2, radius * 2);

            // Wheels (rectangles)
            gc.setFill(Color.BLACK);
            double wheelWidth = radius * 0.4;
            double wheelHeight = radius * 0.15;

            // Top wheel
            gc.fillRect(x - wheelWidth / 2, y - radius - wheelHeight, wheelWidth, wheelHeight);

            // Bottom wheel
            gc.fillRect(x - wheelWidth / 2, y + radius, wheelWidth, wheelHeight);

            // Left wheel
            gc.fillRect(x - radius - wheelHeight, y - wheelWidth / 2, wheelHeight, wheelWidth);

            // Right wheel
            gc.fillRect(x + radius, y - wheelWidth / 2, wheelHeight, wheelWidth);

            // Decorative border
            gc.setStroke(Color.GOLD);
            gc.setLineWidth(2);
            gc.strokeOval(x - radius, y - radius, radius * 2, radius * 2);

            // Health bar
            gc.setFill(Color.RED);
            gc.fillRect(x - radius, y - radius - 10, radius * 2 * health / 100.0, 5);
            gc.setStroke(Color.BLACK);
            gc.strokeRect(x - radius, y - radius - 10, radius * 2, 5);

            // Score display
            gc.setFill(Color.BLACK);
            gc.fillText("Score: " + score, x - radius, y - radius - 20);
        }
    }
}
