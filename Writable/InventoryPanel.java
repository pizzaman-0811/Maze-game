package ui;

import model.Inventory;
import model.Item;

import javax.swing.*;
import java.awt.*;

/**
 * Represents the inventory panel in the Maze Game GUI.
 * Handles displaying, managing, and filtering the player's inventory,
 * including adding items such as keys and flashlights.
 */
public class InventoryPanel extends JPanel {
    private Inventory inventory; // Represents the player's inventory
    private JTextArea inventoryArea; // Text area for displaying inventory items
    private JComboBox<String> filterComboBox; // Dropdown for filter options

    /**
     * REQUIRES: None
     * MODIFIES: this
     * EFFECTS: Initializes the inventory panel with a text area for displaying
     * items,
     * buttons for adding items, and a filter option for displaying specific item
     * types.
     */
    public InventoryPanel() {
        inventory = new Inventory(); // Initialize an empty inventory
        setLayout(new BorderLayout());

        // Text area to display inventory
        inventoryArea = new JTextArea();
        inventoryArea.setEditable(false);
        add(new JScrollPane(inventoryArea), BorderLayout.CENTER);

        // Panel for buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton addKeyButton = new JButton("Add Key");
        JButton addFlashlightButton = new JButton("Add Flashlight");
        JButton filterItemsButton = new JButton("Filter Items");

        // Filter dropdown
        String[] filterOptions = { "All", "Keys", "Flashlights" };
        filterComboBox = new JComboBox<>(filterOptions);
        filterComboBox.setSelectedIndex(0);

        // Add button actions
        addKeyButton.addActionListener(e -> addItem("key"));
        addFlashlightButton.addActionListener(e -> addItem("flashlight"));
        filterItemsButton.addActionListener(e -> updateInventory((String) filterComboBox.getSelectedItem()));

        // Add buttons to the panel
        buttonPanel.add(addKeyButton);
        buttonPanel.add(addFlashlightButton);
        buttonPanel.add(new JLabel("Filter:"));
        buttonPanel.add(filterComboBox);
        buttonPanel.add(filterItemsButton);

        add(buttonPanel, BorderLayout.SOUTH);

        updateInventory("All"); // Refresh inventory display
    }

    /**
     * MODIFIES: inventory
     * EFFECTS: Adds a new item of the specified type (e.g., "key", "flashlight") to
     * the inventory
     * and updates the display.
     * 
     * @param type the type of item to add
     */
    private void addItem(String type) {
        inventory.addItem(new Item(type, 0, 0)); // Add item with dummy coordinates
        updateInventory("All"); // Refresh inventory display
    }

    /**
     * MODIFIES: inventoryArea
     * EFFECTS: Updates the text area to display the current items in the inventory,
     * filtered by the specified item type.
     * 
     * @param filter the filter to apply ("All", "Keys", or "Flashlights")
     */
    private void updateInventory(String filter) {
        StringBuilder sb = new StringBuilder("Inventory:\n");
        inventory.getItems().stream()
                .filter(item -> filter.equals("All")
                        || (filter.equals("Keys") && item.getType().equals("key"))
                        || (filter.equals("Flashlights") && item.getType().equals("flashlight")))
                .forEach(item -> sb.append("- ").append(item.getType()).append("\n"));
        inventoryArea.setText(sb.toString());
    }
}
