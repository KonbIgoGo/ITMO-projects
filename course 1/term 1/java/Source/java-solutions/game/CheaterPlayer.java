package game;

public class CheaterPlayer implements Player{
    @Override
    public Move move(Position position, Cell cell) {
        throw new IllegalStateException();
    }
}
