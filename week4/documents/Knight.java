import java.util.ArrayList;
import java.util.List;

public class Knight extends Piece {
    private static final int[][] OFFSETS = {
        {2, 1}, {2, -1}, {-2, 1}, {-2, -1},
        {1, 2}, {1, -2}, {-1, 2}, {-1, -2}
    };

    public Knight(boolean white, int row, int col) { super(white, row, col); }

    @Override
    public List<int[]> getPseudoLegalMoves(Board board) {
        List<int[]> moves = new ArrayList<>();
        for (int[] off : OFFSETS) {
            int r = row + off[0];
            int c = col + off[1];
            if (canLandOn(board, r, c)) moves.add(new int[]{r, c});
        }
        return moves;
    }

    @Override public String getType() { return "Knight"; }
    @Override public String getSymbol() { return white ? "\u2658" : "\u265E"; }
    @Override public Piece copy() {
        Knight k = new Knight(white, row, col);
        k.hasMoved = this.hasMoved;
        return k;
    }
}