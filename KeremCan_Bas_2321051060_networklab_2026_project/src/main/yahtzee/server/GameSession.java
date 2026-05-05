package main.yahtzee.server;

import main.yahtzee.engine.GameEngine;
import main.yahtzee.engine.Category;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class GameSession implements Runnable {
    private final Socket player1;
    private final Socket player2;
    private final GameEngine p1Engine;
    private final GameEngine p2Engine;

    // IO Streams
    private BufferedReader p1In;
    private PrintWriter p1Out;
    private BufferedReader p2In;
    private PrintWriter p2Out;

    // State Management
    private int activePlayer = 1;
    private boolean isRunning = true;

    public GameSession(Socket player1, Socket player2) {
        this.player1 = player1;
        this.player2 = player2;

        this.p1Engine = new GameEngine();
        this.p2Engine = new GameEngine();
    }

    @Override
    public void run() {
        try {
            setupStreams();

            // Handshake and Game Start
            sendMessage(p1Out, "INFO:Welcome Player 1! Opponent found.");
            sendMessage(p2Out, "INFO:Welcome Player 2! Opponent found.");

            p1Engine.startTurn(); // Initialize P1's first dice state
            broadcastTurnState();

            // Start two separate listener threads so neither client blocks the other
            Thread p1Listener = new Thread(() -> listenToClient(1, p1In));
            Thread p2Listener = new Thread(() -> listenToClient(2, p2In));

            p1Listener.start();
            p2Listener.start();

            // The main session thread waits for the game to end
            p1Listener.join();
            p2Listener.join();

        } catch (Exception e) {
            System.err.println("[SESSION] Game interrupted: " + e.getMessage());
        } finally {
            cleanup();
        }
    }

    private void listenToClient(int playerId, BufferedReader in) {
        try {
            String message;
            while (isRunning && (message = in.readLine()) != null) {
                System.out.println("[P" + playerId + "] " + message);

                if (message.equalsIgnoreCase("QUIT")) {
                    handleDisconnect(playerId);
                    break;
                }

                // Route the command to the synchronized engine processor
                processCommand(playerId, message.trim().toUpperCase());
            }
        } catch (IOException e) {
            handleDisconnect(playerId);
        }
    }

    private synchronized void processCommand(int playerId, String command) {
        PrintWriter out = (playerId == 1) ? p1Out : p2Out;

        // 1. Enforce Turn Order
        if (playerId != activePlayer) {
            sendMessage(out, "ERROR:Not your turn!");
            return;
        }

        GameEngine engine = (playerId == 1) ? p1Engine : p2Engine;
        String[] parts = command.split(":");
        String action = parts[0];

        // 2. Process Actions
        switch (action) {
            case "ROLL":
                if (engine.roll()) {
                    broadcastDiceState(engine);
                } else {
                    sendMessage(out, "ERROR:No rolls remaining, you must SCORE.");
                }
                break;

            case "KEEP":
                // Format: KEEP:0,2,4
                if (parts.length > 1) {
                    String[] indices = parts[1].split(",");
                    for (String idxStr : indices) {
                        try {
                            engine.toggleKeep(Integer.parseInt(idxStr.trim()));
                        } catch (NumberFormatException ignored) {
                        }
                    }
                    broadcastDiceState(engine);
                }
                break;

            case "SCORE":
                // Format: SCORE:FULL_HOUSE
                if (parts.length < 2) {
                    sendMessage(out, "ERROR:Specify a category (e.g., SCORE:FULL_HOUSE).");
                    break;
                }
                try {
                    Category cat = Category.valueOf(parts[1]);
                    int points = engine.scoreTurn(cat);

                    if (points == -1) {
                        sendMessage(out, "ERROR:Category already used.");
                    } else {
                        // Successful Score! Broadcast results.
                        broadcastToBoth("SCORE_UPDATE:Player " + activePlayer + " scored " + points + " in " + cat.name());

                        // Check for Game Over
                        if (p1Engine.isGameOver() && p2Engine.isGameOver()) {
                            handleGameOver();
                        } else {
                            switchTurn();
                        }
                    }
                } catch (IllegalArgumentException e) {
                    sendMessage(out, "ERROR:Unknown category.");
                }
                break;

            default:
                sendMessage(out, "ERROR:Unknown command.");
        }
    }

    private void switchTurn() {
        activePlayer = (activePlayer == 1) ? 2 : 1;
        GameEngine nextEngine = (activePlayer == 1) ? p1Engine : p2Engine;

        nextEngine.startTurn();
        broadcastTurnState();
        broadcastDiceState(nextEngine);
    }

    private void broadcastTurnState() {
        if (activePlayer == 1) {
            sendMessage(p1Out, "TURN:YOUR_TURN");
            sendMessage(p2Out, "TURN:OPPONENT_TURN");
        } else {
            sendMessage(p2Out, "TURN:YOUR_TURN");
            sendMessage(p1Out, "TURN:OPPONENT_TURN");
        }
    }

    private void broadcastDiceState(GameEngine activeEngine) {
        String diceStr = "DICE:" + activeEngine.getDiceDisplay() + " | Rolls left: " + activeEngine.getRollsRemaining();
        sendMessage(p1Out, diceStr);
        sendMessage(p2Out, diceStr);
    }

    private void handleGameOver() {
        isRunning = false;
        int p1Score = p1Engine.getTotalScore();
        int p2Score = p2Engine.getTotalScore();

        String result = "GAME_OVER:P1_Score=" + p1Score + ",P2_Score=" + p2Score;
        broadcastToBoth(result);
    }

    private void handleDisconnect(int playerId) {
        if (!isRunning) return;
        isRunning = false;
        System.out.println("[SESSION] Player " + playerId + " disconnected.");
        broadcastToBoth("INFO:Opponent disconnected. Game ending.");
    }

    // --- I/O Helpers ---

    private void setupStreams() throws IOException {
        p1In = new BufferedReader(new InputStreamReader(player1.getInputStream()));
        p1Out = new PrintWriter(player1.getOutputStream(), true);
        p2In = new BufferedReader(new InputStreamReader(player2.getInputStream()));
        p2Out = new PrintWriter(player2.getOutputStream(), true);
    }

    private void sendMessage(PrintWriter out, String message) {
        if (out != null) out.println(message);
    }

    private void broadcastToBoth(String message) {
        sendMessage(p1Out, message);
        sendMessage(p2Out, message);
    }

    private void cleanup() {
        try {
            if (player1 != null) player1.close();
            if (player2 != null) player2.close();
        } catch (IOException e) {
            System.err.println("[SESSION] Error closing sockets.");
        }
    }
}
