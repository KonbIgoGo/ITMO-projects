package game;

import java.util.Arrays;
import java.util.Map;

public class TicTacToeBoard implements Board, Position {
    private static final Map<Cell, Character> SYMBOLS = Map.of(
            Cell.X, 'X',
            Cell.O, 'O',
            Cell.E, '.'
    );
    private final Cell[][] cells;
    private final int k;
    private final int rows;
    private final int cols;
    private int moveCount = 0;
    private Cell turn;

    public TicTacToeBoard(int m, int n, int k) {
        this.cells = new Cell[m][n];
        this.k = k;
        this.rows = m;
        this.cols = n;
        for (Cell[] row : cells) {
            Arrays.fill(row, Cell.E);
        }
        turn = Cell.X;
    }

    private int changeCounter(int counter, boolean condition) {
        if (condition) {
            counter++;
        } else if (counter < k) {
            counter = 0;
        }
        return counter;
    }

    @Override
    public Position getPosition() {
        return this;
    }

    @Override
    public Cell getCell() {
        return turn;
    }

    @Override
    public Result makeMove(final Move move) {
        if (!isValid(move)) {
            return Result.LOSE;
        }
        cells[move.getRow()][move.getColumn()] = move.getValue();
        moveCount++;
        if (moveCount >= k * 2 - 1) {
            int row = move.getRow();
            int col = move.getColumn();
            int inDiag1 = 0;
            int inDiag2 = 0;
            int inRow = 0;
            int inCol = 0;

            //diags
            for (int i = row, j = row - 1; i < rows || j >= 0; i++, j--) {
                boolean firstPos = false, secondPos = false;
                if (i < rows) {
                    if (col + (i - row) < cols) {
                        firstPos = cells[i][col + (i - row)] == turn;
                        inDiag1 = changeCounter(inDiag1, firstPos);
                    }
                    if (col - (i - row) >= 0) {
                        secondPos = cells[i][col - (i - row)] == turn;
                        inDiag2 = changeCounter(inDiag2, secondPos);
                    }
                }

                boolean firstNeg = false, secondNeg = false;
                if (j >= 0) {
                    if (col - 1 - (row - 1 - j) >= 0) {
                        firstNeg = cells[j][col - 1 - (row - 1 - j)] == turn;
                        inDiag1 = changeCounter(inDiag1, firstNeg);
                    }
                    if (col - 1 + (row - 1 - j) < cols && col - 1 + ((row - 1) - j) >= 0) {
                        secondNeg = cells[j][col - 1 + ((row - 1) - j)] == turn;
                        inDiag2 = changeCounter(inDiag2, secondNeg);
                    }
                }

                if (!(firstPos || firstNeg || secondPos || secondNeg)) {
                    break;
                }
            }


            for (int rowPos = row, rowNeg = row - 1,
                 colPos = col, colNeg = col - 1;
                 rowPos < rows || rowNeg >= 0 ||
                         colPos < cols || colNeg >= 0;
                 rowPos++, rowNeg--, colPos++, colNeg--) {

                if (rowPos < rows) {
                    inRow = changeCounter(inRow, cells[rowPos][col] == turn);
                }
                if (rowNeg >= 0) {
                    inRow = changeCounter(inRow, cells[rowNeg][col] == turn);
                }

                if (colPos < cols) {
                    inCol = changeCounter(inCol, cells[row][colPos] == turn);
                }
                if (colNeg >= 0) {
                    inCol = changeCounter(inCol, cells[row][colNeg] == turn);
                }

            }

            if (inDiag1 == k || inDiag2 == k || inRow == k || inCol == k) {
                return Result.WIN;
            }
            if (moveCount == rows * cols) {
                return Result.DRAW;
            }
        }

        turn = turn == Cell.X ? Cell.O : Cell.X;
        return Result.UNKNOWN;
    }

    @Override
    public boolean isValid(final Move move) {
        try {
            return 0 <= move.getRow() && move.getRow() < rows
                    && 0 <= move.getColumn() && move.getColumn() < cols
                    && cells[move.getRow()][move.getColumn()] == Cell.E
                    && turn == getCell();
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public Cell getCell(final int r, final int c) {
        return cells[r][c];
    }

    @Override
    public String toString() {
        int rowsLen = Integer.toString(rows-1).length();
        final StringBuilder sb = new StringBuilder(" ".repeat(rowsLen));
        for (int c = 0; c < cols; c++) {
            sb.append(" ");
            sb.append(c);
        }
        for (int r = 0; r < rows; r++) {
            sb.append("\n");
            sb.append(r);
            int rLen = Integer.toString(r).length();
            if (rLen < rowsLen) {
                sb.append(" ".repeat(rowsLen - rLen));
            }
            sb.append(" ");
            for (int c = 0; c < cols; c++) {
                int cLen = Integer.toString(c).length();
                sb.append(" ".repeat(cLen-1));
                sb.append(SYMBOLS.get(cells[r][c]));
                sb.append(" ");
            }
        }
        return sb.toString();
    }
}
