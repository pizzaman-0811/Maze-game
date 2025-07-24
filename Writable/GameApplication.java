package ui;

import persistence.GameWriter;
import java.io.IOException;

// Represents the main application for the Maze Game, handling the game loop and user interaction through the console.
public class GameApplication {
    private GamePanel gamePanel;
    private static final String SAVE_FILE = "./data/gamePanelState.json"; // Path for saving/loading the game

    public GameApplication() {
        gamePanel = new GamePanel();
    }
    /*
     * EFFECTS: Initializes the GameApplication and starts the game loop.
     */

    public static void main(String[] args) {
        GameApplication app = new GameApplication();
        app.run();
    }

    /*
     * EFFECTS: Runs the main game loop, displaying the menu and handling user input
     * to start the game, display instructions, or exit.
     */
    @SuppressWarnings("methodlength")
    public void run() {
        while (true) {
            int choice = ConsoleMenu.displayMainMenu();
            switch (choice) {
                case 1: // Start Game
                    if (gamePanel.isLevelInProgress()) {
                        System.out.println("Resuming current level...");
                        gamePanel.playLevel(); // Resume the game directly
                    } else {
                        System.out.println("No ongoing level. Choose a new level:");
                        gamePanel.startGame(); // Start a new game
                    }
                    break;

                case 2: // Save Game
                    saveGame();
                    break;

                case 3: // Load Game
                    if (gamePanel.loadGameState()) { // Use the renamed method
                        if (gamePanel.isLevelInProgress()) {
                            System.out.println("Resuming previous level...");
                            gamePanel.playLevel(); // Resume the loaded game
                        }
                    }
                    break;

                case 4: // View Instructions
                    ConsoleMenu.displayInstructions();
                    break;

                case 5: // Exit
                    System.out.println("Thank you for playing! Exiting...");
                    return;

                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    /*
     * EFFECTS: Saves the current game state to a file using GameWriter.
     */
    private void saveGame() {
        GameWriter writer = new GameWriter(SAVE_FILE);
        try {
            writer.open();
            writer.writeGameCompleteStatus(gamePanel);
            writer.close();
            System.out.println("Game saved successfully.");
        } catch (IOException e) {
            System.out.println("Unable to save the game: " + e.getMessage());
        }
    }
}
