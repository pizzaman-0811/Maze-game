package model;

import persistence.Writable;
import org.json.JSONObject;

// Represents the player in the maze game, tracking their position, items collected (key, flashlight), 
// and visibility range based on level.

public class Player implements Writable {
    private int playerX; // The x-coordinate of the player's position
    private int playerY; // The y-coordinate of the player's position
    private Inventory inventory; // The player's inventory to store collected items
    private int visibilityDiameter;

    /*
     * EFFECTS: initializes player at the starting position (0,0) with no items (key
     * or flashlight) collected
     */
    public Player() {
        this(1);
    }

    /*
     * EFFECTS: initializes player at the specified level number with the starting
     * position (0, 0) and no items collected
     */
    public Player(int levelNumber) {
        this(levelNumber, 0, 0);
    }

    /*
     * EFFECTS: initializes player at the specified starting position (startX,
     * startY)
     * with no items collected. Sets the visibility diameter based on level number.
     */
    public Player(int levelNumber, int startX, int startY) {
        this.playerX = startX;
        this.playerY = startY;
        this.inventory = new Inventory(); // Initialize player's inventory
        if (levelNumber == 1) {
            this.visibilityDiameter = 19;
        } else {
            this.visibilityDiameter = 7;
        }
    }

    /*
     * REQUIRES: direction is one of "up", "down", "left", "right", "w", "a", "s",
     * or "d"
     * MODIFIES: this
     * EFFECTS: Moves the player one unit in the specified direction. Direction
     * input is case-insensitive.
     */
    @SuppressWarnings("methodlength")
    public void move(String direction) {
        switch (direction.toLowerCase()) {
            case "down":
            case "s":
                playerY += 1;
                EventLog.getInstance().logEvent(new Event("Player moved down"));
                break;
            case "up":
            case "w":
                playerY -= 1;
                EventLog.getInstance().logEvent(new Event("Player moved up"));
                break;
            case "left":
            case "a":
                playerX -= 1;
                EventLog.getInstance().logEvent(new Event("Player moved left"));
                break;
            case "right":
            case "d":
                playerX += 1;
                EventLog.getInstance().logEvent(new Event("Player moved right"));
                break;
            default:
                break;
        }
    }

    /*
     * MODIFIES: this
     * EFFECTS: sets the x-coordinate of the player's position
     */
    public void setPlayerX(int x) {
        this.playerX = x;
    }

    /*
     * MODIFIES: this
     * EFFECTS: sets the y-coordinate of the player's position
     */
    public void setPlayerY(int y) {
        this.playerY = y;
    }

    /*
     * MODIFIES: this
     * EFFECTS: sets the player's position to the specified coordinates (x, y)
     */
    public void setPosition(int x, int y) {
        this.playerX = x;
        this.playerY = y;
    }

    /*
     * MODIFIES: this
     * EFFECTS: Adds the given item to the player's inventory.
     * If the item is a flashlight, expands the player's vision.
     * If the item is a key, acknowledges its collection.
     */
    public void collectItem(Item item) {
        inventory.addItem(item);
        item.setCollected(true);
        System.out.println("Collected " + item.getType());
        // Log the event for collecting an item
        EventLog.getInstance().logEvent(new Event("Collected " + item.getType()));

        // If the item is a flashlight, expand the player's vision
        if (item.getType().equals("flashlight")) {
            this.visibilityDiameter = 11;
            System.out.println("Vision expanded!");
            EventLog.getInstance().logEvent(new Event("Flashlight collected, vision expanded"));
        }

        // If the item is a key, display key collection message
        if (item.getType().equals("key")) {
            System.out.println("You can now unlock the door!");
            EventLog.getInstance().logEvent(new Event("Key collected, door can now be unlocked"));
        }
    }

    /*
     * EFFECTS: returns the x-coordinate of the player's position
     */
    public int getPlayerX() {
        return playerX;
    }

    /*
     * EFFECTS: returns the y-coordinate of the player's position
     */
    public int getPlayerY() {
        return playerY;
    }

    /*
     * EFFECTS: Returns whether the player has collected the key
     */
    public boolean hasKey() {
        return inventory.hasItem("key");
    }

    /*
     * EFFECTS: Returns true if the player has collected a flashlight.
     */
    public boolean hasFlashlight() {
        return inventory.hasItem("flashlight");
    }

    /*
     * EFFECTS: Displays all items currently in the player's inventory.
     */
    public void displayInventory() {
        inventory.displayInventory();
    }

    /*
     * EFFECTS: Returns the visibility diameter of the player
     */
    public int getVisibilityDiameter() {
        return this.visibilityDiameter;
    }

    /*
     * MODIFIES: this
     * EFFECTS: sets the visibility diameter of the player
     */
    public void setVisibilityDiameter(int visibilityDiameter) {
        this.visibilityDiameter = visibilityDiameter;
    }

    /*
     * MODIFIES: this
     * EFFECTS: resets the player's position to the starting position (0, 0)
     */
    public void resetPosition() {
        this.playerX = 0;
        this.playerY = 0;
    }

    /*
     * MODIFIES: this
     * EFFECTS: sets the player's inventory
     */
    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    /*
     * EFFECTS: Returns the incentory of the player
     */
    public Inventory getInventory() {
        return inventory;
    }

    public void clearInventory() {
        this.inventory.clearInventory();
    }

    /*
     * EFFECTS: Returns a JSONObject representing the player, including
     * the player's position, visibility, and inventory.
     */
    @Override
    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("playerX", playerX);
        json.put("playerY", playerY);
        json.put("visibilityDiameter", visibilityDiameter);
        json.put("inventory", inventory.toJson()); // Assuming Inventory implements Writable
        return json;
    }

    /*
     * REQUIRES: jsonObject is a valid JSON object representing a Player.
     * EFFECTS: Reconstructs a Player object from its JSON representation.
     */
    public static Player fromJson(JSONObject jsonObject) {
        int playerX = jsonObject.getInt("playerX");
        int playerY = jsonObject.getInt("playerY");
        int visibilityDiameter = jsonObject.getInt("visibilityDiameter");
        Inventory inventory = Inventory.fromJson(jsonObject.getJSONObject("inventory")); // Assuming Inventory has a
                                                                                         // fromJson method

        Player player = new Player(); // Create a new Player object
        player.setPosition(playerX, playerY); // Set the player's position
        player.setVisibilityDiameter(visibilityDiameter); // Set the player's visibility diameter
        player.setInventory(inventory); // Set the player's inventory

        return player;
    }

}
