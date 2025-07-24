package model;

import java.util.Random;

import persistence.Writable;
import org.json.JSONObject;
// Represents a level in the maze game, managing the maze, player, items (key, flashlight), and ghost entities.

public class Level implements Writable {
    private int levelNumber; // The number of the level (e.g., 1, 2, 3)
    private Maze maze; // The maze associated with the level
    private Player player; // The player navigating the maze
    private Item key; // The key item that the player needs to collect
    private boolean completed; // Indicates if the level is completed
    private Item flashlight; // The flashlight item in the level
    private Ghost ghost; // The ghost entity in the level

    /*
     * REQUIRES: levelNumber >= 1 and <= 3
     * EFFECTS: Constructs a new Level with the given number, creates a maze for
     * this level, initializes the player at the entrance of the maze,
     * places a key in the maze, and sets completed to false.
     */
    public Level(int levelNumber, Random random) {
        if (levelNumber < 1 || levelNumber > 3) {
            throw new IllegalArgumentException("Invalid level number");
        }
        this.levelNumber = levelNumber;
        this.maze = new Maze(); // Create a new maze for the level
        this.player = new Player(levelNumber); // Initialize the player at the maze entrance
        this.player.setPosition(9, 18); // Set player position to entrance
        this.completed = false;

        placeKey(random); // Place the key using the provided Random instance
        if (this.levelNumber > 1) {
            placeFlashlight(random); // Place the flashlight for levels 2 and 3
        }
        if (this.levelNumber > 2) {
            placeGhost(random); // Place the ghost for level 3
        }
    }

    /*
     * EFFECTS: Constructs a new Level with the given level number without
     * specifying a Random instance.
     */
    public Level(int levelNumber) {
        this(levelNumber, new Random());
    }

    /*
     * MODIFIES: this
     * EFFECTS: Places a key item randomly in the maze. Ensures the key is not
     * placed
     * at the maze entrance or exit.
     */

    public void placeKey(Random random) {
        if (this.key != null) {
            return; // Skip key placement if a key already exists
        }
        int keyX;
        int keyY;
        int[] entrance = maze.getEntrance();
        int[] exit = maze.getExit();

        // Ensure the key is placed on a path and not at the entrance or exit
        do {
            keyX = random.nextInt(maze.getMaze().length);
            keyY = random.nextInt(maze.getMaze()[0].length);
        } while (maze.getMaze()[keyY][keyX] != 0
                || (keyX == entrance[0] && keyY == entrance[1])
                || (keyX == exit[0] && keyY == exit[1]));

        this.key = new Item("key", keyX, keyY);
    }

    /*
     * MODIFIES: this
     * EFFECTS: Places a flashlight randomly in the maze, ensuring it is not at the
     * entrance, exit, or key position.
     */

    /**
     * MODIFIES: this
     * EFFECTS: Places a flashlight randomly in the maze, ensuring it is not at the
     * entrance, exit, or key position.
     */
    public void placeFlashlight(Random random) {
        if (this.flashlight != null) {
            return; // Skip flashlight placement if a flashlight already exists
        }
        int flashlightX;
        int flashlightY;
        int[] entrance = maze.getEntrance();
        int[] exit = maze.getExit();

        // Ensure the flashlight is placed on a path and not at the entrance, exit, or
        // key position
        do {
            flashlightX = random.nextInt(maze.getMaze().length);
            flashlightY = random.nextInt(maze.getMaze()[0].length);
        } while (maze.getMaze()[flashlightY][flashlightX] != 0 // Must be a path
                || (flashlightX == entrance[0] && flashlightY == entrance[1]) // Avoid entrance
                || (flashlightX == exit[0] && flashlightY == exit[1]) // Avoid exit
                || (this.key != null && flashlightX == this.key.getItemX() && flashlightY == this.key.getItemY()));

        this.flashlight = new Item("flashlight", flashlightX, flashlightY);
    }

    /*
     * MODIFIES: this
     * EFFECTS: Places a ghost entity randomly in the maze and ensures it's not
     * placed
     * at the entrance, exit, key, or flashlight positions.
     */
    public void placeGhost(Random random) {
        int ghostX;
        int ghostY;
        int[] entrance = maze.getEntrance();
        int[] exit = maze.getExit();

        // Ensure the ghost is placed on a path and not at the entrance, exit, key, or
        // flashlight position
        do {
            ghostX = random.nextInt(maze.getMaze().length);
            ghostY = random.nextInt(maze.getMaze()[0].length);
        } while (maze.getMaze()[ghostY][ghostX] != 0
                || (ghostX == entrance[0] && ghostY == entrance[1])
                || (ghostX == exit[0] && ghostY == exit[1])
                || (ghostX == this.key.getItemX() && ghostY == this.key.getItemY())
                || (ghostX == this.flashlight.getItemX() && ghostY == this.flashlight.getItemY()));

        this.ghost = new Ghost(ghostX, ghostY, this.getMaze().getMaze()); // Create a ghost at the chosen position
    }

    /*
     * EFFECTS: Returns the key item associated with this level.
     */
    public Item getKey() {
        return this.key;
    }

    /*
     * EFFECTS: Sets the key item associated with this level.
     */
    public void setKey(Item key) {
        this.key = key; // This should allow us to "remove" the key from the level by setting it to null
    }

    /*
     * EFFECTS: Returns the flashlight item associated with this level.
     */
    public Item getFlashlight() {
        return this.flashlight;
    }

    /*
     * EFFECTS: Sets the flashlight item associated with this level.
     */
    public void setFlashlight(Item flashlight) {
        this.flashlight = flashlight; // This should allow us to "remove" the key from the level by setting it to null
    }

    /*
     * EFFECTS: Returns the ghost entity associated with this level.
     */
    public Ghost getGhost() {
        return this.ghost;
    }

    /*
     * EFFECTS: Returns the number of the level.
     */
    public int getLevelNumber() {
        return levelNumber;
    }

    /*
     * EFFECTS: Returns the current state of the maze associated with this level.
     */
    public Maze getMaze() {
        return maze;
    }

    /*
     * EFFECTS: Returns the player navigating the maze in this level.
     */
    public Player getPlayer() {
        return player;
    }

    /*
     * EFFECTS: Returns whether the level is completed or not.
     */
    public boolean isCompleted() {
        return completed;
    }

    /*
     * MODIFIES: this
     * EFFECTS: Sets the level's completion status to true.
     */
    public void setCompleted() {
        this.completed = true;
        EventLog.getInstance().logEvent(new Event("Level completed"));
    }

    /*
     * MODIFIES: this
     * EFFECTS: Sets the maze for this level
     */
    public void setMaze(Maze maze) {
        this.maze = maze;
    }

    /**
     * MODIFIES: this
     * EFFECTS: Sets the ghost entity associated with this level.
     */
    public void setGhost(Ghost ghost) {
        this.ghost = ghost;
    }

    /*
     * MODIFIES: this
     * EFFECTS: Sets the Player for this level
     */
    public void setPlayer(Player player) {
        this.player = player;
    }

    /*
     * MODIFIES: this
     * EFFECTS: Resets the level, re-initializing the player at the entrance,
     * regenerating the maze, and placing a new key.
     */
    public void resetLevel() {
        this.maze = new Maze(); // Regenerate the maze
        int[] entrance = maze.getEntrance();
        this.player = new Player(); // Reset player to the entrance
        this.player.setPosition(entrance[0], entrance[1]);
        this.completed = false; // Reset completion status
        placeKey(new Random()); // Place a new key in the maze with a new random instance
    }

    /*
     * EFFECTS: Returns a JSONObject representing the level, including
     * the level number, maze, and player state.
     */
    @Override
    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("levelNumber", levelNumber);
        json.put("maze", maze.toJson());
        json.put("player", player.toJson());
        json.put("completed", completed); // Save the completion status
        // Include the key only if it is uncollected
        if (key != null && !key.isCollected()) {
            json.put("key", key.toJson());
        } else {
            json.put("key", JSONObject.NULL); // Explicitly mark absence of key
        }
        if (flashlight != null && !flashlight.isCollected()) {
            json.put("flashlight", flashlight.toJson());
        } else {
            json.put("flashlight", JSONObject.NULL); // Explicitly mark absence of flashlight
        }
        if (ghost != null) {
            json.put("ghost", ghost.toJson());
        }
        return json;
    }

    @SuppressWarnings("methodlength")
    public static Level fromJson(JSONObject jsonObject) {
        int levelNumber = jsonObject.getInt("levelNumber");
        Maze maze = Maze.fromJson(jsonObject.getJSONObject("maze"));
        Player player = Player.fromJson(jsonObject.getJSONObject("player"));

        Level level = new Level(levelNumber);
        level.setMaze(maze);
        level.setPlayer(player);
        level.setCompleted(); // new line

        // Restore the key only if it exists and is not collected
        if (jsonObject.has("key") && !jsonObject.isNull("key")) {
            Item restoredKey = Item.fromJson(jsonObject.getJSONObject("key"));
            if (!restoredKey.isCollected()) {
                level.setKey(restoredKey);
            } else {
                level.setKey(null); // Key is collected and shouldn't appear in the maze
            }
        } else {
            level.setKey(null); // No key exists in the JSON
        }

        // Restore flashlight and ghost if applicable
        if (jsonObject.has("flashlight") && !jsonObject.isNull("flashlight")) {
            Item restoredFlashlight = Item.fromJson(jsonObject.getJSONObject("flashlight"));
            if (!restoredFlashlight.isCollected()) {
                level.setFlashlight(restoredFlashlight);
            } else {
                level.setFlashlight(null); // flashlight is collected and shouldn't appear in the maze
            }
        } else {
            level.setFlashlight(null); // No key exists in the JSON
        }
        // Restore the ghost for level 3 or higher
        if (levelNumber > 2 && jsonObject.has("ghost") && !jsonObject.isNull("ghost")) {
            Ghost restoredGhost = Ghost.fromJson(jsonObject.getJSONObject("ghost"), level.getMaze().getMaze());
            level.setGhost(restoredGhost);
        } else {
            level.setGhost(null); // No ghost exists in the JSON
        }

        // Restore completed status
        level.completed = jsonObject.optBoolean("completed", false);
        return level;
    }

}
