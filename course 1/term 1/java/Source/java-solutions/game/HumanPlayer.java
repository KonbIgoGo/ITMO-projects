package game;

import scanner.Sc;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Scanner;

public class HumanPlayer implements Player {
    private final PrintStream out;
    private final Scanner in;

    public HumanPlayer(final PrintStream out, final Scanner in) {
        this.out = out;
        this.in = in;
    }

    public HumanPlayer() {
        this(System.out, new Scanner(System.in));
    }

    private boolean checkMoveValidity(String a, String b) {
        boolean flag = true;
        try {
            Integer.parseInt(a);
            Integer.parseInt(b);
        } catch (NumberFormatException e) {
            flag = false;
        }
        return flag;
    };

    @Override
    public Move move(final Position position, final Cell cell) throws IOException {
        out.println("Position");
        out.println(position);
        out.println(cell + "'s move");
        out.println("Enter row and column");
        while (true) {
            String a = in.next();
            String b = in.next();
            if (checkMoveValidity(a, b)) {
                Move move = new Move(Integer.parseInt(a), Integer.parseInt(b), cell);
                if (position.isValid(move)) {
                    return move;
                }
                System.out.println("Move is invalid");
            } else {
                out.println("Move is invalid");
                in.nextLine();
            }
        }
    }
}
