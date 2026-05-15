package main.java.yahtzee.engine;

import java.util.Map;

public class GameEngine {
    private static final int MAX_ROLLS = 3;
    private final Dice dice;
    private final Scorecard scorecard;
    private int rollsRemaining;

    public GameEngine() {
        this.dice = new Dice();
        this.scorecard = new Scorecard();
        this.rollsRemaining = 0;
    }

    public void startTurn() {
        dice.reset();
        rollsRemaining = MAX_ROLLS;
    }

    public boolean roll() {
        if (rollsRemaining > 0) {
            dice.roll();
            rollsRemaining--;
            return true;
        }
        return false;
    }

    public void toggleKeep(int index) {
        dice.toggleKeep(index);
    }

    public int scoreTurn(Category category) {
        int points = scorecard.markScore(category, dice.getValues());

        if (points != -1) {
            rollsRemaining = 0;
        }

        return points;
    }

    public boolean isGameOver() {
        return scorecard.getScores().size() == 13;
    }

    public int getRollsRemaining() {
        return rollsRemaining;
    }

    public int getTotalScore() {
        return scorecard.getTotalScore();
    }

    public Map<Category, Integer> getCurrentScores() {
        return scorecard.getScores();
    }

    public String getDiceDisplay() {
        return dice.toString();
    }
}
