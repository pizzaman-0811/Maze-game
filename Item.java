package model;

import java.util.Random;

import persistence.Writable;
import org.json.JSONObject;
// Represents an item in the maze, such as a key or flashlight, with position and type attributes.

public class Item implements Writable {
    private String type; // The type of the item ("key" or "flashlight")
    private int itemX; // The x-coordinate of the item's position
    private int itemY; // The y-coordinate of the item's position
    private Random random; // Random instance for generating coordinates
    private boolean collected; // Tracks if the item has been collected

    /*
     * REQUIRES: type is "key" or "flashlight", mazeWidth > 0, mazeHeight > 0
     * EFFECTS: Constructs an item of the given type with a specified position
     * within
     * the maze.
     */
    public Item(String type, int itemX, int itemY) {
        this.type = type;
        this.random = new Random();
        setPosition(itemX, itemY);
        this.collected = false; // Default: not collected
    }

    /**
     * Returns whether the item has been collected.
     */
    public boolean isCollected() {
        return collected;
    }

    /**
     * Sets the collected status of the item.
     */
    public void setCollected(boolean collected) {
        this.collected = collected;
        EventLog.getInstance().logEvent(new Event("Item collected: " + this.type));
    }

    /*
     * MODIFIES: this
     * EFFECTS: Sets a random position for the item within the maze boundaries.
     */
    public void setRandomPosition(int mazeWidth, int mazeHeight) {
        this.itemX = generateRandomX(mazeWidth);
        this.itemY = generateRandomY(mazeHeight);
    }

    /*
     * EFFECTS: Randomly generates an x-coordinate within the maze boundaries.
     */
    public int generateRandomX(int mazeWidth) {
        return random.nextInt(mazeWidth);
    }

    /*
     * EFFECTS: Randomly generates a y-coordinate within the maze boundaries.
     */
    public int generateRandomY(int mazeHeight) {
        return random.nextInt(mazeHeight);
    }

    /*
     * MODIFIES: this
     * EFFECTS: Sets the position of this item to the specified x and y coordinates.
     */

    public void setPosition(int itemX, int itemY) {
        this.itemX = itemX;
        this.itemY = itemY;
    }

    /*
     * EFFECTS: Returns the x-coordinate of the item's position.
     */
    public int getItemX() {
        return this.itemX;
    }

    /*
     * EFFECTS: Returns the y-coordinate of the item's position.
     */
    public int getItemY() {
        return this.itemY;
    }

    /*
     * EFFECTS: Returns the type of the item ("key" or "flashlight").
     */
    public String getType() {
        return this.type;
    }

    /*
     * EFFECTS: Returns a JSONObject representing the item, including
     * the item's type and position in the maze.
     */
    @Override
    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("type", type);
        json.put("x", itemX);
        json.put("y", itemY);
        json.put("collected", collected);
        return json;
    }

    /*
     * REQUIRES: jsonObject is a valid JSON object representing an item.
     * EFFECTS: Parses an item from the JSON object and returns it.
     */
    public static Item fromJson(JSONObject jsonObject) {
        String type = jsonObject.getString("type");
        int x = jsonObject.getInt("x");
        int y = jsonObject.getInt("y");
        boolean collected = jsonObject.optBoolean("collected", false); // Default to false if missing
        Item item = new Item(type, x, y);
        item.setCollected(collected);
        return item;
    }

}
