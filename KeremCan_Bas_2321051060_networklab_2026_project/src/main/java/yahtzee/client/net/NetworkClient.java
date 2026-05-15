package main.java.yahtzee.client.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.function.Consumer;

public class NetworkClient {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private boolean isConnected = false;

    // This callback allows the network thread to send messages back to the UI
    private final Consumer<String> onMessageReceived;

    public NetworkClient(Consumer<String> onMessageReceived) {
        this.onMessageReceived = onMessageReceived;
    }

    public void connect(String ip, int port) throws IOException {
        socket = new Socket(ip, port);
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        isConnected = true;

        // Start the background listening thread
        new Thread(this::listenToServer).start();
    }

    private void listenToServer() {
        try {
            String message;
            while (isConnected && (message = in.readLine()) != null) {
                // Pass the raw string protocol message to the UI
                onMessageReceived.accept(message);
            }
        } catch (IOException e) {
            if (isConnected) {
                onMessageReceived.accept("ERROR:Connection to server lost.");
            }
        } finally {
            disconnect();
        }
    }

    public void sendCommand(String command) {
        if (isConnected && out != null) {
            out.println(command);
        }
    }

    public void disconnect() {
        isConnected = false;

        // We run the shutdown on a background thread so it NEVER blocks the JavaFX UI
        new Thread(() -> {
            try {
                // Closes the raw socket natively.
                // This instantly unblocks the readLine() loop without deadlocking.
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
            } catch (IOException e) {
                System.out.println("[CLIENT] Socket closed safely.");
            }
        }).start();
    }
}
