package main.java.yahtzee.client.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class StartScreen extends VBox {
    private final TextField ipInput;
    private final TextField usernameInput;
    private final Button connectButton;
    private final Label statusLabel;

    public StartScreen() {
        setSpacing(20);
        setAlignment(Pos.CENTER);
        setPadding(new Insets(50));
        setStyle("-fx-background-color: #f4f4f9;");

        Label titleLabel = new Label("YAHTZEE MULTIPLAYER");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        titleLabel.setStyle("-fx-text-fill: #2c3e50;");

        ipInput = new TextField("127.0.0.1"); // Default to localhost for testing
        ipInput.setPromptText("Server IP Address");
        ipInput.setMaxWidth(250);
        ipInput.setStyle("-fx-font-size: 16px; -fx-padding: 10px;");

        usernameInput = new TextField();
        usernameInput.setPromptText("Enter Username");
        usernameInput.setMaxWidth(250);
        usernameInput.setStyle("-fx-font-size: 16px; -fx-padding: 10px;");

        connectButton = new Button("CONNECT TO GAME");
        connectButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 10px 20px;");
        connectButton.setMaxWidth(250);

        statusLabel = new Label("Enter server details to begin.");
        statusLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 14px;");

        getChildren().addAll(titleLabel, ipInput, usernameInput, connectButton, statusLabel);
    }

    public TextField getIpInput() {
        return ipInput;
    }

    public TextField getUsernameInput() {
        return usernameInput;
    }

    public Button getConnectButton() {
        return connectButton;
    }

    public Label getStatusLabel() {
        return statusLabel;
    }
}
