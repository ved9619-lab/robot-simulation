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

    public void addItem(ArenaItem item) {
        items.add(item);
    }

    public ArrayList<ArenaItem> getItems() {
        return items;
    }

    public void update() {
        for (ArenaItem item : items) {
            item.update(this);
        }
    }

    public void draw(GraphicsContext gc) {
        for (ArenaItem item : items) {
            item.draw(gc);
        }
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }
}
