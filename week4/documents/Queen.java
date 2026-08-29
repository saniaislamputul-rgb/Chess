import java.util.ArrayList;
import java.util.List;

public class Queen extends Piece {
    public Queen(boolean white, int row, int col) { super(white, row, col); }

    @Override
    public List<int[]> getPseudoLegalMoves(Board board) {
        List<int[]> moves = new ArrayList<>();
        // Rook-like
        slide(board, moves, 1, 0);
        slide(board, moves, -1, 0);
        slide(board, moves, 0, 1);
        slide(board, moves, 0, -1);
        // Bishop-like
        slide(board, moves, 1, 1);
        slide(board, moves, 1, -1);
        slide(board, moves, -1, 1);
        slide(board, moves, -1, -1);
        return moves;
    }

    @Override public String getType() { return "Queen"; }
    @Override public String getSymbol() { return white ? "\u2655" : "\u265B"; }
    @Override public Piece copy() {
        Queen q = new Queen(white, row, col);
        q.hasMoved = this.hasMoved;
        return q;
    }
}