package game;
import java.util.Random;
public class RandomPlayer implements Player {
    final int randomMax;
    private final Random random;

    public RandomPlayer(final Random random, final int max) {
        this.random = random;
        this.randomMax = max;
    }

    public RandomPlayer(final int max) {
        this(new Random(), max);
    }

    @Override
    public Move move(final Position position, final Cell cell) {
        while (true) {
            int r = random.nextInt(randomMax);
            int c = random.nextInt(randomMax);
            final Move move = new Move(r, c, cell);
            if (position.isValid(move)) {
                return move;
            }
        }
    }
}
