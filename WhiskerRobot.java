package com.example.robotgui;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * A robot with visible whisker sensors and wheels.
 */
public class WhiskerRobot extends Robot {
    private double whiskerLength;

    public WhiskerRobot(double x, double y, double radius, double angle, double speed, double whiskerLength) {
        super(x, y, radius, angle, speed);
        this.whiskerLength = whiskerLength;
    }

    @Override
    public void draw(GraphicsContext gc) {
        // Draw robot body and wheels using the parent method
        super.draw(gc);

        // Draw whiskers
        gc.setStroke(Color.RED);
        double whiskerAngle = Math.PI / 8;
        gc.strokeLine(x, y, x + whiskerLength * Math.cos(angle - whiskerAngle),
                y + whiskerLength * Math.sin(angle - whiskerAngle));
        gc.strokeLine(x, y, x + whiskerLength * Math.cos(angle + whiskerAngle),
                y + whiskerLength * Math.sin(angle + whiskerAngle));
    }

    @Override
    public void update(RobotArena arena) {
        super.update(arena);
    }
}
