package model;

import java.util.ArrayList;
import java.util.List;

import persistence.Writable;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Represents the player's inventory, which stores collected items such as keys
 * and flashlights.
 * Provides methods to add items, check for specific items, and display the
 * inventory.
 */
public class Inventory implements Writable {

    private List<Item> items; // List to hold collected items

    /*
     * REQUIRES: none
     * MODIFIES: this
     * EFFECTS: Initializes an empty inventory.
     */
    public Inventory() {
        items = new ArrayList<>();
    }

    /*
     * REQUIRES: item is not null
     * MODIFIES: this
     * EFFECTS: Adds the specified item to the inventory.
     */
    public void addItem(Item item) {
        items.add(item);
        EventLog.getInstance().logEvent(new Event("Added item: " + item.getType() + " into inventory"));
    }

    /*
     * REQUIRES: itemType is not null
     * EFFECTS: Returns true if an item of the specified type is in the inventory,
     * false otherwise.
     */
    public boolean hasItem(String itemType) {
        for (Item item : items) {
            if (item.getType().equals(itemType)) {
                EventLog.getInstance().logEvent(new Event("Checked for item: " + itemType + " - Found"));
                return true;
            }
        }
        EventLog.getInstance().logEvent(new Event("Checked for item: " + itemType + " - Not found"));
        return false;
    }

    /*
     * REQUIRES: none
     * EFFECTS: Displays all items in the inventory. If the inventory is empty,
     * prints a message indicating so.
     */
    public void displayInventory() {
        if (items.isEmpty()) {
            System.out.println("Inventory is empty.");
        } else {
            System.out.println("Inventory contains:");
            for (Item item : items) {
                System.out.println("- " + item.getType());
            }
        }
    }

    /*
     * REQUIRES: none
     * EFFECTS: Returns a list of all items in the inventory.
     */
    public List<Item> getItems() {
        return items;
    }

    /*
     * REQUIRES: none
     * MODIFIES: this
     * EFFECTS: Clears all items from the inventory.
     */
    public void clearInventory() {
        EventLog.getInstance().logEvent(new Event("Cleared inventory"));
        items.clear();
    }

    /*
     * EFFECTS: Returns a JSONObject representing the inventory, including
     * all items collected by the player.
     */
    @Override
    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        JSONArray jsonItems = new JSONArray();
        for (Item item : items) {
            jsonItems.put(item.toJson()); // Assuming Item implements Writable
        }
        json.put("items", jsonItems);
        return json;
    }

    /*
     * REQUIRES: jsonObject is a valid JSON representation of an inventory.
     * EFFECTS: Parses the inventory from JSON and returns it as an Inventory
     * object.
     */
    public static Inventory fromJson(JSONObject jsonObject) {
        Inventory inventory = new Inventory();
        JSONArray jsonItems = jsonObject.getJSONArray("items");
        for (int i = 0; i < jsonItems.length(); i++) {
            JSONObject jsonItem = jsonItems.getJSONObject(i);
            Item item = Item.fromJson(jsonItem); // Assuming Item has a fromJson method
            inventory.addItem(item);
        }
        return inventory;
    }

}
