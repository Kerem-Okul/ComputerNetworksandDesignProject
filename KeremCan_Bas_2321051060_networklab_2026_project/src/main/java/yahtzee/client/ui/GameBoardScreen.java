package main.java.yahtzee.client.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class GameBoardScreen extends BorderPane {
    // Dice UI
    private final ToggleButton[] diceButtons = new ToggleButton[5];
    private Button rollButton;
    private Label rollsLeftLabel;
    private Label turnStatusLabel;

    // Scorecard UI
    private GridPane scorecardGrid;
    private Button[] categoryButtons = new Button[13];
    private Label[] scoreLabels = new Label[13];
    private Label totalScoreLabel;

    // The 13 Yahtzee Categories for the UI labels
    private final String[] categoryNames = {
            "Aces", "Twos", "Threes", "Fours", "Fives", "Sixes",
            "3 of a Kind", "4 of a Kind", "Full House",
            "Sm. Straight", "Lg. Straight", "Yahtzee", "Chance"
    };

    public GameBoardScreen() {
        setStyle("-fx-background-color: #ecf0f1;");

        // Top: Status Banner
        turnStatusLabel = new Label("Waiting for game to start...");
        turnStatusLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        turnStatusLabel.setStyle("-fx-text-fill: #2c3e50; -fx-padding: 20px;");
        BorderPane.setAlignment(turnStatusLabel, Pos.CENTER);
        setTop(turnStatusLabel);

        // Center: Dice Area
        setCenter(makeDiceArea());

        // Right: Scorecard Area
        scorecardGrid = makeScorecardArea();
        setRight(scorecardGrid);
    }

    private VBox makeDiceArea() {
        VBox diceArea = new VBox(30);
        diceArea.setAlignment(Pos.CENTER);
        diceArea.setPadding(new Insets(50));

        HBox diceBox = new HBox(15);
        diceBox.setAlignment(Pos.CENTER);

        // Initialize the 5 dice toggle buttons
        for (int i = 0; i < 5; i++) {
            ToggleButton die = new ToggleButton("?");
            die.setPrefSize(80, 80);
            die.setFont(Font.font("Arial", FontWeight.BOLD, 36));
            // Style it to look like a physical block. When selected, it turns green.
            die.setStyle("-fx-background-color: white; -fx-border-color: #bdc3c7; -fx-border-width: 2px; -fx-border-radius: 10px; -fx-background-radius: 10px; -fx-cursor: hand;");

            // Add a listener to change color when kept (selected)
            die.selectedProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal) {
                    die.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-border-color: #27ae60; -fx-border-width: 2px; -fx-border-radius: 10px; -fx-background-radius: 10px; -fx-cursor: hand;");
                } else {
                    die.setStyle("-fx-background-color: white; -fx-text-fill: black; -fx-border-color: #bdc3c7; -fx-border-width: 2px; -fx-border-radius: 10px; -fx-background-radius: 10px; -fx-cursor: hand;");
                }
            });

            diceButtons[i] = die;
            diceBox.getChildren().add(die);
        }

        rollButton = new Button("ROLL DICE");
        rollButton.setPrefSize(200, 50);
        rollButton.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        rollButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-background-radius: 25px; -fx-cursor: hand;");

        rollsLeftLabel = new Label("Rolls remaining: 3");
        rollsLeftLabel.setFont(Font.font("Arial", 16));
        rollsLeftLabel.setStyle("-fx-text-fill: #7f8c8d;");

        diceArea.getChildren().addAll(diceBox, rollButton, rollsLeftLabel);
        return diceArea;
    }

    private GridPane makeScorecardArea() {
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setVgap(10);
        grid.setHgap(15);
        grid.setPadding(new Insets(20, 50, 20, 20));
        grid.setStyle("-fx-background-color: white; -fx-border-color: #bdc3c7; -fx-border-width: 0 0 0 2px;");

        Label headerCategory = new Label("CATEGORY");
        headerCategory.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        Label headerScore = new Label("SCORE");
        headerScore.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        grid.add(headerCategory, 0, 0);
        grid.add(headerScore, 1, 0);

        // Build the 13 rows for scoring
        for (int i = 0; i < 13; i++) {
            Button catBtn = new Button(categoryNames[i]);
            catBtn.setPrefWidth(120);
            catBtn.setStyle("-fx-background-color: #ecf0f1; -fx-cursor: hand;");
            categoryButtons[i] = catBtn;

            Label scoreLbl = new Label("-");
            scoreLbl.setFont(Font.font("Arial", FontWeight.BOLD, 16));
            scoreLabels[i] = scoreLbl;

            grid.add(catBtn, 0, i + 1);
            grid.add(scoreLbl, 1, i + 1);
        }

        totalScoreLabel = new Label("TOTAL: 0");
        totalScoreLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        grid.add(totalScoreLabel, 0, 15, 2, 1); // Spans 2 columns

        return grid;
    }

    // Getters so the main app can attach event listeners
    public ToggleButton[] getDiceButtons() {
        return diceButtons;
    }

    public Button getRollButton() {
        return rollButton;
    }

    public Button[] getCategoryButtons() {
        return categoryButtons;
    }

    public Label getTurnStatusLabel() {
        return turnStatusLabel;
    }

    public Label[] getScoreLabels() {
        return scoreLabels;
    }

    public Label getTotalScoreLabel() {
        return totalScoreLabel;
    }

    public Label getRollsLeftLabel() {
        return rollsLeftLabel;
    }
}
