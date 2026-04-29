package main.yahtzee.engine;

import java.util.Arrays;
import java.util.Random;

public class Dice {
    private final int[] values;
    private final boolean[] kept;
    private final Random random;

    public Dice() {
        this.values = new int[5];
        this.kept = new boolean[5];
        this.random = new Random();

        reset();
    }

    public void roll() {
        for (int i = 0; i < 5; i++) {
            if (!kept[i]) {
                values[i] = random.nextInt(6) + 1;
            }
        }
    }

    public void toggleKeep(int index) {
        if (index >= 0 && index < 5) {
            kept[index] = !kept[index];
        }
    }

    public void reset() {
        Arrays.fill(kept, false);
    }

    public int[] getValues() {
        return Arrays.copyOf(values, values.length);
    }

    public boolean[] getKept() {
        return Arrays.copyOf(kept, kept.length);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            if (kept[i]) {
                sb.append("[").append(values[i]).append("] ");
            } else {
                sb.append(values[i]).append(" ");
            }
        }
        return sb.toString().trim();
    }
}