package model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import persistence.Writable;
import org.json.JSONObject;

// Represents a ghost in a maze with a position and movement logic.
public class Ghost implements Writable {
    private int ghostX; // The x-coordinate of the ghost's position
    private int ghostY; // The y-coordinate of the ghost's position
    private boolean movingVertically; // Indicates if the ghost is moving vertically
    private boolean movingPositiveDirection; // Indicates if the ghost is moving in a positive direction
    private Random random; // Random instance for generating random movement
    private int[][] mazeStructure; // Structure of the maze in which the ghost moves

    /*
     * REQUIRES: ghostX and ghostY are valid coordinates within the mazeStructure
     * EFFECTS: Constructs a ghost with the given position and maze structure.
     */
    public Ghost(int ghostX, int ghostY, int[][] mazeStructure) {
        this.ghostX = ghostX;
        this.ghostY = ghostY;
        this.mazeStructure = mazeStructure;
        this.random = new Random();
    }

    /*
     * MODIFIES: this
     * EFFECTS: Moves the ghost to a random neighboring cell that is a path (not a
     * wall),
     * if there are valid moves available.
     */

    public void moveToNeighbor() {
        List<int[]> possibleMoves = new ArrayList<>();

        // Check the neighboring cells of the current position for available paths
        // up (ghostY - 1, ghostX)
        if (this.ghostY - 1 >= 0 && this.mazeStructure[this.ghostY - 1][this.ghostX] == 0) {
            possibleMoves.add(new int[] { this.ghostX, this.ghostY - 1 });
        }
        // down (ghostY + 1, ghostX)
        if (this.ghostY + 1 < this.mazeStructure.length && this.mazeStructure[this.ghostY + 1][this.ghostX] == 0) {
            possibleMoves.add(new int[] { this.ghostX, this.ghostY + 1 });
        }
        // left (ghostY, ghostX - 1)
        if (this.ghostX - 1 >= 0 && this.mazeStructure[this.ghostY][this.ghostX - 1] == 0) {
            possibleMoves.add(new int[] { this.ghostX - 1, this.ghostY });
        }
        // right (ghostY, ghostX + 1)
        if (this.ghostX + 1 < this.mazeStructure[0].length && this.mazeStructure[this.ghostY][this.ghostX + 1] == 0) {
            possibleMoves.add(new int[] { this.ghostX + 1, this.ghostY });
        }

        // If there is an available cell, randomly choose the next move
        if (!possibleMoves.isEmpty()) {
            int[] nextMove = possibleMoves.get(random.nextInt(possibleMoves.size()));
            this.ghostX = nextMove[0];
            this.ghostY = nextMove[1];
        }
    }

    /*
     * MODIFIES: this
     * EFFECTS: Sets a random position for the ghost within the maze boundaries.
     * Ensures the ghost is placed on a path (not a wall).
     */
    public void setRandomPosition(int[][] maze) {
        int fixedX;
        int fixedY; // Use these fixed values for testing consistency
        do {
            fixedX = random.nextInt(maze[0].length);
            fixedY = random.nextInt(maze.length);
        } while (maze[fixedY][fixedX] != 0);
        this.ghostX = fixedX;
        this.ghostY = fixedY;
    }

    /*
     * MODIFIES: this
     * EFFECTS: Moves the ghost in its designated direction (either horizontally or
     * vertically) within the maze.
     */
    public void move(int[][] maze) {
        if (movingVertically) {
            moveVertical(maze);
        } else {
            moveHorizontal(maze);
        }
    }

    /*
     * MODIFIES: this
     * EFFECTS: Moves the ghost horizontally in the maze. Changes direction if the
     * ghost reaches the edge or a wall.
     */
    private void moveHorizontal(int[][] maze) {
        if (movingPositiveDirection) {
            if (ghostX + 1 < maze[0].length && maze[ghostY][ghostX + 1] == 0) {
                ghostX++;
            } else {
                movingPositiveDirection = false;
            }
        } else {
            if (ghostX - 1 >= 0 && maze[ghostY][ghostX - 1] == 0) {
                ghostX--;
            } else {
                movingPositiveDirection = true;
            }
        }
    }

    /*
     * MODIFIES: this
     * EFFECTS: Moves the ghost vertically in the maze. Changes direction if the
     * ghost reaches the edge or a wall.
     */
    private void moveVertical(int[][] maze) {
        if (movingPositiveDirection) {
            if (ghostY + 1 < maze.length && maze[ghostY + 1][ghostX] == 0) {
                ghostY++;
            } else {
                movingPositiveDirection = false;
            }
        } else {
            if (ghostY - 1 >= 0 && maze[ghostY - 1][ghostX] == 0) {
                ghostY--;
            } else {
                movingPositiveDirection = true;
            }
        }
    }

    /*
     * MODIFIES: this
     * EFFECTS: Resets the ghost's position to a random position within the maze.
     */
    public void resetPosition(int[][] maze) {
        setRandomPosition(maze);
    }

    /*
     * EFFECTS: Returns the x-coordinate of the ghost's position.
     */
    public int getGhostX() {
        return ghostX;
    }

    /*
     * EFFECTS: Returns the y-coordinate of the ghost's position.
     */
    public int getGhostY() {
        return ghostY;
    }

    /*
     * MODIFIES: this
     * EFFECTS: Sets the ghost's position to the specified coordinates.
     */
    public void setPosition(int x, int y) {
        this.ghostX = x;
        this.ghostY = y;
    }

    /*
     * MODIFIES: this
     * EFFECTS: Sets whether the ghost moves vertically or not.
     */
    public void setVerticalMovement(boolean movingVertically) {
        this.movingVertically = movingVertically;
    }

    /*
     * MODIFIES: this
     * EFFECTS: Sets whether the ghost moves in a positive direction or not.
     */
    public void setPositiveDirection(boolean movingPositiveDirection) {
        this.movingPositiveDirection = movingPositiveDirection;
    }

    /*
     * EFFECTS: Returns a JSONObject representing the ghost, including
     * the ghost's position in the maze.
     */
    @Override
    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("ghostX", ghostX);
        json.put("ghostY", ghostY);
        return json;
    }

    public static Ghost fromJson(JSONObject jsonObject, int[][] maze) {
        int ghostX = jsonObject.getInt("ghostX");
        int ghostY = jsonObject.getInt("ghostY");
        return new Ghost(ghostX, ghostY, maze);
    }

}
