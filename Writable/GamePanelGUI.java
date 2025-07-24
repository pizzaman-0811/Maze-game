package ui;

import model.Level;
import model.Player;
import model.Ghost;
import model.Item;
import persistence.GameWriter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.FileNotFoundException;

/**
 * Represents the GUI panel for playing the Maze Game.
 * Handles:
 * - Rendering the maze and player position
 * - Player movement and interactions with items
 * - Inventory management
 * - Save game functionality
 */
public class GamePanelGUI extends JPanel implements KeyListener {
    private static final String SAVE_FILE = "./data/gamePanelState.json";
    private GamePanel gamePanel; // Reference to the overall game panel
    private Level level; // Current game level
    private JLabel[][] mazeCells;
    private JLabel statusLabel; // Label for displaying messages
    private JButton saveButton; // Save button
    private JButton collectKeyButton; // Collect key button
    private JButton collectFlashlightButton; // Collect flashlight button
    private JList<String> inventoryList; // Inventory list UI
    private DefaultListModel<String> inventoryListModel; // Inventory list model
    private static final int TILE_SIZE = 30;

    /**
     * REQUIRES: gamePanel and level are not null
     * MODIFIES: this
     * EFFECTS: Initializes the game panel GUI with the maze, controls, inventory,
     * and event listeners.
     */
    public GamePanelGUI(GamePanel gamePanel, Level level) {
        this.gamePanel = gamePanel; // Initialize the GamePanel reference
        this.level = gamePanel.getCurrentLevel(); // Initialize the level
        setLayout(new BorderLayout());

        // Maze Panel
        JPanel mazePanel = new JPanel(
                new GridLayout(level.getMaze().getMaze().length, level.getMaze().getMaze()[0].length));
        mazeCells = new JLabel[level.getMaze().getMaze().length][level.getMaze().getMaze()[0].length];
        initMaze(mazePanel);

        // Status Label
        statusLabel = new JLabel("Use WASD to move!", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 14));

        // Inventory Panel with Toggle
        JPanel inventoryPanel = createInventoryPanel();

        // Control Panel for Save and Collect buttons
        JPanel controlPanel = createControlPanel();

        // Add components to the main panel
        add(mazePanel, BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);
        add(inventoryPanel, BorderLayout.EAST);
        add(controlPanel, BorderLayout.NORTH); // Place control buttons at the top

        // Restore the inventory in the UI
        restoreInventory();

        addKeyListener(this);
        setFocusable(true);
        requestFocusInWindow();
    }

    /**
     * MODIFIES: this
     * EFFECTS: Creates a control panel with buttons for saving the game, collecting
     * keys,
     * and collecting flashlights.
     */
    private JPanel createControlPanel() {
        JPanel controlPanel = new JPanel(new GridLayout(1, 3));
        controlPanel.setBorder(BorderFactory.createTitledBorder("Controls"));

        // Save Button
        saveButton = new JButton("Save Game");
        saveButton.addActionListener(e -> saveGame());
        controlPanel.add(saveButton);

        // Collect Key Button
        collectKeyButton = new JButton("Collect Key");
        collectKeyButton.setEnabled(false); // Initially disabled
        collectKeyButton.addActionListener(e -> collectKey(level.getKey()));
        controlPanel.add(collectKeyButton);

        // Collect Flashlight Button
        collectFlashlightButton = new JButton("Collect Flashlight");
        collectFlashlightButton.setEnabled(false); // Initially disabled
        collectFlashlightButton.addActionListener(e -> collectFlashlight(level.getFlashlight()));
        controlPanel.add(collectFlashlightButton);

        return controlPanel;
    }

    /**
     * MODIFIES: this
     * EFFECTS: Creates the inventory panel, allowing the player to view and toggle
     * the inventory.
     */
    private JPanel createInventoryPanel() {
        JPanel inventoryPanel = new JPanel(new BorderLayout());
        inventoryPanel.setBorder(BorderFactory.createTitledBorder("Inventory"));
        inventoryPanel.setPreferredSize(new Dimension(200, 200));

        // Inventory list
        inventoryListModel = new DefaultListModel<>();
        inventoryList = new JList<>(inventoryListModel);
        JScrollPane inventoryScrollPane = new JScrollPane(inventoryList);
        inventoryPanel.add(inventoryScrollPane, BorderLayout.CENTER);

        // Toggle Inventory Button
        JButton toggleInventoryButton = new JButton("Toggle Inventory");
        toggleInventoryButton.addActionListener(e -> {
            inventoryList.setVisible(!inventoryList.isVisible());
            inventoryScrollPane.setVisible(!inventoryScrollPane.isVisible());
            revalidate();
            repaint();
            requestFocusInWindow();
        });
        inventoryPanel.add(toggleInventoryButton, BorderLayout.SOUTH);

        return inventoryPanel;
    }

    /**
     * MODIFIES: this
     * EFFECTS: Restores the player's inventory in the GUI.
     */
    private void restoreInventory() {
        inventoryListModel.clear(); // Clear any existing items in the GUI inventory
        for (Item item : level.getPlayer().getInventory().getItems()) {
            inventoryListModel.addElement(item.getType()); // Add each item's type to the inventory UI
        }
    }

    /**
     * MODIFIES: this
     * EFFECTS: Initializes the maze panel with cells representing walls, paths, and
     * items.
     * 
     * @param mazePanel the panel to render the maze
     */
    private void initMaze(JPanel mazePanel) {
        int[][] maze = level.getMaze().getMaze();
        for (int y = 0; y < maze.length; y++) {
            for (int x = 0; x < maze[0].length; x++) {
                mazeCells[y][x] = new JLabel("", SwingConstants.CENTER);
                mazeCells[y][x].setOpaque(true);
                mazeCells[y][x].setPreferredSize(new Dimension(TILE_SIZE, TILE_SIZE));

                mazeCells[y][x].setBackground(maze[y][x] == 1 ? Color.BLACK : Color.WHITE);
                mazePanel.add(mazeCells[y][x]);
            }
        }
        updateMaze();
    }

    /**
     * MODIFIES: this
     * EFFECTS: Updates the visual representation of the maze in the GUI,
     * highlighting the player's position, uncollected items, walls, paths,
     * and dark areas outside the player's visibility for levels 2 and 3.
     * Level 1 displays the full maze without dark cells.
     */
    @SuppressWarnings("methodlength")
    private void updateMaze() {
        Player player = level.getPlayer();
        Ghost ghost = level.getGhost();
        // System.out.println(player);
        int[][] maze = level.getMaze().getMaze();
        Item key = level.getKey(); // Retrieve the key for this level
        Item flashlight = level.getFlashlight(); // Retrieve the flashlight for this level
        int playerX = player.getPlayerX();
        int playerY = player.getPlayerY();

        int visibilityDiameter = player.getVisibilityDiameter(); // Player's vision range
        int levelNumber = level.getLevelNumber(); // Current level number

        for (int y = 0; y < maze.length; y++) {
            for (int x = 0; x < maze[0].length; x++) {
                if (levelNumber > 1) {
                    // For levels 2 and 3, calculate visibility
                    boolean isVisible = Math.abs(x - playerX) <= visibilityDiameter / 2
                            && Math.abs(y - playerY) <= visibilityDiameter / 2;

                    if (!isVisible) {
                        // Cells outside visibility range are dark
                        mazeCells[y][x].setBackground(Color.DARK_GRAY);
                    } else {
                        updateCell(x, y, playerX, playerY, key, flashlight, ghost, maze);
                    }
                } else {
                    // For level 1, always display the full maze
                    updateCell(x, y, playerX, playerY, key, flashlight, ghost, maze);
                }
            }
        }
        // Call ghost collision check
        checkForGhostCollision();
        if (ghost != null) {
            ghost.moveToNeighbor();
            checkForGhostCollision(); // Move the ghost to a neighboring cell
        }
        gamePanel.setCurrentLevel(level);
        gamePanel.setLevelInProgress(true);
        revalidate();
        repaint();
    }

    /**
     * MODIFIES: this
     * EFFECTS: Updates the cell at (x, y) based on its type, player's position, and
     * ghost's position.
     */
    private void updateCell(int x, int y, int playerX, int playerY, Item key, Item flashlight, Ghost ghost,
            int[][] maze) {
        if (playerX == x && playerY == y) {
            // Player's position
            mazeCells[y][x].setBackground(Color.BLUE);
        } else if (ghost != null && ghost.getGhostX() == x && ghost.getGhostY() == y) {
            // Ghost's position
            mazeCells[y][x].setBackground(Color.RED);
        } else if (key != null && !key.isCollected() && key.getItemX() == x && key.getItemY() == y) {
            // Key position
            mazeCells[y][x].setBackground(Color.YELLOW);
        } else if (flashlight != null && !flashlight.isCollected()
                && flashlight.getItemX() == x && flashlight.getItemY() == y) {
            // Flashlight position
            mazeCells[y][x].setBackground(Color.GREEN);
        } else if (maze[y][x] == 1) {
            // Wall
            mazeCells[y][x].setBackground(Color.BLACK);
        } else {
            // Path
            mazeCells[y][x].setBackground(Color.WHITE);
        }
    }

    /**
     * MODIFIES: this
     * EFFECTS: Check whether player collision with the ghost
     */
    private void checkForGhostCollision() {
        Player player = level.getPlayer();
        Ghost ghost = level.getGhost();

        if (ghost != null && ghost.getGhostX() == player.getPlayerX() && ghost.getGhostY() == player.getPlayerY()) {
            // Notify the player about the collision
            JOptionPane.showMessageDialog(this, "You collided with a ghost! Returning to entrance.", "Ghost Collision",
                    JOptionPane.WARNING_MESSAGE);

            // Move the player back to the entrance
            int[] entrance = level.getMaze().getEntrance();
            player.setPosition(entrance[0], entrance[1]);
            System.out.println("Forced return to entrance due to ghost collision.");
            // Update the maze and repaint
            updateMaze();
        }
    }

    /**
     * MODIFIES: this
     * EFFECTS: Saves the current game state to a file. Displays a message
     * indicating
     * success or failure.
     */
    @SuppressWarnings("methodlength")
    private void saveGame() {
        GameWriter writer = new GameWriter(SAVE_FILE);
        try {
            writer.open();
            writer.write(gamePanel);
            writer.close();
            JOptionPane.showMessageDialog(this, "Game saved successfully!", "Save Game",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (FileNotFoundException e) {
            JOptionPane.showMessageDialog(this, "Failed to save game: " + e.getMessage(), "Save Game",
                    JOptionPane.ERROR_MESSAGE);
        }
        requestFocusInWindow();
    }

    /**
     * MODIFIES: this
     * EFFECTS: Updates the current game panel GUI to reflect the loaded game state.
     * Synchronizes the game level, player inventory, and maze visuals
     * with the loaded level state.
     */
    public void loadGame(Level loadedLevel) {
        if (gamePanel.loadGameState()) { // Attempt to load the game state
            if (gamePanel.isLevelInProgress()) {
                JOptionPane.showMessageDialog(this, "Resuming previous game...", "Load Game",
                        JOptionPane.INFORMATION_MESSAGE);
                gamePanel.setCurrentLevel(loadedLevel);
                this.level = gamePanel.getCurrentLevel(); // Sync GUI with GamePanel
                restoreInventory(); // Restore inventory
                updateMaze(); // Refresh the maze
            } else {
                JOptionPane.showMessageDialog(this, "No ongoing level found. Starting a new game.", "Load Game",
                        JOptionPane.WARNING_MESSAGE);
                gamePanel.startGame(); // Start a new game
                level = gamePanel.getCurrentLevel(); // Sync GUI with GamePanel
                restoreInventory(); // Initialize inventory for the new game
                updateMaze(); // Refresh the maze for the new game
            }
        }
    }

    /**
     * MODIFIES: this, level
     * EFFECTS: Handles the player's movement based on key presses (WASD).
     * Updates the maze and checks for interactions.
     */
    @Override
    public void keyPressed(KeyEvent e) {
        String move = switch (e.getKeyCode()) {
            case KeyEvent.VK_W -> "up";
            case KeyEvent.VK_A -> "left";
            case KeyEvent.VK_S -> "down";
            case KeyEvent.VK_D -> "right";
            default -> null;
        };

        if (move != null) {
            boolean validMove = processMove(move);
            if (validMove) {
                checkForGhostCollision();
                checkForKey();
                checkForFlashlight();
                checkForExit();
                updateMaze();
                statusLabel.setText("Use WASD to move!"); // Reset status
            } else {
                statusLabel.setText("You hit a wall!"); // Display status
            }
        }
    }

    /**
     * MODIFIES: this, level
     * EFFECTS: Processes the player's movement. If the move is invalid (into a wall
     * or out of bounds),
     * the player's position is reset. Returns true if the move is valid.
     * 
     * @param move the move direction ("up", "down", "left", or "right")
     * @return true if the move is valid, false otherwise
     */
    private boolean processMove(String move) {
        int[][] maze = level.getMaze().getMaze();
        Player player = level.getPlayer();

        int prevX = player.getPlayerX();
        int prevY = player.getPlayerY();
        player.move(move);

        try {
            if (maze[player.getPlayerY()][player.getPlayerX()] == 1) {
                player.setPosition(prevX, prevY);
                return false;
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            player.setPosition(prevX, prevY);
            return false;
        }
        return true;
    }

    /**
     * MODIFIES: this, level
     * EFFECTS: Checks if the player is near the key. If so, enables the "Collect
     * Key" button and
     * updates the status label.
     */
    private void checkForKey() {
        Player player = level.getPlayer();
        Item key = level.getKey();

        if (key != null && player.getPlayerX() == key.getItemX() && player.getPlayerY() == key.getItemY()) {
            collectKeyButton.setEnabled(true); // Enable the button when near the key
            statusLabel.setText("Press 'Collect Key' to pick up the key!");
        } else {
            collectKeyButton.setEnabled(false); // Disable the button if not near the key
        }
    }

    /**
     * MODIFIES: this, level
     * EFFECTS: Checks if the player is near the flashlight. If so, enables the
     * "Collect Flashlight" button
     * and updates the status label.
     */
    private void checkForFlashlight() {
        Player player = level.getPlayer();
        Item flashlight = level.getFlashlight();

        if (flashlight != null && player.getPlayerX() == flashlight.getItemX()
                && player.getPlayerY() == flashlight.getItemY()) {
            collectFlashlightButton.setEnabled(true); // Enable the button when near the flashlight
            statusLabel.setText("Press 'Collect Flashlight' to pick up the flashlight!");
        } else {
            collectFlashlightButton.setEnabled(false); // Disable the button if not near the flashlight
        }
    }

    /**
     * MODIFIES: this, level
     * EFFECTS: Collects the key if the player is near it, adds it to the inventory,
     * and updates the maze and UI.
     * 
     * @param key the key item to collect
     */
    private void collectKey(Item key) {
        if (key == null) {
            return; // No key to collect
        }

        Player player = level.getPlayer();

        // Check if the key is already in the inventory to avoid duplicates
        if (!player.hasKey()) {
            player.collectItem(key); // Add the key to the player's inventory
            level.setKey(null); // Remove the key from the level
            inventoryListModel.addElement("Key"); // Update inventory UI
        }

        updateMaze(); // Refresh the maze to reflect the change
        statusLabel.setText("Key added to inventory!");
        collectKeyButton.setEnabled(false); // Disable the button after collecting
        requestFocusInWindow(); // Ensure focus returns to the game panel
    }

    /**
     * MODIFIES: this, level
     * EFFECTS: Collects the flashlight if the player is near it, adds it to the
     * inventory,
     * and updates the maze and UI.
     * 
     * @param flashlight the flashlight item to collect
     */
    private void collectFlashlight(Item flashlight) {
        if (flashlight == null) {
            return; // No flashlight to collect
        }

        Player player = level.getPlayer();

        // Check if the flashlight is already in the inventory to avoid duplicates
        if (!player.hasFlashlight()) {
            player.collectItem(flashlight); // Add the flashlight to the player's inventory
            level.setFlashlight(null); // Remove the flashlight from the level
            inventoryListModel.addElement("Flashlight"); // Update inventory UI
        }

        updateMaze(); // Refresh the maze to reflect the change
        statusLabel.setText("Flashlight added to inventory!");
        collectFlashlightButton.setEnabled(false); // Disable the button after collecting
        requestFocusInWindow(); // Ensure focus returns to the game panel
    }

    /**
     * MODIFIES: this, level
     * EFFECTS: Checks if the player is at the exit. If they have the key, completes
     * the level.
     */
    @SuppressWarnings("methodlength")
    private void checkForExit() {
        Player player = level.getPlayer();
        int[] exit = level.getMaze().getExit();

        if (player.getPlayerX() == exit[0] && player.getPlayerY() == exit[1]) {
            if (player.hasKey()) {
                // Mark the level as completed
                JOptionPane.showMessageDialog(this,
                        "Congratulations! You cleared Level " + level.getLevelNumber() + "!",
                        "Level Completed", JOptionPane.INFORMATION_MESSAGE);
                gamePanel.getCurrentLevel().setCompleted();
                gamePanel.getLevelCompleted()[level.getLevelNumber() - 1] = true; // Update level completion status

                // Ask the user if they want to go to the next level
                int choice = JOptionPane.showConfirmDialog(this,
                        "Do you want to proceed to the next level?",
                        "Next Level",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE);

                if (choice == JOptionPane.YES_OPTION) {
                    // Proceed to the next level
                    int nextLevelNumber = level.getLevelNumber() + 1;
                    if (nextLevelNumber <= 3) {
                        player.clearInventory();
                        restoreInventory();
                        JOptionPane.showMessageDialog(this, "Starting Level " + nextLevelNumber + "...", "Next Level",
                                JOptionPane.INFORMATION_MESSAGE);
                        gamePanel.setCurrentLevel(new Level(nextLevelNumber)); // Update GamePanel's current level
                        level = gamePanel.getCurrentLevel(); // Sync GUI with GamePanel
                        updateMaze(); // Refresh the maze
                    } else {
                        JOptionPane.showMessageDialog(this, "You completed all levels! Congratulations!",
                                "Game Completed",
                                JOptionPane.INFORMATION_MESSAGE);
                        gamePanel.setLevelInProgress(false); // Mark game as completed
                    }
                }
            } else {
                // Player doesn't have the key, show the message
                statusLabel.setText("You need the key to exit! Find it first!");
                JOptionPane.showMessageDialog(this, "You need the key to exit! Find it first!", "Key Required",
                        JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    /**
     * MODIFIES: this, level
     * EFFECTS: Handles the player's movement based on key presses (WASD).
     * Updates the maze and checks for interactions.
     */
    @Override
    public void keyReleased(KeyEvent e) {
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }
}
