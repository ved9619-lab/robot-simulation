package com.example.robotgui;

import javafx.scene.canvas.GraphicsContext;
import java.util.ArrayList;

/**
 * Manages all objects in the arena.
 */
public class RobotArena {
    private ArrayList<ArenaItem> items;
    private double width, height;

    public RobotArena(double width, double height) {
        this.width = width;
        this.height = height;
        items = new ArrayList<>();
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
}
