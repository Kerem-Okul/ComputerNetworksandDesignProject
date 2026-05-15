package main.java.yahtzee.cli;

import main.java.yahtzee.engine.Category;
import main.java.yahtzee.engine.GameEngine;

import java.util.Map;
import java.util.Scanner;

public class ConsoleApp {
    public static void main(String[] args) {
        GameEngine engine = new GameEngine();
        Scanner scanner = new Scanner(System.in);

        System.out.println("=====================================");
        System.out.println("   YAHTZEE ENGINE TEST ENVIRONMENT   ");
        System.out.println("=====================================");

        while (!engine.isGameOver()) {
            engine.startTurn();
            boolean turnOver = false;

            System.out.println("\n--- NEW TURN STARTED ---");

            while (!turnOver) {
                System.out.println("\nDice: " + engine.getDiceDisplay());
                System.out.println("Rolls remaining: " + engine.getRollsRemaining());
                System.out.println("Commands: [roll] | [keep 0 1 2] | [score FULL_HOUSE] | [board]");
                System.out.print("> ");

                String input = scanner.nextLine().trim().toUpperCase();
                if (input.isEmpty()) continue;

                String[] parts = input.split("\\s+");
                String command = parts[0];

                switch (command) {
                    case "ROLL":
                        if (!engine.roll()) {
                            System.out.println("[!] No rolls remaining! You must score a category.");
                        }
                        break;

                    case "KEEP":
                        for (int i = 1; i < parts.length; i++) {
                            try {
                                int index = Integer.parseInt(parts[i]);
                                if (index >= 0 && index < 5) {
                                    engine.toggleKeep(index);
                                } else {
                                    System.out.println("[!] Invalid index: " + index + " (Use 0-4)");
                                }
                            } catch (NumberFormatException e) {
                                System.out.println("[!] Invalid number format: " + parts[i]);
                            }
                        }
                        break;

                    case "SCORE":
                        if (parts.length < 2) {
                            System.out.println("[!] Specify a category! Example: SCORE THREE_OF_A_KIND");
                            break;
                        }
                        try {
                            Category cat = Category.valueOf(parts[1]);
                            int points = engine.scoreTurn(cat);

                            if (points == -1) {
                                System.out.println("[!] Category already used! Pick another one.");
                            } else {
                                System.out.println("[+] Scored " + points + " points in " + cat.name() + "!");
                                turnOver = true;
                            }
                        } catch (IllegalArgumentException e) {
                            System.out.println("[!] Unknown category. Type 'board' to see valid names.");
                        }
                        break;

                    case "BOARD":
                        printScorecard(engine);
                        break;

                    default:
                        System.out.println("[!] Unknown command.");
                }
            }
        }

        System.out.println("\n=====================================");
        System.out.println(" GAME OVER! Final Score: " + engine.getTotalScore());
        System.out.println("=====================================");
        scanner.close();
    }

    private static void printScorecard(GameEngine engine) {
        System.out.println("\n--- CURRENT SCORECARD ---");
        Map<Category, Integer> scores = engine.getCurrentScores();

        for (Category cat : Category.values()) {
            String scoreStr = scores.containsKey(cat) ? String.valueOf(scores.get(cat)) : "[ ]";
            System.out.printf("%-18s : %s%n", cat.name(), scoreStr);
        }
        System.out.println("-------------------------");
        System.out.println("TOTAL SCORE        : " + engine.getTotalScore());
        System.out.println("-------------------------\n");
    }
}
