package com.example.robotgui;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

/**
 * Main simulation application with non-overlapping object placement.
 */
public class RobotSimulation extends Application {
    private RobotArena arena;
    private AnimationTimer animationTimer;

    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();

        // Menu bar
        MenuBar menuBar = createMenuBar(primaryStage);
        root.setTop(menuBar);

        // Canvas
        Canvas canvas = new Canvas(800, 600);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        root.setCenter(canvas);

        // Toolbar
        HBox toolbar = createToolbar(canvas.getWidth(), canvas.getHeight());
        root.setBottom(toolbar);

        // Initialize arena
        arena = new RobotArena(canvas.getWidth(), canvas.getHeight());
        if (!loadDefaultConfiguration()) {
            setupDefaultArena();
        }

        // Animation loop
        animationTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
                arena.update();
                arena.draw(gc);
            }
        };

        primaryStage.setScene(new Scene(root));
        primaryStage.setTitle("Robot Simulation");
        primaryStage.show();
    }

    private MenuBar createMenuBar(Stage stage) {
        MenuBar menuBar = new MenuBar();

        // File Menu
        Menu fileMenu = new Menu("File");

        MenuItem saveItem = new MenuItem("Save Configuration");
        saveItem.setOnAction(e -> saveConfiguration(stage));

        MenuItem loadItem = new MenuItem("Load Configuration");
        loadItem.setOnAction(e -> loadConfiguration(stage));

        fileMenu.getItems().addAll(saveItem, loadItem);

        // Simulation Menu
        Menu simulationMenu = new Menu("Simulation");

        MenuItem startItem = new MenuItem("Start");
        startItem.setOnAction(e -> animationTimer.start());

        MenuItem pauseItem = new MenuItem("Pause");
        pauseItem.setOnAction(e -> animationTimer.stop());

        simulationMenu.getItems().addAll(startItem, pauseItem);

        menuBar.getMenus().addAll(fileMenu, simulationMenu);
        return menuBar;
    }

    private HBox createToolbar(double canvasWidth, double canvasHeight) {
        HBox toolbar = new HBox(10);

        // Start button
        Button startButton = new Button("Start");
        startButton.setOnAction(e -> animationTimer.start());

        // Pause button
        Button pauseButton = new Button("Pause");
        pauseButton.setOnAction(e -> animationTimer.stop());

        // Add Robot button
        Button addRobotButton = new Button("Add Robot");
        addRobotButton.setOnAction(e -> addNonOverlappingItem(new WhiskerRobot(
                0, 0, 20, Math.random() * 2 * Math.PI, 2, 50), canvasWidth, canvasHeight));

        // Add Obstacle button
        Button addObstacleButton = new Button("Add Obstacle");
        addObstacleButton.setOnAction(e -> addNonOverlappingItem(
                new Obstacle(0, 0, 30), canvasWidth, canvasHeight));

        // Add Predator button
        Button addPredatorButton = new Button("Add Predator");
        addPredatorButton.setOnAction(e -> addNonOverlappingItem(
                new PredatorRobot(0, 0, 20, Math.random() * 2 * Math.PI, 2), canvasWidth, canvasHeight));

        toolbar.getChildren().addAll(startButton, pauseButton, addRobotButton, addObstacleButton, addPredatorButton);
        return toolbar;
    }

    private void addNonOverlappingItem(ArenaItem item, double canvasWidth, double canvasHeight) {
        boolean overlapping;
        do {
            overlapping = false;
            item.x = Math.random() * canvasWidth;
            item.y = Math.random() * canvasHeight;

            for (ArenaItem other : arena.getItems()) {
                double dx = item.x - other.x;
                double dy = item.y - other.y;
                double distance = Math.sqrt(dx * dx + dy * dy);

                if (distance < item.radius + other.radius) {
                    overlapping = true; // Overlap detected
                    break;
                }
            }
        } while (overlapping);

        arena.addItem(item);
    }

    private void setupDefaultArena() {
        addNonOverlappingItem(new WhiskerRobot(100, 100, 20, Math.PI / 4, 2, 50), 800, 600);
        addNonOverlappingItem(new WhiskerRobot(200, 200, 20, Math.PI / 3, 1.5, 50), 800, 600);
        addNonOverlappingItem(new Obstacle(400, 300, 30), 800, 600);
        addNonOverlappingItem(new PredatorRobot(300, 300, 20, Math.PI / 4, 2), 800, 600);
    }

    private boolean loadDefaultConfiguration() {
        File defaultConfig = new File("default_config.txt");
        if (defaultConfig.exists()) {
            try {
                loadArenaFromFile(defaultConfig);
                return true;
            } catch (IOException e) {
                showError("Failed to load default configuration.");
            }
        }
        return false;
    }

    private void saveConfiguration(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Configuration");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));

        File file = fileChooser.showSaveDialog(stage);
        if (file != null) {
            try {
                StringBuilder data = new StringBuilder();
                for (ArenaItem item : arena.getItems()) {
                    data.append(item.getClass().getSimpleName()).append(",")
                            .append(item.x).append(",")
                            .append(item.y).append(",")
                            .append(item.radius).append("\n");
                }
                Files.write(file.toPath(), data.toString().getBytes());
            } catch (IOException e) {
                showError("Failed to save configuration.");
            }
        }
    }

    private void loadConfiguration(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load Configuration");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));

        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            try {
                loadArenaFromFile(file);
            } catch (IOException e) {
                showError("Failed to load configuration.");
            }
        }
    }

    private void loadArenaFromFile(File file) throws IOException {
        arena = new RobotArena(800, 600); // Reset arena
        List<String> lines = Files.readAllLines(file.toPath());
        for (String line : lines) {
            String[] parts = line.split(",");
            String type = parts[0];
            double x = Double.parseDouble(parts[1]);
            double y = Double.parseDouble(parts[2]);
            double radius = Double.parseDouble(parts[3]);

            if (type.equals("WhiskerRobot")) {
                arena.addItem(new WhiskerRobot(x, y, radius, Math.PI / 4, 2, 50));
            } else if (type.equals("Obstacle")) {
                arena.addItem(new Obstacle(x, y, radius));
            } else if (type.equals("PredatorRobot")) {
                arena.addItem(new PredatorRobot(x, y, radius, Math.PI / 4, 2));
            }
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("An Error Occurred");
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
