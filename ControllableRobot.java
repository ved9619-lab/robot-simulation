package com.example.robotgui;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class ControllableRobot extends ArenaItem {
    private double speed;
    private int health;
    private int score; // New feature: score tracking
    private boolean shieldActive; // New feature: shield mechanism

    public ControllableRobot(double x, double y, double radius, double speed) {
        super(x, y, radius);
        this.speed = speed;
        this.health = 100; // Initial health
        this.score = 0; // Initial score
        this.shieldActive = false; // Shield is inactive by default
    }

    public void moveUp() {
        this.y = Math.max(this.radius, this.y - speed); // Move up while staying within bounds
    }

    public void moveDown(double maxHeight) {
        this.y = Math.min(maxHeight - this.radius, this.y + speed); // Move down while staying within bounds
    }

    public void moveLeft() {
        this.x = Math.max(this.radius, this.x - speed); // Move left while staying within bounds
    }

    public void moveRight(double maxWidth) {
        this.x = Math.min(maxWidth - this.radius, this.x + speed); // Move right while staying within bounds
    }

    public int getHealth() {
        return health;
    }

    public void reduceHealth(int amount) {
        if (!shieldActive) { // Reduce health only if shield is inactive
            this.health = Math.max(0, this.health - amount); // Prevent health from dropping below 0
        }
    }

    public void increaseHealth(int amount) {
        this.health = Math.min(100, this.health + amount); // Cap health at 100
    }

    public int getScore() {
        return score;
    }

    public void increaseScore(int amount) {
        this.score += amount;
    }

    public boolean isShieldActive() {
        return shieldActive;
    }

    public void activateShield() {
        this.shieldActive = true;
    }

    public void deactivateShield() {
        this.shieldActive = false;
    }

    public double getSpeed() {
        return speed;
    }

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
            arena.removeItem(this);
        }
    }

    private boolean isCollidingWith(ArenaItem other) {
        double dx = this.x - other.x;
        double dy = this.y - other.y;
        double distance = Math.sqrt(dx * dx + dy * dy);
        return distance < this.radius + other.radius;
    }

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
            gc.setStroke(shieldActive ? Color.CYAN : Color.GOLD); // Cyan border when shield is active
            gc.setLineWidth(2);
            gc.strokeOval(x - radius, y - radius, radius * 2, radius * 2);

            // Health bar
            gc.setFill(Color.RED);
            gc.fillRect(x - radius, y - radius - 10, radius * 2 * health / 100.0, 5);
            gc.setStroke(Color.BLACK);
            gc.strokeRect(x - radius, y - radius - 10, radius * 2, 5);

            // Score display
            gc.setFill(Color.WHITE);
            gc.fillText("Score: " + score, x - radius, y - radius - 20);
        }
    }
}
