import java.util.ArrayList;
import java.util.List;

public class Rook extends Piece {
    public Rook(boolean white, int row, int col) { super(white, row, col); }

    @Override
    public List<int[]> getPseudoLegalMoves(Board board) {
        List<int[]> moves = new ArrayList<>();
        slide(board, moves, 1, 0);
        slide(board, moves, -1, 0);
        slide(board, moves, 0, 1);
        slide(board, moves, 0, -1);
        return moves;
    }

    @Override public String getType() { return "Rook"; }
    @Override public String getSymbol() { return white ? "\u2656" : "\u265C"; }
    @Override public Piece copy() {
        Rook r = new Rook(white, row, col);
        r.hasMoved = this.hasMoved;
        return r;
    }
}