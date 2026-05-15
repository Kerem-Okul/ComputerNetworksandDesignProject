package main.java.yahtzee.client.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class EndScreen extends VBox {
    private final Label winnerLabel;
    private final Label scoreLabel;
    private final Button playAgainButton;

    public EndScreen() {
        setSpacing(25);
        setAlignment(Pos.CENTER);
        setPadding(new Insets(50));
        setStyle("-fx-background-color: #2c3e50;"); // Dark background for game over

        winnerLabel = new Label("GAME OVER");
        winnerLabel.setFont(Font.font("Arial", FontWeight.BOLD, 42));
        winnerLabel.setStyle("-fx-text-fill: #f1c40f;"); // Yellow text

        scoreLabel = new Label("P1: 0 | P2: 0");
        scoreLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 24));
        scoreLabel.setStyle("-fx-text-fill: #ecf0f1;");

        playAgainButton = new Button("PLAY AGAIN");
        playAgainButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold; -fx-padding: 12px 24px;");

        getChildren().addAll(winnerLabel, scoreLabel, playAgainButton);
    }

    public void setResults(String winnerText, int p1Score, int p2Score) {
        winnerLabel.setText(winnerText);
        scoreLabel.setText("Player 1: " + p1Score + "  |  Player 2: " + p2Score);
    }

    public Button getPlayAgainButton() {
        return playAgainButton;
    }
}
