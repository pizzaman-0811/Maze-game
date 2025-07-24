package model;

import java.util.Random;

import persistence.Writable;

import org.json.JSONArray;
import org.json.JSONObject;
// Represents a maze generated using the stick flip algorithm.
// The maze contains an entrance, exit, and paths created using a recursive stick flip process.
// The class also checks if the exit is accessible from the entrance.

public class Maze implements Writable {
    public static final int MAZE_SIZE = 19;
    private int[][] maze; // 2D array representing the maze structure
    private int[] entrance = new int[2]; // [0] = x, [1] = y
    private int[] exit = new int[2]; // [0] = x, [1] = y
    private Random random; // Random instance for generating maze paths
    private boolean exitAccessible; // Indicates if the exit is reachable from the entrance

    /*
     * EFFECTS: Constructs a new Maze of size MAZE_SIZE x MAZE_SIZE,
     * initializes the maze, creates paths using the stick flip algorithm,
     * and sets entrance and exit points. Verifies if the exit is accessible.
     */
    public Maze() {
        maze = new int[MAZE_SIZE][MAZE_SIZE];
        random = new Random();
        initialize();
        mazeCreate();
        setEntranceAndExit();
        checkExitAccessibility();
    }

    /*
     * REQUIRES: mazeStructure is a valid 2D array of size MAZE_SIZE x MAZE_SIZE
     * MODIFIES: this
     * EFFECTS: Initializes a maze with the given maze structure
     */
    public Maze(int[][] mazeStructure) {
        if (mazeStructure.length == MAZE_SIZE && mazeStructure[0].length == MAZE_SIZE) {
            this.maze = mazeStructure;
        } else {
            throw new IllegalArgumentException("Invalid maze size");
        }
    }

    /*
     * MODIFIES: this
     * EFFECTS: Initializes the maze grid with walls and paths.
     */
    private void initialize() {
        for (int i = 0; i < MAZE_SIZE; i++) {
            for (int j = 0; j < MAZE_SIZE; j++) {
                // Set the entire top and bottom rows as walls
                if (i == 0 || i == MAZE_SIZE - 1) {
                    maze[i][j] = 1;
                    // Set the leftmost and rightmost columns as walls
                } else if (j == 0 || j == MAZE_SIZE - 1) {
                    maze[i][j] = 1;
                    // Set the internal cells based on a grid pattern (even rows and columns)
                } else if (i % 2 == 0 && j % 2 == 0) {
                    maze[i][j] = 1;
                } else {
                    maze[i][j] = 0; // Set as a path initially
                }
            }
        }
    }
    /*
     * MODIFIES: this
     * EFFECTS: Applies the stick flip algorithm to create random paths in the maze.
     */

    private void mazeCreate() {
        for (int i = 2; i < MAZE_SIZE - 2; i += 2) {
            for (int j = 2; j < MAZE_SIZE - 2; j += 2) {
                boolean flag = false;
                do {
                    int randomDirection = random.nextInt(4);
                    if (i > 2 && randomDirection == 0) {
                        continue; // Skip upward direction for rows beyond the first row
                    }
                    flag = attemptPathCreation(i, j, randomDirection);
                } while (!flag);
            }
        }
    }

    /*
     * MODIFIES: this
     * EFFECTS: Attempts to create a path based on the specified direction (0: Up,
     * 1: Right, 2: Down, 3: Left).
     * Returns true if a path was successfully created; false otherwise.
     */
    private boolean attemptPathCreation(int i, int j, int direction) {
        switch (direction) {
            case 0:
                return createPathUp(i, j);
            case 1:
                return createPathRight(i, j);
            case 2:
                return createPathDown(i, j);
            case 3:
                return createPathLeft(i, j);
            default:
                return false;
        }
    }

    /*
     * EFFECTS: Creates a path upward if possible and returns true, otherwise
     * returns false.
     */
    private boolean createPathUp(int i, int j) {
        if (i > 2 && maze[i - 1][j] == 0) {
            maze[i - 1][j] = 1;
            return true;
        }
        return false;
    }

    /*
     * EFFECTS: Creates a path to the right if possible and returns true, otherwise
     * returns false.
     */
    private boolean createPathRight(int i, int j) {
        if (j < maze[i].length - 2 && maze[i][j + 1] == 0) {
            maze[i][j + 1] = 1;
            return true;
        }
        return false;
    }

    /*
     * EFFECTS: Creates a path downward if possible and returns true, otherwise
     * returns false.
     */
    private boolean createPathDown(int i, int j) {
        if (i < maze.length - 2 && maze[i + 1][j] == 0) {
            maze[i + 1][j] = 1;
            return true;
        }
        return false;
    }

    /*
     * EFFECTS: Creates a path to the left if possible and returns true, otherwise
     * returns false.
     */
    private boolean createPathLeft(int i, int j) {
        if (j > 2 && maze[i][j - 1] == 0) {
            maze[i][j - 1] = 1;
            return true;
        }
        return false;
    }
    /*
     * MODIFIES: this
     * EFFECTS: Sets the entrance at the middle of the bottom row and the exit at a
     * random position on the top row of the maze.
     */

    private void setEntranceAndExit() {
        int middleColumn = MAZE_SIZE / 2;
        maze[MAZE_SIZE - 1][middleColumn] = 0; // Entrance at the middle of the bottom row
        this.entrance[0] = middleColumn;
        this.entrance[1] = MAZE_SIZE - 1;
        int exitCol = random.nextInt(MAZE_SIZE - 2) + 1;
        maze[0][exitCol] = 0; // Random exit in the top row
        this.exit[0] = exitCol;
        this.exit[1] = 0;
    }

    /*
     * MODIFIES: this
     * EFFECTS: Checks if the exit is accessible from the entrance and sets
     * exitAccessible accordingly.
     */
    private void checkExitAccessibility() {
        boolean[][] visited = new boolean[MAZE_SIZE][MAZE_SIZE];
        exitAccessible = depthFirstSearch(MAZE_SIZE - 1, MAZE_SIZE / 2, visited);
    }
    /*
     * EFFECTS: Recursively performs a depth-first search to determine if there is
     * a path from the entrance to the exit in the maze.
     * Returns true if the exit is reachable, false otherwise.
     */

    private boolean depthFirstSearch(int row, int col, boolean[][] visited) {
        if (row < 0 || row >= MAZE_SIZE || col < 0 || col >= MAZE_SIZE || maze[row][col] == 1 || visited[row][col]) {
            return false;
        }
        if (row == 0) {
            return true; // Reached the top row where the exit is located
        }

        visited[row][col] = true;

        return depthFirstSearch(row - 1, col, visited) || depthFirstSearch(row + 1, col, visited)
                || depthFirstSearch(row, col + 1, visited) || depthFirstSearch(row, col - 1, visited);
    }

    /*
     * EFFECTS: Returns the maze as a 2D integer array where 1 represents a wall and
     * 0 represents a path.
     */
    public int[][] getMaze() {
        return maze;
    }

    /*
     * EFFECTS: Returns the coordinates of the entrance to the maze.
     */
    public int[] getEntrance() {
        return this.entrance;
    }

    /*
     * EFFECTS: Returns the coordinates of the exit of the maze.
     */
    public int[] getExit() {
        return this.exit;
    }

    /*
     * EFFECTS: Returns whether the exit is accessible from the entrance.
     */
    public boolean isExitAccessible() {
        return exitAccessible;
    }

    /*
     * MODIFIES: this
     * EFFECTS: Sets the maze structure to the provided 2D integer array
     */
    public void setMaze(int[][] newMaze) {
        if (newMaze.length == MAZE_SIZE && newMaze[0].length == MAZE_SIZE) {
            this.maze = newMaze;
        } else {
            throw new IllegalArgumentException("Invalid maze size");
        }
    }

    /*
     * EFFECTS: Returns a JSONObject representing the maze, including
     * the maze structure and exit coordinates.
     */
    @Override
    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("maze", mazeToJson()); // Serialize the 2D array (maze structure)
        json.put("entrance", new JSONArray(entrance)); // Include the entrance coordinates
        json.put("exit", new JSONArray(exit)); // Include the exit coordinates
        json.put("exitAccessible", exitAccessible); // Include exit accessibility status
        return json;
    }

    // Helper method to convert 2D maze array into JSON
    private JSONArray mazeToJson() {
        JSONArray jsonArray = new JSONArray();
        for (int[] row : maze) {
            JSONArray jsonRow = new JSONArray();
            for (int value : row) {
                jsonRow.put(value);
            }
            jsonArray.put(jsonRow);
        }
        return jsonArray;
    }

    /*
     * REQUIRES: jsonObject is a valid JSON object representing a maze.
     * EFFECTS: Reconstructs a Maze object from its JSON representation.
     */
    public static Maze fromJson(JSONObject jsonObject) {
        // Parse the maze structure
        JSONArray structureArray = jsonObject.getJSONArray("maze");
        int[][] mazeStructure = new int[MAZE_SIZE][MAZE_SIZE];
        for (int i = 0; i < structureArray.length(); i++) {
            JSONArray row = structureArray.getJSONArray(i);
            for (int j = 0; j < row.length(); j++) {
                mazeStructure[i][j] = row.getInt(j);
            }
        }

        // Create a Maze object using the parsed structure
        Maze maze = new Maze(mazeStructure);

        // Parse and set the entrance
        JSONArray entranceArray = jsonObject.getJSONArray("entrance");
        maze.entrance = new int[] { entranceArray.getInt(0), entranceArray.getInt(1) };

        // Parse and set the exit
        JSONArray exitArray = jsonObject.getJSONArray("exit");
        maze.exit = new int[] { exitArray.getInt(0), exitArray.getInt(1) };

        // Parse and set the exit accessibility
        maze.exitAccessible = jsonObject.getBoolean("exitAccessible");

        return maze;
    }

}
