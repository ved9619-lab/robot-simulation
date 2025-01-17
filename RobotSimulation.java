package com.example.robotgui;

import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.control.Button;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

public class RobotSimulation extends Application {
    private RobotArena arena;
    private AnimationTimer animationTimer;
    private Timeline foodSpawner;
    private boolean isFoodSpawning = false; // Flag to control food spawning
    private ArenaItem selectedRobot;
    private Text selectedRobotInfo;

    private static final int MAX_FOOD_ITEMS = 10; // Maximum number of food items allowed in the arena

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
        HBox toolbar = createToolbar(canvas.getWidth(), canvas.getHeight(), canvas);
        root.setBottom(toolbar);

        // Info section for selected robot
        selectedRobotInfo = new Text("Selected Robot: None");
        root.setRight(selectedRobotInfo);

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
                arena.drawWalls(gc); // Draw walls first
                arena.update();
                arena.draw(gc);
                drawSelectedRobotHighlight(gc);
                updateSelectedRobotInfo(); // Update live location of the selected robot
            }
        };

        // Initialize food spawner timeline (disabled by default)
        foodSpawner = new Timeline(new KeyFrame(Duration.seconds(5), e -> spawnFood()));
        foodSpawner.setCycleCount(Timeline.INDEFINITE);

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

        // Food Menu (acts as a toggle button)
        Menu foodMenu = new Menu("Food");

        MenuItem toggleFoodItem = new MenuItem("Enable Food Spawning");
        toggleFoodItem.setOnAction(e -> toggleFoodSpawning(toggleFoodItem));

        foodMenu.getItems().add(toggleFoodItem);

        // Help Menu
        Menu helpMenu = new Menu("Help");

        MenuItem helpItem = new MenuItem("Show Help");
        helpItem.setOnAction(e -> showHelp());

        helpMenu.getItems().add(helpItem);


        Menu aboutMenu = new Menu("About");
        MenuItem aboutItem = new MenuItem("About This Application");
        aboutItem.setOnAction(e -> showAbout());
        aboutMenu.getItems().add(aboutItem);

        // Reset Menu (Standalone Menu)
        Menu resetMenu = new Menu("Reset");
        MenuItem resetItem = new MenuItem("Reset Arena");
        resetItem.setOnAction(e -> resetArena());

        resetMenu.getItems().add(resetItem); // Add Reset functionality to its own menu

// Add menus to the menu bar
        menuBar.getMenus().addAll(fileMenu, foodMenu, helpMenu, aboutMenu, resetMenu);
        return menuBar;
    }
    private void showAbout() {
        Alert aboutAlert = new Alert(Alert.AlertType.INFORMATION);
        aboutAlert.setTitle("About - Robot Simulation");
        aboutAlert.setHeaderText("About This Application");
        aboutAlert.setContentText(
                "Robot Simulation\n" +
                        "Version: 1.0\n" +
                        "Author: [Vedant Pawar]\n" +
                        "Description:\n" +
                        "This simulation allows users to interact with robots, obstacles, and food in a virtual arena. " +
                        "Users can add, select, and manage various items, and observe robot behavior in a dynamic environment."
        );
        aboutAlert.showAndWait();
    }

    private HBox createToolbar(double canvasWidth, double canvasHeight, Canvas canvas) {
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
                new PredatorRobot(0, 0, 20, Math.random() * 2 * Math.PI, 1.2), canvasWidth, canvasHeight));

        // Add this code in the createToolbar method
        Button addBeamRobotButton = new Button("Add Beam Sensor Robot");
        addBeamRobotButton.setOnAction(e -> addNonOverlappingItem(
                new BeamSensorRobot(0, 0, 20, Math.random() * 2 * Math.PI, 1.5, 100),
                canvasWidth, canvasHeight));

        toolbar.getChildren().addAll(addBeamRobotButton);



        // Select Robot button
        Button selectRobotButton = new Button("Select Robot");
        selectRobotButton.setOnAction(e -> {
            enableRobotSelection(canvas);
            enableRobotMovement(canvas); // Enable robot movement after selection
        });

        // Delete Selected Robot button
        Button deleteRobotButton = new Button("Delete Selected Robot");
        deleteRobotButton.setOnAction(e -> deleteSelectedRobot());

        toolbar.getChildren().addAll(startButton, pauseButton, addRobotButton, addObstacleButton, addPredatorButton,
                selectRobotButton, deleteRobotButton);
        return toolbar;
    }

    private void enableRobotSelection(Canvas canvas) {
        canvas.setOnMouseClicked(event -> {
            double mouseX = event.getX();
            double mouseY = event.getY();

            // Find the robot closest to the click position
            selectedRobot = null;
            double minDistance = Double.MAX_VALUE;

            for (ArenaItem item : arena.getItems()) {
                double distance = Math.sqrt(Math.pow(item.x - mouseX, 2) + Math.pow(item.y - mouseY, 2));
                if (distance < item.radius && distance < minDistance) {
                    selectedRobot = item;
                    minDistance = distance;
                }
            }

            // Update selected robot info
            updateSelectedRobotInfo();
        });
    }

    private void enableRobotMovement(Canvas canvas) {
        canvas.setOnMousePressed(event -> {
            if (selectedRobot != null) {
                // Record the initial position of the mouse and robot
                selectedRobot.x = event.getX();
                selectedRobot.y = event.getY();
            }
        });

        canvas.setOnMouseDragged(event -> {
            if (selectedRobot != null) {
                // Update the robot's position as the mouse moves
                selectedRobot.x = event.getX();
                selectedRobot.y = event.getY();
                canvas.getGraphicsContext2D().clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
                arena.drawWalls(canvas.getGraphicsContext2D());
                arena.draw(canvas.getGraphicsContext2D());
                drawSelectedRobotHighlight(canvas.getGraphicsContext2D());
                updateSelectedRobotInfo();
            }
        });

        canvas.setOnMouseReleased(event -> {
            if (selectedRobot != null) {
                // Finalize the robot's position
                selectedRobot.x = event.getX();
                selectedRobot.y = event.getY();
            }
        });
    }

    private void drawSelectedRobotHighlight(GraphicsContext gc) {
        if (selectedRobot != null) {
            gc.setStroke(Color.YELLOW);
            gc.setLineWidth(2);
            gc.strokeOval(selectedRobot.x - selectedRobot.radius, selectedRobot.y - selectedRobot.radius,
                    selectedRobot.radius * 2, selectedRobot.radius * 2);
        }
    }

    private void updateSelectedRobotInfo() {
        if (selectedRobot != null) {
            selectedRobotInfo.setText(String.format("Selected Robot: %s\nPosition: (%.2f, %.2f)\nRadius: %.2f",
                    selectedRobot.getClass().getSimpleName(), selectedRobot.x, selectedRobot.y, selectedRobot.radius));
        } else {
            selectedRobotInfo.setText("Selected Robot: None");
        }
    }

    private void deleteSelectedRobot() {
        if (selectedRobot != null) {
            arena.removeItem(selectedRobot);
            selectedRobot = null;
            updateSelectedRobotInfo(); // Update the display after deletion
        }
    }

    private void spawnFood() {
        // Count current food items
        long currentFoodCount = arena.getItems().stream().filter(item -> item instanceof Food).count();

        if (currentFoodCount < MAX_FOOD_ITEMS) {
            // Add a new food item at a random position
            addNonOverlappingItem(new Food(0, 0, 10), arena.getWidth(), arena.getHeight());
        }
    }

    private void toggleFoodSpawning(MenuItem toggleFoodItem) {
        if (isFoodSpawning) {
            foodSpawner.stop();
            isFoodSpawning = false;
            toggleFoodItem.setText("Enable Food Spawning");
        } else {
            foodSpawner.play();
            isFoodSpawning = true;
            toggleFoodItem.setText("Disable Food Spawning");
        }
    }

    private void showHelp() {
        Alert helpAlert = new Alert(Alert.AlertType.INFORMATION);
        helpAlert.setTitle("Help - Robot Simulation");
        helpAlert.setHeaderText("Instructions");
        helpAlert.setContentText(
                "Welcome to the Robot Simulation!\n\n" +
                        "Here are the controls:\n" +
                        "- Start: Starts the simulation.\n" +
                        "- Pause: Pauses the simulation.\n" +
                        "- Add Robot: Adds a prey bot to the arena.\n" +
                        "- Add Obstacle: Adds an obstacle to the arena.\n" +
                        "- Add Predator: Adds a predator bot to the arena.\n" +
                        "- Select Robot: Allows you to select a robot by clicking on it.\n" +
                        "- Delete Selected Robot: Deletes the currently selected robot.\n" +
                        "- Toggle Food Spawning: Starts or stops food spawning.\n\n" +
                        "Objective:\n" +
                        "- Prey bots chase food and avoid obstacles.\n" +
                        "- Predator bots chase prey bots and avoid obstacles.\n" +
                        "- Food spawns periodically, and prey bots absorb it to increase speed.\n\n" +
                        "Enjoy the simulation!"
        );
        helpAlert.showAndWait();
    }



    private void resetArena() {
        // Stop any ongoing animations or timers
        animationTimer.stop();
        foodSpawner.stop();
        isFoodSpawning = false;

        // Clear the arena and reload the default configuration
        arena = new RobotArena(800, 600); // Reset arena with default size
        setupDefaultArena(); // Load default arena setup

        // Reset selected robot info
        selectedRobot = null;
        updateSelectedRobotInfo();

        // Restart the animation timer
        animationTimer.start();
    }

    private void addNonOverlappingItem(ArenaItem item, double canvasWidth, double canvasHeight) {
        boolean overlapping;
        do {
            overlapping = false;

            // Ensure items are placed within safe boundaries (at least radius distance from borders)
            item.x = item.radius + Math.random() * (canvasWidth - 2 * item.radius);
            item.y = item.radius + Math.random() * (canvasHeight - 2 * item.radius);

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
        // Add only normal robots and obstacles initially, with increased speed for normal robots
        addNonOverlappingItem(new WhiskerRobot(100, 100, 20, Math.PI / 4, 2, 50), 800, 600);
        addNonOverlappingItem(new WhiskerRobot(200, 200, 20, Math.PI / 3, 1.8, 50), 800, 600);
        addNonOverlappingItem(new Obstacle(400, 300, 30), 800, 600);
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
                arena.addItem(new PredatorRobot(x, y, radius, Math.PI / 4, 1.2));
            } else if (type.equals("Food")) {
                arena.addItem(new Food(x, y, radius));
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