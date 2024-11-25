package game;

import java.io.IOException;

public class PassPlayer implements Player{
    @Override
    public Move move(Position position, Cell cell) throws IOException {
        return new Move(-1, -1, cell);
    }
}
