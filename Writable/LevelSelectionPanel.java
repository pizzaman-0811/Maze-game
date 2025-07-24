package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * Represents the level selection panel in the Maze Game GUI.
 * Allows the player to choose a difficulty level before starting the game.
 */
public class LevelSelectionPanel extends JPanel {
    private JComboBox<String> levelComboBox; // Dropdown for difficulty levels
    private JButton confirmButton; // Button to confirm level selection

    /**
     * REQUIRES: onConfirm is a valid ActionListener
     * MODIFIES: this
     * EFFECTS: Initializes the level selection panel with a title, a dropdown
     * for selecting difficulty levels, and a confirm button.
     * 
     * @param onConfirm the action to perform when the confirm button is pressed
     */
    public LevelSelectionPanel(ActionListener onConfirm) {
        setLayout(new BorderLayout());

        // Title label
        JLabel titleLabel = new JLabel("Select Difficulty Level", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        add(titleLabel, BorderLayout.NORTH);

        // Dropdown for difficulty levels
        String[] levels = { "Easy (1)", "Medium (2)", "Hard (3)" };
        levelComboBox = new JComboBox<>(levels);
        levelComboBox.setFont(new Font("Arial", Font.PLAIN, 16));
        levelComboBox.setSelectedIndex(0);

        JPanel centerPanel = new JPanel();
        centerPanel.add(new JLabel("Difficulty:"));
        centerPanel.add(levelComboBox);
        add(centerPanel, BorderLayout.CENTER);

        // Confirm button
        confirmButton = new JButton("Start Game");
        confirmButton.setFont(new Font("Arial", Font.BOLD, 16));
        confirmButton.addActionListener(e -> onConfirm.actionPerformed(e));
        add(confirmButton, BorderLayout.SOUTH);

        setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));
    }

    public void updateLevelAvailability(boolean[] levelCompleted) {
        for (int i = 0; i < levelComboBox.getItemCount(); i++) {
            boolean enabled = (i == 0) || levelCompleted[i - 1]; // Level 1 is always enabled
            levelComboBox.setEnabled(enabled); // Enable/disable levels in the dropdown
        }
    }

    /**
     * Updates the availability of levels in the level selection dropdown based on
     * the completion status.
     * 
     * MODIFIES: levelComboBox
     * EFFECTS: Iterates through all levels in the levelComboBox and enables or
     * disables them based on the
     * provided levelCompleted array. Level 1 (index 0) is always enabled, while
     * subsequent levels
     * are enabled only if their prerequisite level (previous index) is marked as
     * completed.
     */
    public int getSelectedLevel() {
        return levelComboBox.getSelectedIndex() + 1; // Levels are 1-indexed
    }
}
