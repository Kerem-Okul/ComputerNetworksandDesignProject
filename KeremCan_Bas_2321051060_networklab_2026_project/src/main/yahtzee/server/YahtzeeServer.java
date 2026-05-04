package main.yahtzee.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class YahtzeeServer {
    private static final int PORT = 8080;

    public static void main(String[] args) {
        System.out.println("[SERVER] Starting Yahtzee Server on port " + PORT + "...");

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("[SERVER] Listening for connections...");

            while (true) {
                System.out.println("[SERVER] Waiting for Player 1...");
                Socket player1 = serverSocket.accept();
                System.out.println("[SERVER] Player 1 connected from " + player1.getInetAddress());

                System.out.println("[SERVER] Waiting for Player 2...");
                Socket player2 = serverSocket.accept();
                System.out.println("[SERVER] Player 2 connected from " + player2.getInetAddress());

                System.out.println("[SERVER] Two players connected. Starting a new Game Session thread.");

                GameSession session = new GameSession(player1, player2);
                new Thread(session).start();
            }

        } catch (IOException e) {
            System.err.println("[SERVER] Fatal Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
