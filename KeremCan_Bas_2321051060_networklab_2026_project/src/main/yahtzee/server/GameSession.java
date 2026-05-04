package main.yahtzee.server;

import main.yahtzee.engine.GameEngine;

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

            sendMessage(p1Out, "INFO:Welcome Player 1! Game is starting.");
            sendMessage(p2Out, "INFO:Welcome Player 2! Game is starting.");

            // TODO for Day 5: Implement the actual turn-based game loop here.

            String p1Message;
            while ((p1Message = p1In.readLine()) != null) {
                System.out.println("[SESSION] Received from P1: " + p1Message);

                if (p1Message.equalsIgnoreCase("QUIT")) break;

                sendMessage(p1Out, "ECHO: " + p1Message);
                sendMessage(p2Out, "OPPONENT_DID: " + p1Message);
            }

        } catch (IOException e) {
            System.err.println("[SESSION] Communication error: " + e.getMessage());
        } finally {
            cleanup();
        }
    }

    private void setupStreams() throws IOException {
        p1In = new BufferedReader(new InputStreamReader(player1.getInputStream()));
        p1Out = new PrintWriter(player1.getOutputStream(), true);

        p2In = new BufferedReader(new InputStreamReader(player2.getInputStream()));
        p2Out = new PrintWriter(player2.getOutputStream(), true);
    }

    private void sendMessage(PrintWriter out, String message) {
        if (out != null) {
            out.println(message);
        }
    }
    
    private void cleanup() {
        System.out.println("[SESSION] Closing connections.");
        try {
            if (player1 != null) player1.close();
            if (player2 != null) player2.close();
        } catch (IOException e) {
            System.err.println("[SESSION] Error closing sockets.");
        }
    }
}
