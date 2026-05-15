package main.java.yahtzee.engine;

import java.util.HashMap;
import java.util.Map;

public class Scorecard {
    private final Map<Category, Integer> scores;

    public Scorecard() {
        this.scores = new HashMap<>();
    }

    public int markScore(Category category, int[] currentDice) {
        if (scores.containsKey(category)) {
            return -1;
        }
        int score = calculateScore(category, currentDice);
        scores.put(category, score);
        return score;
    }

    public int getTotalScore() {
        int total = 0;
        int upperSum = 0;

        for (Map.Entry<Category, Integer> entry : scores.entrySet()) {
            total += entry.getValue();
            if (isUpperSection(entry.getKey())) {
                upperSum += entry.getValue();
            }
        }
        if (upperSum >= 63) {
            total += 35;
        }

        return total;
    }

    private int calculateScore(Category cat, int[] dice) {
        return switch (cat) {
            case ACES -> scoreUpper(dice, 1);
            case TWOS -> scoreUpper(dice, 2);
            case THREES -> scoreUpper(dice, 3);
            case FOURS -> scoreUpper(dice, 4);
            case FIVES -> scoreUpper(dice, 5);
            case SIXES -> scoreUpper(dice, 6);
            case THREE_OF_A_KIND -> isThreeOfAKind(dice) ? sumAll(dice) : 0;
            case FOUR_OF_A_KIND -> isFourOfAKind(dice) ? sumAll(dice) : 0;
            case FULL_HOUSE -> isFullHouse(dice) ? 25 : 0;
            case SMALL_STRAIGHT -> isSmallStraight(dice) ? 30 : 0;
            case LARGE_STRAIGHT -> isLargeStraight(dice) ? 40 : 0;
            case YAHTZEE -> isYahtzee(dice) ? 50 : 0;
            case CHANCE -> sumAll(dice);
        };
    }

    private int[] getFrequencies(int[] dice) {
        int[] counts = new int[7];
        for (int d : dice) {
            counts[d]++;
        }
        return counts;
    }

    private int sumAll(int[] dice) {
        int sum = 0;
        for (int d : dice) sum += d;
        return sum;
    }

    private boolean isUpperSection(Category cat) {
        return cat == Category.ACES || cat == Category.TWOS || cat == Category.THREES ||
                cat == Category.FOURS || cat == Category.FIVES || cat == Category.SIXES;
    }

    private int scoreUpper(int[] dice, int targetValue) {
        int sum = 0;
        for (int d : dice) {
            if (d == targetValue) sum += d;
        }
        return sum;
    }

    private boolean isThreeOfAKind(int[] dice) {
        for (int count : getFrequencies(dice)) {
            if (count >= 3) return true;
        }
        return false;
    }

    private boolean isFourOfAKind(int[] dice) {
        for (int count : getFrequencies(dice)) {
            if (count >= 4) return true;
        }
        return false;
    }

    private boolean isFullHouse(int[] dice) {
        boolean hasThree = false;
        boolean hasTwo = false;
        for (int count : getFrequencies(dice)) {
            if (count == 3) hasThree = true;
            if (count == 2) hasTwo = true;
        }
        return hasThree && hasTwo;
    }

    private boolean isSmallStraight(int[] dice) {
        int[] counts = getFrequencies(dice);
        return (counts[1] > 0 && counts[2] > 0 && counts[3] > 0 && counts[4] > 0) ||
                (counts[2] > 0 && counts[3] > 0 && counts[4] > 0 && counts[5] > 0) ||
                (counts[3] > 0 && counts[4] > 0 && counts[5] > 0 && counts[6] > 0);
    }

    private boolean isLargeStraight(int[] dice) {
        int[] counts = getFrequencies(dice);
        return (counts[1] > 0 && counts[2] > 0 && counts[3] > 0 && counts[4] > 0 && counts[5] > 0) ||
                (counts[2] > 0 && counts[3] > 0 && counts[4] > 0 && counts[5] > 0 && counts[6] > 0);
    }

    private boolean isYahtzee(int[] dice) {
        for (int count : getFrequencies(dice)) {
            if (count == 5) return true;
        }
        return false;
    }


    public Map<Category, Integer> getScores() {
        return new HashMap<>(scores);
    }
}