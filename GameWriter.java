package persistence;

import org.json.JSONObject;
import ui.GamePanel;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.File;

// Referenced from the JsonSerialization Demo
// https://github.students.cs.ubc.ca/CPSC210/JsonSerializationDemo
/**
 * Represents a writer that writes JSON representation of the game state to a
 * file.
 */
public class GameWriter {
    private static final int TAB = 4; // Number of spaces for indentation in the JSON file
    private PrintWriter writer;
    private String destination;

    /*
     * REQUIRES: destination is not null
     * EFFECTS: constructs writer to write to the destination file
     */
    public GameWriter(String destination) {
        this.destination = destination;
    }

    /*
     * MODIFIES: this
     * EFFECTS: opens the writer; throws FileNotFoundException if the destination
     * file cannot be opened for writing
     */
    public void open() throws FileNotFoundException {
        writer = new PrintWriter(new File(destination));
    }

    /*
     * MODIFIES: this
     * EFFECTS: writes the JSON representation of the game state to the file
     */
    public void writeGameCompleteStatus(GamePanel gamePanel) {
        JSONObject json = new JSONObject();
        json.put("levelCompleted", gamePanel.getLevelCompleted());
        saveToFile(json.toString(TAB)); // Save the JSON string to the file with indentation
    }

    /*
     * MODIFIES: this
     * EFFECTS: writes the full JSON representation of the game state to the file
     */
    public void write(GamePanel gamePanel) {
        // Serialize the entire GamePanel state
        JSONObject json = gamePanel.toJson();
        saveToFile(json.toString(TAB)); // Save the JSON string to the file with indentation
    }

    /*
     * MODIFIES: this
     * EFFECTS: closes the writer
     */
    public void close() {
        writer.close();
    }

    /*
     * MODIFIES: this
     * EFFECTS: saves the given string to the file
     */
    public void saveToFile(String json) {
        writer.print(json);
    }

    /*
     * MODIFIES: this
     * EFFECTS: returns the destination of the file
     */
    public String getDestination() {
        return destination;
    }

    /*
     * MODIFIES: this
     * EFFECTS: returns the tab size of the file
     */
    public static int getTabSize() {
        return TAB;
    }
}
