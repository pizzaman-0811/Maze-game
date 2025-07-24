package persistence;

import org.json.JSONArray;
import org.json.JSONObject;

import model.Inventory;
import model.Level;
import ui.GamePanel;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

// Referenced from the JsonSerialization Demo
// https://github.students.cs.ubc.ca/CPSC210/JsonSerializationDemo
/**
 * Represents a reader that reads the game state from JSON data stored in a
 * file.
 */
public class GameReader {
    private String source;

    /*
     * REQUIRES: source is not null
     * EFFECTS: constructs reader to read from the source file
     */
    public GameReader(String source) {
        this.source = source;
    }

    /*
     * EFFECTS: reads the game state from the file and returns a GamePanel object;
     * throws IOException if an error occurs reading data from file
     */
    public GamePanel read() throws IOException {
        String jsonData = readFile(source);
        JSONObject jsonObject = new JSONObject(jsonData);
        return parseGame(jsonObject);
    }

    /*
     * EFFECTS: reads the source file as a string and returns it
     * throws IOException if an error occurs reading data from the file
     */
    public String readFile(String source) throws IOException {
        StringBuilder contentBuilder = new StringBuilder();

        try (Stream<String> stream = Files.lines(Paths.get(source), StandardCharsets.UTF_8)) {
            stream.forEach(contentBuilder::append);
        }

        return contentBuilder.toString();
    }

    /*
     * EFFECTS: parses the game state from a JSON object and returns a GamePanel
     * object
     */
    public GamePanel parseGame(JSONObject jsonObject) {
        GamePanel game = new GamePanel();
        try {
            // Load level completion status
            game.setlevelCompleted(loadLevelCompleted(jsonObject));

            // Load current level
            if (jsonObject.has("currentLevel")) {
                Level currentLevel = loadLevel(jsonObject);
                game.setCurrentLevel(currentLevel); //game.setCurrentLevel(currentLevel.getLevelNumber()); 
                game.getCurrentLevel().setMaze(currentLevel.getMaze());
                game.getCurrentLevel().setPlayer(currentLevel.getPlayer());
                game.getCurrentLevel().setKey(currentLevel.getKey());
                game.getCurrentLevel().setFlashlight(currentLevel.getFlashlight());
                // Fix: Set the ghost for level 3
                if (currentLevel.getLevelNumber() == 3 && currentLevel.getGhost() != null) {
                    game.getCurrentLevel().setGhost(currentLevel.getGhost());
                }
            }
        } catch (Exception e) {
            System.out.println("Error loading game state: " + e.getMessage());
        }
        return game;
    }

    /*
     * EFFECTS: parses the "levelCompleted" array from JSON and returns it as a
     * boolean array
     */
    public boolean[] loadLevelCompleted(JSONObject jsonObject) {
        JSONArray jsonArray = jsonObject.getJSONArray("levelCompleted");
        boolean[] levelCompleted = new boolean[jsonArray.length()];
        for (int i = 0; i < jsonArray.length(); i++) {
            levelCompleted[i] = jsonArray.getBoolean(i);
        }
        return levelCompleted;
    }

    /*
     * EFFECTS: parses the "currentLevel" object from JSON and returns it as a Level
     * object
     */
    public Level loadLevel(JSONObject jsonObject) {
        JSONObject levelJson = jsonObject.getJSONObject("currentLevel");
        return Level.fromJson(levelJson); // Level.fromJson() handles deserializing the Maze and other objects
    }

    /*
     * EFFECTS: parses the "inventory" object from JSON and returns it as an
     * Inventory object
     */
    public Inventory loadInventory(JSONObject jsonObject) {
        JSONObject inventoryJson = jsonObject.getJSONObject("inventory");
        return Inventory.fromJson(inventoryJson); // Ensure Inventory has a fromJson() method
    }

}
