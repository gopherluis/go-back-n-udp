package receiver;

import java.util.Random;

public class LossSimulator {

    private final double lossProbability;
    private final Random random;

    public LossSimulator(double lossProbability) {
        this.lossProbability = lossProbability;
        this.random = new Random();
    }

    public boolean shouldDrop() {
        return random.nextDouble() < lossProbability;
    }
}