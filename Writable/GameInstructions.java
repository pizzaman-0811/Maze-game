package ui;

import javax.swing.*;

// Represents the instructions panel in the Maze Game GUI. 
// Displays information about how to play the game.

public class GameInstructions extends JPanel {
    /**
     * REQUIRES: None
     * MODIFIES: this
     * EFFECTS: Initializes the instructions panel with non-editable text detailing
     * the game controls and objectives. The instructions are displayed
     * in a scrollable text area.
     */
    public GameInstructions() {
        JTextArea instructions = new JTextArea();
        instructions.setEditable(false);
        instructions.setText("""
                Instructions:
                - Use W/A/S/D to move.
                - Collect keys to unlock exits.
                - Collect flashlight to support your adventure in Level 2 and 3.
                - Avoid ghosts in Level 3.
                """);

        add(new JScrollPane(instructions));
    }
}
