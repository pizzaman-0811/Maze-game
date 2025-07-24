package ui;

import java.util.Scanner;
// Represents the console-based menu for the Maze Game, allowing the player to start a game, view instructions, or exit.

public class ConsoleMenu {
    /*
     * EFFECTS: Displays the main menu options and prompts the user to enter a
     * choice.
     * Returns the user's choice as an integer.
     */

    public static int displayMainMenu() {
        System.out.println("Welcome to the Maze Game!");
        System.out.println("1. Start Game");
        System.out.println("2. Save Game");
        System.out.println("3. Load Game(Click Start Game after load your game)");
        System.out.println("4. View Instructions");
        System.out.println("5. Exit");
        System.out.print("Enter your choice: ");
        Scanner in = new Scanner(System.in);
        return in.nextInt();
    }
    /*
     * EFFECTS: Displays the instructions on how to play the Maze Game.
     */

    public static void displayInstructions() {
        System.out.println("Instructions:");
        System.out.println("Navigate the maze to reach the exit.");
        System.out.println("Use the following commands: up(W), down(S), left(A), right(D).");
        System.out.println("Collect flashlight to aid your journey.(In level 2 and 3)");
        System.out.println("You must collect the key to exit each level.");
        System.out.println("Avoid ghosts or get sent back to the maze's start.(Only in level 3)");
        System.out.println("Good luck!");
    }
}
