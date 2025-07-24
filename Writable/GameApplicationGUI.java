package ui;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

import model.Event;
import model.EventLog;
import model.Level;
import persistence.GameReader;
import persistence.GameWriter;

// Represents the main GUI application for the Maze Game. 
// It manages different views such as the main menu, level selection, 
// game instructions, inventory, and the game itself.

public class GameApplicationGUI extends JFrame {
    private CardLayout cardLayout; // Manages different panels in the GUI
    private JPanel mainPanel; // Main container for all panels
    private static final String SAVE_FILE = "./data/gamePanelState.json"; // File path for saving/loading game state
    private GamePanel gamePanel; // GamePanel instance to manage game state
    private LevelSelectionPanel levelSelectionPanel; // For selecting difficulty level

    /**
     * REQUIRES: None
     * MODIFIES: this
     * EFFECTS: Initializes the Maze Game application GUI with the main menu, level
     * selection panel, and other views. Sets up event handling for
     * window closing and panel transitions.
     */
    public GameApplicationGUI() {
        setTitle("Maze Game");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); // Prevent automatic closing
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                showSavePrompt(); // Show the save prompt when user clicks the "X" button
            }
        });

        setSize(800, 600);

        // Initialize the GamePanel
        gamePanel = new GamePanel();

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // Initialize Level Selection Panel
        levelSelectionPanel = new LevelSelectionPanel(e -> startGame(levelSelectionPanel.getSelectedLevel()));

        mainPanel.add(
                new MainMenu(this::showLevelSelection, this::loadGame, this::viewInventory, this::viewInstructions),
                "MainMenu");
        mainPanel.add(levelSelectionPanel, "LevelSelection");
        mainPanel.add(new GameInstructions(), "Instructions");
        mainPanel.add(new InventoryPanel(), "Inventory");

        add(mainPanel);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    /**
     * REQUIRES: None
     * MODIFIES: this
     * EFFECTS: Displays a pop-up save prompt when the user clicks the "X" button
     * to close the application. Saves the game state if the user chooses to.
     */
    private void showSavePrompt() {
        int choice = JOptionPane.showConfirmDialog(this,
                "Do you want to save your progress before exiting?",
                "Exit Confirmation",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (choice == JOptionPane.YES_OPTION) {
            saveGame(); // Save the game state
            for (Event event : EventLog.getInstance()) {
                System.out.println(event);
            }
            System.exit(0); // Exit the application
        } else if (choice == JOptionPane.NO_OPTION) {
            gamePanel.clearGameState(); // Clear the game state
            for (Event event : EventLog.getInstance()) {
                System.out.println(event);
            }
            System.exit(0); // Exit without saving
        }
        // If cancel, do nothing
    }

    /**
     * REQUIRES: None
     * MODIFIES: SAVE_FILE
     * EFFECTS: Saves the current game state to a JSON file. Displays a message
     * indicating success or failure.
     */
    private void saveGame() {
        GameWriter writer = new GameWriter(SAVE_FILE);
        try {
            writer.open();
            writer.write(gamePanel); // Save the current game state
            writer.close();
            JOptionPane.showMessageDialog(this, "Game saved successfully!", "Save Game",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Failed to save game: " + e.getMessage(), "Save Game",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * REQUIRES: None
     * MODIFIES: this
     * EFFECTS: Displays the level selection panel in the GUI.
     */
    private void showLevelSelection() {
        cardLayout.show(mainPanel, "LevelSelection");
    }

    /**
     * REQUIRES: selectedLevel >= 1
     * MODIFIES: this
     * EFFECTS: Starts the game at the selected difficulty level. If the level is
     * locked, displays a warning message and prevents starting the level.
     */
    private void startGame(int selectedLevel) {
        if (!gamePanel.checkAvailability(selectedLevel)) {
            JOptionPane.showMessageDialog(this, "You must complete the previous level first!", "Level Locked",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        Level level = new Level(selectedLevel); // Start the game at the selected difficulty level
        GamePanelGUI gamePanelGUI = new GamePanelGUI(gamePanel, level); // Pass GamePanel and Level to GamePanelGUI
        mainPanel.add(gamePanelGUI, "Game");
        cardLayout.show(mainPanel, "Game");
    }

    /**
     * REQUIRES: SAVE_FILE exists and contains a valid game state
     * MODIFIES: this, gamePanel
     * EFFECTS: Loads the game state from a JSON file. Displays an error message
     * if the file does not exist or cannot be loaded.
     */
    private void loadGame() {
        try {
            GameReader reader = new GameReader(SAVE_FILE);
            gamePanel = reader.read(); // Load GamePanel from save
            Level loadedLevel = gamePanel.getCurrentLevel();

            if (loadedLevel == null) {
                JOptionPane.showMessageDialog(this, "No saved game found!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            GamePanelGUI gamePanelGUI = new GamePanelGUI(gamePanel, loadedLevel); // Create GamePanelGUI
            gamePanelGUI.loadGame(loadedLevel); // Sync the GUI with loaded data
            mainPanel.add(gamePanelGUI, "Game");
            cardLayout.show(mainPanel, "Game"); // Display the game panel
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Failed to load game: " + e.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * REQUIRES: None
     * MODIFIES: this
     * EFFECTS: Displays the inventory panel in the GUI.
     */
    private void viewInventory() {
        cardLayout.show(mainPanel, "Inventory");
    }

    /**
     * REQUIRES: None
     * MODIFIES: this
     * EFFECTS: Displays the instructions panel in the GUI.
     */
    private void viewInstructions() {
        cardLayout.show(mainPanel, "Instructions");
    }

    /**
     * REQUIRES: None
     * MODIFIES: None
     * EFFECTS: Launches the Maze Game application GUI.
     */
    public static void main(String[] args) {
        new GameApplicationGUI();
    }
}
