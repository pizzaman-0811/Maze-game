package ui;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

import model.Player;
import persistence.GameReader;
import persistence.GameWriter;
import persistence.Writable;

import org.json.JSONArray;
import org.json.JSONObject;
import model.Item;
import model.Level;
import java.util.Set;

/**
 * Represents the game panel responsible for handling the game logic, including
 * level selection, player moves,
 * interactions with items (key, flashlight), and collisions with ghosts.
 */
public class GamePanel implements Writable {
    private GamePanel gamePanel;
    private Level currentLevel; // The current level being played
    private boolean[] levelCompleted = new boolean[] { false, false, false }; // Tracks completion of each level
    private static final String SAVE_FILE = "./data/gamePanelState.json";
    private boolean resumedGame; // Indicates if the game is being resumed
    private boolean isLevelInProgress; // Tracks if a level is in progress
    Scanner in = new Scanner(System.in); // Scanner for user input

    /**
     * MODIFIES: this
     * EFFECTS: Starts the game by prompting the player to select a level. The
     * selected level is loaded and played.
     * Continues the game if the player chooses to proceed to the next level.
     */
    @SuppressWarnings("methodlength")
    public void startGame() {
        int levelNumber;

        if (resumedGame) {
            // Skip level selection and directly resume the current level
            levelNumber = currentLevel.getLevelNumber();
            System.out.println("Resuming Level " + levelNumber + "...");
            resumedGame = false; // Reset the flag after resuming
        } else {
            // Normal flow for starting a new game
            levelNumber = askLevel();
            while (!checkAvailability(levelNumber)) {
                System.out.println("Selected level is unavailable as you need to play the prerequisite level!");
                levelNumber = askLevel();
            }
            in.nextLine(); // Consume the line break
            currentLevel = new Level(levelNumber);
        }

        playLevel();

        while (true) {
            if (!askContinue()) {
                System.out.println("Thank you for playing! Exiting...");
                return;
            } else {
                levelNumber = askLevel();
                while (!checkAvailability(levelNumber)) {
                    System.out.println("Selected level is unavailable as you need to play the prerequisite level!");
                    levelNumber = askLevel();
                }
                in.nextLine(); // Consume the line break
                currentLevel = new Level(levelNumber);
                playLevel();
                currentLevel = null;
            }
        }
    }

    // Constructor to initialize GamePanel with default level
    public GamePanel() {
        this.currentLevel = new Level(1); // Default to level 1
        this.gamePanel = this; // Point to itself for CUI logic
    }

    /**
     * EFFECTS: Returns true if the selected level is available to play. Level 1 is
     * always available,
     * and subsequent levels become available once previous levels are completed.
     */
    public boolean checkAvailability(int levelNumber) {
        if (levelNumber == 1) {
            return true;
        } else {
            return this.levelCompleted[levelNumber - 2];
        }
    }

    /**
     * EFFECTS: Prompts the player to select a difficulty level (1 for Easy, 2 for
     * Medium, 3 for Hard).
     * Ensures valid input by repeatedly prompting the player until a valid level is
     * entered.
     * RETURNS: The selected level number as an integer.
     */
    public int askLevel() {
        int levelNumber = Integer.MIN_VALUE;
        System.out.println("Choose difficulty level (1: Easy, 2: Medium, 3: Hard): ");
        while (true) {
            if (!in.hasNextInt()) {
                System.out.println("Invalid input. Please enter a number (1, 2, or 3):");
                in.next(); // Consume the invalid input
                continue;
            }
            levelNumber = in.nextInt();
            if (levelNumber < 1 || levelNumber > 3) {
                System.out.println("Invalid input. Please enter a number (1, 2, or 3):");
                continue;
            }
            break;
        }
        return levelNumber;
    }

    /**
     * EFFECTS: Prompts the player to decide if they want to continue playing.
     * Accepts 'y' for yes and 'n' for no (case-insensitive).
     * RETURNS: true if the player chooses to continue ('y'), false if the player
     * chooses to stop ('n').
     * Continues prompting until valid input is provided.
     */
    public boolean askContinue() {
        String input;

        while (true) {
            System.out.println("Do you want to continue playing? (y/n)");
            System.out.println("Select no to go back to the main menu to choose if you want to save the game");
            input = in.nextLine().trim().toLowerCase();

            if (input.equals("y")) {
                return true; // Continue playing
            } else if (input.equals("n")) {
                currentLevel = null; // Clear the current level when going back to the menu
                System.out.println("Returning to main menu...");
                return false; // Return to main menu
            } else {
                System.out.println("Invalid input. Please enter 'y' or 'n'.");
            }
        }
    }

    /**
     * MODIFIES: this
     * EFFECTS: Plays the current level, allowing the player to move and interact
     * with items in the maze.
     * Ends the level when the player finds the key and reaches the exit.
     */
    @SuppressWarnings("methodlength")
    public void playLevel() {
        this.setLevelInProgress(true); // Mark the game as in progress
        int[] exit = currentLevel.getMaze().getExit();
        while (!currentLevel.isCompleted()) {
            displayMaze();

            while (true) {
                String move = askMove();
                if (move.equals("save")) {
                    resumedGame = true;
                    saveGame(); // Save the game state
                    continue;
                }
                if (!processMove(move)) {
                    System.out.println("Invalid move or ran into a wall. Try again.");
                    displayMaze();
                } else {
                    if (currentLevel.getLevelNumber() > 2) {
                        currentLevel.getGhost().moveToNeighbor();
                    }
                    break;
                }
            }

            checkForItem(currentLevel.getKey(), "key");
            if (currentLevel.getLevelNumber() != 1) {
                checkForItem(currentLevel.getFlashlight(), "flashlight");
            }
            if (currentLevel.getLevelNumber() > 2) {
                checkForGhostCollision();
            }

            if (exit[0] == currentLevel.getPlayer().getPlayerX()
                    && exit[1] == currentLevel.getPlayer().getPlayerY()) {
                if (currentLevel.getPlayer().hasKey()) {
                    System.out.println("Congratulations! You have completed the level!");
                    this.levelCompleted[currentLevel.getLevelNumber() - 1] = true;
                    currentLevel.setCompleted();
                    currentLevel.getPlayer().clearInventory();
                    currentLevel = null;
                    break; // Exit level
                } else {
                    System.out.println("You need the key to exit. Find it first!");
                }
            }

        }
    }

    /**
     * MODIFIES: this
     * EFFECTS: Checks if the player is at the same position as the specified item.
     * If so, the player collects the item and a message is displayed.
     */
    public void checkForItem(Item item, String itemType) {
        int playerX = currentLevel.getPlayer().getPlayerX();
        int playerY = currentLevel.getPlayer().getPlayerY();

        if (item != null && item.getItemX() == playerX && item.getItemY() == playerY) {
            System.out.println("You found the " + itemType + "!");
            currentLevel.getPlayer().collectItem(item);
        }
    }

    /**
     * MODIFIES: this
     * EFFECTS: Checks if the player is near the ghost (within a distance of 1
     * unit).
     * If a collision is detected, the player is sent back to the maze's entrance,
     * and a message is displayed.
     */
    public void checkForGhostCollision() {
        int playerX = currentLevel.getPlayer().getPlayerX();
        int playerY = currentLevel.getPlayer().getPlayerY();
        int ghostX = currentLevel.getGhost().getGhostX();
        int ghostY = currentLevel.getGhost().getGhostY();

        if (ghostX == playerX && ghostY == playerY) {
            System.out.println("You have collided with a ghost! Sent back to the entrance.");
            currentLevel.getPlayer().setPosition(currentLevel.getMaze().getEntrance()[0],
                    currentLevel.getMaze().getEntrance()[1]);
        }
    }

    /**
     * EFFECTS: Prompts the player to enter a move command (up, down, left, right,
     * or their corresponding
     * WASD keys). Ensures valid input and returns the move as a string. Continues
     * prompting
     * until valid input is provided.
     * RETURNS: A string representing the player's chosen move.
     */
    public String askMove() {
        String move;
        Set<String> validMoves = Set.of("w", "a", "s", "d", "save"); // Only WASD keys are valid moves

        while (true) {
            System.out.println("Enter your move (up(W), down(S), left(A), right(D)) or Save (by typing 'save'): ");
            move = in.nextLine().trim().toLowerCase();

            if (validMoves.contains(move)) {
                break;
            } else {
                System.out.println("Invalid move. Please enter 'w', 'a', 's', 'd' or 'save'.");
                displayMaze();
            }
        }
        return move;
    }

    /**
     * MODIFIES: this
     * EFFECTS: Moves the player in the specified direction. If the player moves
     * into a wall or outside
     * the maze boundaries, the player's position is reset to the previous valid
     * position
     * and false is returned. Otherwise, returns true.
     * RETURNS: true if the move is valid (not into a wall or out of bounds), false
     * otherwise.
     */
    public boolean processMove(String move) {
        int prevX = currentLevel.getPlayer().getPlayerX();
        int prevY = currentLevel.getPlayer().getPlayerY();
        currentLevel.getPlayer().move(move);

        int[][] mazeStructure = currentLevel.getMaze().getMaze();

        try {
            // If the player moves into a wall, reset their position and return false
            if (mazeStructure[currentLevel.getPlayer().getPlayerY()][currentLevel.getPlayer().getPlayerX()] == 1) {
                currentLevel.getPlayer().setPosition(prevX, prevY);
                return false;
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            // If the player moves out of the maze, reset their position and return false
            currentLevel.getPlayer().setPosition(prevX, prevY);
            return false;
        }
        return true;
    }

    /*
     * EFFECTS: Displays the current state of the maze on the console. The player's
     * position, ghost position,
     * and items (key, flashlight) are shown based on the player's visibility. The
     * maze is displayed
     * with walls, paths, and unexplored areas.
     */
    @SuppressWarnings("methodlength")
    public void displayMaze() {
        int[][] mazeStructure = currentLevel.getMaze().getMaze();
        int playerX = currentLevel.getPlayer().getPlayerX();
        int playerY = currentLevel.getPlayer().getPlayerY();
        int visibilityDiameter = currentLevel.getPlayer().getVisibilityDiameter();

        for (int y = 0; y < mazeStructure.length; y++) {
            for (int x = 0; x < mazeStructure[y].length; x++) {
                // Only show the dark effect for levels 2 and 3
                if (currentLevel.getLevelNumber() == 1
                        || (Math.abs(y - playerY) <= visibilityDiameter / 2
                                && Math.abs(x - playerX) <= visibilityDiameter / 2)) {
                    if (y == playerY && x == playerX) {
                        System.out.print("P ");
                    } else if (currentLevel.getLevelNumber() > 2 && currentLevel.getGhost() != null
                            && (currentLevel.getGhost().getGhostX() == x && currentLevel.getGhost().getGhostY() == y)) {
                        System.out.print("G ");
                    } else if (currentLevel.getPlayer().hasKey() == false
                            && (currentLevel.getKey().getItemX() == x && currentLevel.getKey().getItemY() == y)) {
                        System.out.print("K ");
                    } else if (currentLevel.getLevelNumber() > 1
                            && currentLevel.getPlayer().hasFlashlight() == false
                            && (currentLevel.getFlashlight().getItemX() == x
                                    && currentLevel.getFlashlight().getItemY() == y)) {
                        System.out.print("F ");
                    } else if (mazeStructure[y][x] == 1) {
                        System.out.print("■ ");
                    } else {
                        System.out.print(". ");
                    }
                } else {
                    System.out.print("* ");
                }
            }
            System.out.println();
        }
    }

    /*
     * MODIFIES: this
     * EFFECTS: Advances the game to the next level if available. If there are no
     * more levels,
     * displays a congratulatory message and ends the game.
     */
    public void startNextLevel() {
        int nextLevelNumber = currentLevel.getLevelNumber() + 1;
        if (nextLevelNumber <= 3) {
            currentLevel = new Level(nextLevelNumber);
            System.out.println("Starting Level " + nextLevelNumber);
            playLevel();
        } else {
            System.out.println("You have completed all levels! Congratulations!");
        }
    }

    /**
     * EFFECTS: Returns the current level being played.
     */
    public Level getCurrentLevel() {
        return currentLevel;
    }

    /**
     * EFFECTS: Sets the current level being played.
     */
    public void setCurrentLevel(int levelNumber) {
        this.currentLevel = new Level(levelNumber); // Set the correct level
    }

    /**
     * MODIFIES: this
     * EFFECTS: Sets the current level being played, using an existing Level object.
     */
    public void setCurrentLevel(Level level) {
        this.currentLevel = level;
    }

    /**
     * EFFECTS: Returns the level completed status.
     */
    public boolean[] getLevelCompleted() {
        return levelCompleted;
    }

    /**
     * EFFECTS: sets the level completed status.
     */
    public void setlevelCompleted(boolean[] levelCompleted) {
        this.levelCompleted = levelCompleted;
    }

    /**
     * EFFECTS: Returns the levelInProgress.
     */
    public boolean isLevelInProgress() {
        return isLevelInProgress;
    }

    public void setLevelInProgress(boolean levelInProgress) {
        this.isLevelInProgress = levelInProgress;
    }

    /**
     * EFFECTS: Returns the player in the current level.
     */
    public Player getPlayer() {
        return currentLevel.getPlayer(); // Assuming Level class has a getPlayer() method
    }

    /*
     * MODIFIES: this
     * EFFECTS: Saves the current game state based on the progress of the current
     * level.
     * If a level is in progress, saves the full game state. If a level is
     * completed,
     * saves only the level completion status.
     */
    public void saveGame() {
        GameWriter writer = new GameWriter(SAVE_FILE);
        try {
            gamePanel.setLevelInProgress(true);
            writer.open();
            writer.write(this); // Save the entire game panel state, including current level and progress
            System.out.println("Game saved successfully!");
            writer.close();
        } catch (IOException e) {
            System.out.println("Unable to save the game: " + e.getMessage());
        }
    }

    /**
     * MODIFIES: SAVE_FILE
     * EFFECTS: Clears the game state by overwriting the save file with an empty
     * JSON object.
     * This ensures that no residual game data is loaded during the next game
     * session.
     * If the file cannot be written to, prints an error message to the console.
     */
    public void clearGameState() {
        try (FileWriter writer = new FileWriter(SAVE_FILE)) {
            writer.write("{}");
            System.out.println("Game state cleared successfully.");
        } catch (IOException e) {
            System.out.println("Failed to clear the game state: " + e.getMessage());
        }
    }

    // override toJson method
    @Override
    public JSONObject toJson() {
        JSONObject json = new JSONObject();

        // Save the current level (including maze and items)
        if (currentLevel != null) {
            json.put("currentLevel", currentLevel.toJson());
        }

        // Save the level completion status
        json.put("levelCompleted", new JSONArray(levelCompleted));

        // Save the resumed game state
        json.put("resumedGame", resumedGame);

        // Save whether a level is in progress
        json.put("isLevelInProgress", isLevelInProgress());
        return json;
    }

    /*
     * MODIFIES: this
     * EFFECTS: Reconstructs the game state from a JSON object.
     */
    public static GamePanel fromJson(JSONObject json) {
        GamePanel gamePanel = new GamePanel();

        // Restore level completion states
        JSONArray levelArray = json.getJSONArray("levelCompleted");
        for (int i = 0; i < gamePanel.levelCompleted.length; i++) {
            gamePanel.levelCompleted[i] = levelArray.getBoolean(i);
        }
        // Restore current level if it exists
        if (json.has("currentLevel")) {
            gamePanel.currentLevel = Level.fromJson(json.getJSONObject("currentLevel"));
            gamePanel.isLevelInProgress = json.getBoolean("isLevelInProgress"); // Restore level in progress
        } else {
            gamePanel.currentLevel = null;
            gamePanel.isLevelInProgress = false;
        }

        // Restore resumedGame state
        if (json.has("resumedGame")) {
            gamePanel.resumedGame = json.getBoolean("resumedGame");
        } else {
            gamePanel.resumedGame = false; // Default to false if not present
        }

        return gamePanel;
    }

    /*
     * MODIFIES: this
     * EFFECTS: Loads the game state from a file, restoring level completion status
     * and optionally the current level.
     */
    public boolean loadGameState() {
        GameReader reader = new GameReader(SAVE_FILE);
        try {
            JSONObject json = new JSONObject(reader.readFile(SAVE_FILE));
            GamePanel loadedGame = GamePanel.fromJson(json);

            this.levelCompleted = loadedGame.getLevelCompleted(); // Restore level completion
            this.setCurrentLevel(loadedGame.getCurrentLevel()); // Restore the current level//new
            this.isLevelInProgress = loadedGame.isLevelInProgress(); // Restore progress flag

            System.out.println("GamePanel's game state loaded successfully.");
            return true; // Game successfully loaded
        } catch (IOException e) {
            System.out.println("Unable to load the game: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("No file can be loaded, start a new game from level 1...");
        }
        return false; // Game loading failed
    }

}
