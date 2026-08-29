import java.util.ArrayList;
import java.util.List;

public class Bishop extends Piece {
    public Bishop(boolean white, int row, int col) { super(white, row, col); }

    @Override
    public List<int[]> getPseudoLegalMoves(Board board) {
        List<int[]> moves = new ArrayList<>();
        slide(board, moves, 1, 1);
        slide(board, moves, 1, -1);
        slide(board, moves, -1, 1);
        slide(board, moves, -1, -1);
        return moves;
    }

    @Override public String getType() { return "Bishop"; }
    @Override public String getSymbol() { return white ? "\u2657" : "\u265D"; }
    @Override public Piece copy() {
        Bishop b = new Bishop(white, row, col);
        b.hasMoved = this.hasMoved;
        return b;
    }
}