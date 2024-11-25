package game;

import java.io.FileNotFoundException;
import java.io.IOException;

public interface Player {
    Move move(Position position, Cell cell) throws IOException;
}
