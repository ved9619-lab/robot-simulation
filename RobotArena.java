package com.example.robotgui;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Manages all objects in the arena.
 */
public class RobotArena {
    private ArrayList<ArenaItem> items;
    private double width, height;
    private Set<Food> targetedFood; // New set to track targeted food

    public RobotArena(double width, double height) {
        this.width = width;
        this.height = height;
        items = new ArrayList<>();
        targetedFood = new HashSet<>();
    }

    /**
     * Adds a new item to the arena.
     *
     * @param item The item to be added.
     */
    public void addItem(ArenaItem item) {
        items.add(item);
    }

    /**
     * Removes an item from the arena.
     *
     * @param item The item to be removed.
     */
    public void removeItem(ArenaItem item) {
        items.remove(item);
        targetedFood.remove(item); // Remove from targeted food if necessary
    }

    /**
     * Returns the list of all items in the arena.
     *
     * @return ArrayList of ArenaItem objects.
     */
    public ArrayList<ArenaItem> getItems() {
        return items;
    }

    /**
     * Returns the width of the arena.
     *
     * @return The width of the arena.
     */
    public double getWidth() {
        return width;
    }

    /**
     * Returns the height of the arena.
     *
     * @return The height of the arena.
     */
    public double getHeight() {
        return height;
    }

    /**
     * Returns the set of currently targeted food.
     *
     * @return Set of Food items currently being targeted.
     */
    public Set<Food> getTargetedFood() {
        return targetedFood;
    }

    /**
     * Clears the set of targeted food.
     */
    public void clearTargetedFood() {
        targetedFood.clear();
    }

    /**
     * Periodically clears the set of targeted food.
     */
    public void periodicFoodReset() {
        Timeline resetTimer = new Timeline(new KeyFrame(Duration.seconds(10), e -> clearTargetedFood()));
        resetTimer.setCycleCount(Timeline.INDEFINITE);
        resetTimer.play();
    }

    /**
     * Updates the state of all items in the arena.
     */
    public void update() {
        for (ArenaItem item : items) {
            item.update(this);
        }
    }

    /**
     * Draws all items in the arena on the canvas.
     *
     * @param gc The GraphicsContext used for drawing.
     */
    public void draw(GraphicsContext gc) {
        for (ArenaItem item : items) {
            item.draw(gc);
        }
    }

    /**
     * Draws walls (borders) around the arena.
     *
     * @param gc The GraphicsContext used for drawing.
     */
    public void drawWalls(GraphicsContext gc) {
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(3);

        // Draw top border
        gc.strokeLine(0, 0, width, 0);
        // Draw right border
        gc.strokeLine(width, 0, width, height);
        // Draw bottom border
        gc.strokeLine(0, height, width, height);
        // Draw left border
        gc.strokeLine(0, 0, 0, height);
    }
}
