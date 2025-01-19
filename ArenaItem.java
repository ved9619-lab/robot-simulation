package com.example.robotgui;

import javafx.scene.canvas.GraphicsContext;

/**
 * Abstract base class for all items in the arena.The class from which other classes extend
 */
public abstract class ArenaItem {
    protected double x, y; // Position
    protected double radius; // Size

    public ArenaItem(double x, double y, double radius) {
        this.x = x;
        this.y = y;
        this.radius = radius;
    }

    /**
     * Draws the item on the canvas.
     * @param gc GraphicsContext used to render the item.
     */
    public abstract void draw(GraphicsContext gc);

    /**
     * Updates the item's state.
     * @param arena Reference to the RobotArena for interaction.
     */
    public abstract void update(RobotArena arena);

    /**
     * Checks if this item overlaps with another item.
     * @param other The other ArenaItem.
     * @return True if the items overlap, false otherwise.
     */
    public boolean overlaps(ArenaItem other) {
        double dx = this.x - other.x;
        double dy = this.y - other.y;
        double distance = Math.sqrt(dx * dx + dy * dy);
        return distance < this.radius + other.radius;
    }
}
