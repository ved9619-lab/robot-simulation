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
/**
 * Main class for the Robot Simulation application.
 * This class manages the UI, robot behaviors, and interactions within the arena.
 */
public class RobotSimulation extends Application {
    private RobotArena arena; // The arena that has all items
    private AnimationTimer animationTimer;// Timer
    private Timeline foodSpawner;// Flag to control food
    private boolean isFoodSpawning = false; // Flag to control food spawning
    private ArenaItem selectedRobot; // selected bot
    private Text selectedRobotInfo;// To display info of selected bot
    private ControllableRobot controllableRobot; // Reference to the user-controlled robot


    private static final int MAX_FOOD_ITEMS = 10; // Maximum number of food items allowed in the arena

    @Override
    public void start(Stage primaryStage) {
        // Set up the primary layout

        BorderPane root = new BorderPane();
        Scene scene = new Scene(root); // Initialise the Scene object
        primaryStage.setScene(scene);  // Set the Scene for the Stage

        // Menu bar
        MenuBar menuBar = createMenuBar(primaryStage);
        root.setTop(menuBar);

        // Canvas
        Canvas canvas = new Canvas(800, 600);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        root.setCenter(canvas);// Place the menu bar at the top of the window

        // Toolbar
        HBox toolbar = createToolbar(canvas.getWidth(), canvas.getHeight(), canvas);
        root.setBottom(toolbar);

        // Info section for selected robot
        selectedRobotInfo = new Text("Selected Robot: None");
        root.setRight(selectedRobotInfo);

        // Initialise arena
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

        // Initialise food spawner timeline (disabled by default)
        foodSpawner = new Timeline(new KeyFrame(Duration.seconds(5), e -> spawnFood()));
        foodSpawner.setCycleCount(Timeline.INDEFINITE);

        // Add keyboard event handlers for controlling the bot
        scene.setOnKeyPressed(event -> {
            if (controllableRobot != null) {
                switch (event.getCode()) {
                    case W -> controllableRobot.moveUp();                // Move up
                    case S -> controllableRobot.moveDown(canvas.getHeight()); // Move down
                    case A -> controllableRobot.moveLeft();              // Move left
                    case D -> controllableRobot.moveRight(canvas.getWidth()); // Move right
                }
                // Refresh canvas
                gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
                arena.drawWalls(gc);
                arena.draw(gc); // Redraw all arena items
            }
        });


        // Set title and show the primary stage
        primaryStage.setTitle("Robot Simulation");
        primaryStage.show();
    }
    /**
     * Creates the menu bar for the application with options for saving, loading, and toggling food spawning.
     *
     * @param stage The primary stage, used for file dialog interactions.
     * @return The created MenuBar object.
     */

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
    /**
     * Displays information about the application in a dialog box.
     */
    private void showAbout() {
        Alert aboutAlert = new Alert(Alert.AlertType.INFORMATION);
        aboutAlert.setTitle("About - Robot Simulation");
        aboutAlert.setHeaderText("About This Application");
        aboutAlert.setContentText(
                "Robot Simulation\n" +
                        "Version: 1.0\n" +
                        "Author: Vedant Pawar\n" +
                        "Student Number: 32001248\n"+
                        "Description:\n" +
                        "This simulation allows users to interact with robots, obstacles, and food in a virtual arena. " +
                        "Users can add, select, and manage various items, and observe robot behavior in a dynamic environment."
        );
        aboutAlert.showAndWait();
    }

    /**
     * Creates a toolbar with buttons for user interaction, such as starting, pausing,
     * and adding different types of robots or obstacles to the arena.
     * @param canvasWidth  The width of the canvas to ensure items are placed within bounds.
     * @param canvasHeight The height of the canvas to ensure items are placed within bounds.
     * @param canvas       The canvas on which robots and other items are drawn.
     * @return An HBox containing the toolbar buttons.
     */

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

        // Add Control Bot button
        Button addControlBotButton = new Button("Add Control Bot");
        addControlBotButton.setOnAction(e -> {
            controllableRobot = new ControllableRobot(100, 100, 20, 5); // Place at a default position
            arena.addItem(controllableRobot);
        });
        toolbar.getChildren().add(addControlBotButton);


        // Add this code in the createToolbar method
        Button addBeamRobotButton = new Button("Add Beam Sensor Robot");
        addBeamRobotButton.setOnAction(e -> addNonOverlappingItem(
                new BeamSensorRobot(0, 0, 20, Math.random() * 2 * Math.PI, 1.5, 100, 50),

                canvasWidth, canvasHeight));

        toolbar.getChildren().addAll(addBeamRobotButton);



        // Select Robot button
        Button selectRobotButton = new Button("Select");
        selectRobotButton.setOnAction(e -> {
            enableRobotSelection(canvas);
            enableRobotMovement(canvas); // Enable robot movement after selection
        });

        // Delete Selected Robot button
        Button deleteRobotButton = new Button("Delete Selected ");
        deleteRobotButton.setOnAction(e -> deleteSelectedRobot());

        toolbar.getChildren().addAll(startButton, pauseButton, addRobotButton, addObstacleButton, addPredatorButton,
                selectRobotButton, deleteRobotButton);
        return toolbar;
    }
    /**
     * Enables robot selection in the arena when the canvas is clicked.
     * Highlights the closest robot to the click position.
     * @param canvas The canvas on which the robots are drawn.
     */

    private void enableRobotSelection(Canvas canvas) {
        canvas.setOnMouseClicked(event -> {
            // Get the mouse click position

            double mouseX = event.getX();
            double mouseY = event.getY();

            // Find the robot closest to the click position
            selectedRobot = null;
            double minDistance = Double.MAX_VALUE;
            // Iterate through all items in the arena
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
    /**
     * Enables dragging and moving a selected robot on the canvas.
     * Updates the robot's position based on mouse interactions.
     *
     * @param canvas The canvas on which the robots are drawn.
     */


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
                // Finalise the robot's position
                selectedRobot.x = event.getX();
                selectedRobot.y = event.getY();
            }
        });
    }
    /**
     * Draws a highlight around the selected robot to indicate it is selected.
     *
     * @param gc The graphics context used for drawing on the canvas.
     */

    private void drawSelectedRobotHighlight(GraphicsContext gc) {
        if (selectedRobot != null) {
            gc.setStroke(Color.YELLOW);
            gc.setLineWidth(2);
            gc.strokeOval(selectedRobot.x - selectedRobot.radius, selectedRobot.y - selectedRobot.radius,
                    selectedRobot.radius * 2, selectedRobot.radius * 2);
        }
    }
    /**
     * Updates the information display for the currently selected robot.
     * Displays the robot's type, position, and radius.
     */

    private void updateSelectedRobotInfo() {
        if (selectedRobot != null) {
            selectedRobotInfo.setText(String.format("Selected Robot: %s\nPosition: (%.2f, %.2f)\nRadius: %.2f",
                    selectedRobot.getClass().getSimpleName(), selectedRobot.x, selectedRobot.y, selectedRobot.radius));
        } else {
            selectedRobotInfo.setText("Selected Robot: None");
        }
    }
    /**
     * Deletes the currently selected robot from the arena.
     * Updates the display and clears the selection.
     */

    private void deleteSelectedRobot() {
        if (selectedRobot != null) {
            arena.removeItem(selectedRobot);
            selectedRobot = null;
            updateSelectedRobotInfo(); // Update the display after deletion
        }
    }
    /**
     * Spawns food items in the arena at random positions, ensuring the maximum limit is not exceeded.
     */

    private void spawnFood() {
        // Count current food items
        long currentFoodCount = arena.getItems().stream().filter(item -> item instanceof Food).count();

        if (currentFoodCount < MAX_FOOD_ITEMS) {
            // Add a new food item at a random position
            addNonOverlappingItem(new Food(0, 0, 10), arena.getWidth(), arena.getHeight());
        }
    }

    /**
     * Toggles the automatic spawning of food items in the arena.
     *
     * @param toggleFoodItem The menu item that toggles food spawning.
     */

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
    /**
     * Displays a help dialog with instructions on how to use the simulation.
     */

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
                        "- Add Control Bot: Adds a bot that can be controlled by the user using w,s,a,d,\n"+
                        "- Add Beam Sensor Robot: Adds a beam sensor robot to the arena.\n" +
                        "- Reset: Resets the simulation.\n" +
                        "- Toggle Food Spawning: Starts or stops food spawning.\n\n" +
                        "Objective:\n" +
                        "- Prey bots chase food and avoid obstacles.\n" +
                        "- Predator bots chase prey bots and avoid obstacles.\n" +
                        "- Food spawns periodically, and prey bots absorb it to increase speed.\n\n" +
                        "Enjoy the simulation!"
        );
        // Display the alert and wait for the user to close it
        helpAlert.showAndWait();
    }

    /**
     * Resets the arena to its default state by clearing all items and restarting the simulation.
     */

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


    /**
     * Adds an item to the arena ensuring it does not overlap with existing items.
     *
     * @param item         The ArenaItem to add to the arena.
     * @param canvasWidth  The width of the canvas, used to ensure placement within bounds.
     * @param canvasHeight The height of the canvas, used to ensure placement within bounds.
     */
    private void addNonOverlappingItem(ArenaItem item, double canvasWidth, double canvasHeight) {
        boolean overlapping; // Flag to check for overlapping items
        do {
            overlapping = false;

            // Generate random position within canvas boundaries, accounting for item radius
            item.x = item.radius + Math.random() * (canvasWidth - 2 * item.radius);
            item.y = item.radius + Math.random() * (canvasHeight - 2 * item.radius);

            // Check for overlap with existing items in the arena
            for (ArenaItem other : arena.getItems()) {
                double dx = item.x - other.x;
                double dy = item.y - other.y;
                double distance = Math.sqrt(dx * dx + dy * dy);

                // If the distance between items is less than the sum of their radii, overlap exists
                if (distance < item.radius + other.radius) {
                    overlapping = true; // Set flag to true to retry position generation
                    break;
                }
            }
        } while (overlapping); // Repeat until a non-overlapping position is found

        arena.addItem(item); // Add the item to the arena after finding a valid position
    }

    /**
     * Sets up the arena with a default configuration of robots and obstacles.
     * Adds only normal robots and obstacles initially, with predefined positions and attributes.
     */
    private void setupDefaultArena() {
        addNonOverlappingItem(new WhiskerRobot(100, 100, 20, Math.PI / 4, 2, 50), 800, 600);
        addNonOverlappingItem(new WhiskerRobot(200, 200, 20, Math.PI / 3, 1.8, 50), 800, 600);
        addNonOverlappingItem(new Obstacle(400, 300, 30), 800, 600);
    }

    /**
     * Attempts to load the default configuration from a file.
     *
     * @return True if the default configuration is successfully loaded, false otherwise.
     */
    private boolean loadDefaultConfiguration() {
        File defaultConfig = new File("default_config.txt");
        if (defaultConfig.exists()) {
            try {
                loadArenaFromFile(defaultConfig); // Load arena items from the file
                return true;
            } catch (IOException e) {
                showError("Failed to load default configuration."); // Show error if loading fails
            }
        }
        return false; // Return false if the file does not exist or loading fails
    }

    /**
     * Saves the current arena configuration to a file.
     *
     * @param stage The primary stage, used to display a file chooser dialog.
     */
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
                Files.write(file.toPath(), data.toString().getBytes()); // Write the configuration to the file
            } catch (IOException e) {
                showError("Failed to save configuration."); // Show error if saving fails
            }
        }
    }

    /**
     * Loads the arena configuration from a file.
     *
     * @param stage The primary stage, used to display a file chooser dialog.
     */
    private void loadConfiguration(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load Configuration");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));

        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            try {
                loadArenaFromFile(file); // Load arena items from the selected file
            } catch (IOException e) {
                showError("Failed to load configuration."); // Show error if loading fails
            }
        }
    }

    /**
     * Loads arena items from a specified file.
     *
     * @param file The file containing the arena configuration.
     * @throws IOException If an error occurs while reading the file.
     */
    private void loadArenaFromFile(File file) throws IOException {
        arena = new RobotArena(800, 600); // Reset the arena
        List<String> lines = Files.readAllLines(file.toPath());
        for (String line : lines) {
            String[] parts = line.split(",");
            String type = parts[0];
            double x = Double.parseDouble(parts[1]);
            double y = Double.parseDouble(parts[2]);
            double radius = Double.parseDouble(parts[3]);

            // Add items based on their type
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

    /**
     * Displays an error message in a dialog box.
     *
     * @param message The error message to display.
     */
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
