package main.java.yahtzee.client.ui;

import main.java.yahtzee.client.net.NetworkClient;
import main.java.yahtzee.engine.Category;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class YahtzeeApp extends Application {
    private Stage primaryStage;
    private Scene mainScene;
    private StartScreen startScreen;
    private EndScreen endScreen;
    private GameBoardScreen gameBoardScreen;

    private NetworkClient networkClient;
    private boolean isMyTurn = false;

    private int myTotalScore = 0;
    private int opponentTotalScore = 0;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("Yahtzee Network Project");

        // Initialize Screens
        startScreen = new StartScreen();
        endScreen = new EndScreen();
        gameBoardScreen = new GameBoardScreen();

        mainScene = new Scene(startScreen, 800, 600);
        primaryStage.setScene(mainScene);

        // Setup Event Handlers
        setupEventHandlers();

        // Show Start Screen initially
        showStartScreen();
        primaryStage.show();
    }

    private void setupEventHandlers() {
        // --- 1. START SCREEN LOGIC ---
        startScreen.getConnectButton().setOnAction(e -> {
            String ip = startScreen.getIpInput().getText().trim();
            String username = startScreen.getUsernameInput().getText().trim();

            if (ip.isEmpty() || username.isEmpty()) {
                startScreen.getStatusLabel().setText("Error: IP and Username are required!");
                return;
            }

            startScreen.getStatusLabel().setText("Connecting to " + ip + ":8080...");
            startScreen.getConnectButton().setDisable(true);

            // Initialize the network client with a lambda callback for incoming messages
            networkClient = new NetworkClient(this::handleServerMessage);

            try {
                // Attempt to connect to the AWS/Local server on port 8080
                networkClient.connect(ip, 8080);
                networkClient.sendCommand("JOIN:" + username);
                startScreen.getStatusLabel().setText("Connected! Waiting for opponent...");
            } catch (Exception ex) {
                startScreen.getStatusLabel().setText("Failed to connect. Is the server running?");
                startScreen.getConnectButton().setDisable(false);
            }
        });

        // --- 2. GAME BOARD LOGIC ---
        gameBoardScreen.getRollButton().setOnAction(e -> {
            if (isMyTurn) networkClient.sendCommand("ROLL");
        });

        // Loop through the 5 dice to send keep commands
        for (int i = 0; i < 5; i++) {
            final int index = i;
            gameBoardScreen.getDiceButtons()[i].setOnAction(e -> {

                // Grab the current text of the clicked button
                String currentText = gameBoardScreen.getDiceButtons()[index].getText();

                // If it's NOT your turn, OR if the die hasn't been rolled yet ("?")
                if (!isMyTurn || currentText.equals("?")) {
                    // Revert the visual toggle instantly (force it to stay unselected/white)
                    gameBoardScreen.getDiceButtons()[index].setSelected(false);
                    return; // Stop running any more code
                }

                // If it IS your turn and the die has a real number, send the command
                networkClient.sendCommand("KEEP:" + index);
            });
        }

        // Loop through the 13 category buttons to send score commands
        for (int i = 0; i < 13; i++) {
            final String categoryName = Category.values()[i].name();
            gameBoardScreen.getCategoryButtons()[i].setOnAction(e -> {
                if (isMyTurn) networkClient.sendCommand("SCORE:" + categoryName);
            });
        }

        // --- 3. END SCREEN LOGIC ---
        endScreen.getPlayAgainButton().setOnAction(e -> {
            startScreen.getConnectButton().setDisable(false);
            startScreen.getStatusLabel().setText("Enter server details to begin.");
            showStartScreen();
        });
    }

    /**
     * Parses messages from the background network thread and updates the UI.
     * Platform.runLater() is REQUIRED here because this is called by the NetworkClient thread.
     */
    private void handleServerMessage(String message) {
        Platform.runLater(() -> {
            System.out.println("[CLIENT RECEIVED]: " + message);

            if (message.contains("Opponent found")) {
                showGameBoard();
            } else if (message.startsWith("TURN:")) {
                isMyTurn = message.equals("TURN:YOUR_TURN");
                String status = isMyTurn ? "YOUR TURN!" : "Opponent's Turn...";
                String color = isMyTurn ? "#27ae60" : "#e74c3c";

                // Update the text
                gameBoardScreen.getTurnStatusLabel().setText(status);
                gameBoardScreen.getTurnStatusLabel().setStyle("-fx-text-fill: " + color + "; -fx-padding: 20px;");

                gameBoardScreen.getRollButton().setDisable(!isMyTurn);
            } else if (message.startsWith("DICE:")) {
                // Split the string at the "|" character
                String[] parts = message.substring(5).split("\\|");
                String diceStr = parts[0].trim();

                // NEW: Grab the second half of the string and update the label
                if (parts.length > 1) {
                    String rollsStr = parts[1].trim(); // e.g., "Rolls left: 2"
                    gameBoardScreen.getRollsLeftLabel().setText(rollsStr);
                }

                // Existing dice parsing logic
                String[] diceTokens = diceStr.split(" ");
                for (int i = 0; i < 5 && i < diceTokens.length; i++) {
                    String token = diceTokens[i];
                    boolean isKept = token.startsWith("[");
                    String value = token.replaceAll("[\\[\\]]", "");

                    if (value.equals("0")) {
                        value = "?";
                    }

                    gameBoardScreen.getDiceButtons()[i].setText(value);
                    gameBoardScreen.getDiceButtons()[i].setSelected(isKept);
                }
            } else if (message.startsWith("VALID_SCORE:")) {
                // Format: VALID_SCORE:CATEGORY:POINTS:TOTAL
                String[] parts = message.split(":");
                String catName = parts[1];
                String points = parts[2];
                myTotalScore = Integer.parseInt(parts[3]);

                // Find which row in the grid matches this category
                int index = Category.valueOf(catName).ordinal();

                // Update the score label and lock the category button
                gameBoardScreen.getScoreLabels()[index].setText(points);
                gameBoardScreen.getCategoryButtons()[index].setDisable(true);

                updateTotalScores();
            } else if (message.startsWith("OPPONENT_SCORE:")) {
                // Format: OPPONENT_SCORE:CATEGORY:POINTS:TOTAL
                String[] parts = message.split(":");
                opponentTotalScore = Integer.parseInt(parts[3]);
                updateTotalScores();
            } else if (message.startsWith("GAME_OVER:")) {
                // Format: GAME_OVER:RESULT_TEXT:MY_SCORE:OPPONENT_SCORE
                String[] parts = message.split(":");
                String resultText = parts[1];
                int myFinalScore = Integer.parseInt(parts[2]);
                int oppFinalScore = Integer.parseInt(parts[3]);

                // Disconnect the socket so the background thread closes cleanly
                if (networkClient != null) {
                    networkClient.disconnect();
                }

                // Transition to the final screen
                showEndScreen(resultText, myFinalScore, oppFinalScore);
            }
        });
    }

    public void showStartScreen() {
        mainScene.setRoot(startScreen);
    }

    private void showGameBoard() {
        // Swap out the root to show our new GameBoardScreen
        mainScene.setRoot(gameBoardScreen);
    }

    // A placeholder
//    private void showDummyGameBoard() {
//        StackPane placeholder = new StackPane();
//        placeholder.setStyle("-fx-background-color: #ffffff;");
//
//        Button triggerEndGameBtn = new Button("Simulate Game Over");
//        triggerEndGameBtn.setOnAction(e -> showEndScreen("YOU WON!", 250, 190));
//
//        placeholder.getChildren().add(triggerEndGameBtn);
//
//        // Swap to the game board
//        mainScene.setRoot(placeholder);
//    }

    public void showEndScreen(String winnerText, int p1Score, int p2Score) {
        endScreen.setResults(winnerText, p1Score, p2Score);

        // Swap to the end screen
        mainScene.setRoot(endScreen);
    }

    private void updateTotalScores() {
        gameBoardScreen.getTotalScoreLabel().setText(
                "MY SCORE: " + myTotalScore + "   |   OPPONENT: " + opponentTotalScore
        );
    }

    public static void main(String[] args) {
        launch(args);
    }
}
