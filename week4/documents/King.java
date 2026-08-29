import java.util.ArrayList;
import java.util.List;

public class King extends Piece {
    private static final int[][] OFFSETS = {
        {1, 0}, {-1, 0}, {0, 1}, {0, -1},
        {1, 1}, {1, -1}, {-1, 1}, {-1, -1}
    };

    public King(boolean white, int row, int col) { super(white, row, col); }

    /** Plain one-square moves, used both for real moves and as the basis of attack squares. */
    private List<int[]> basicMoves(Board board) {
        List<int[]> moves = new ArrayList<>();
        for (int[] off : OFFSETS) {
            int r = row + off[0];
            int c = col + off[1];
            if (canLandOn(board, r, c)) moves.add(new int[]{r, c});
        }
        return moves;
    }

    @Override
    public List<int[]> getAttackSquares(Board board) {
        // King attacks its 8 neighboring squares only - castling is never a "threat".
        return basicMoves(board);
    }

    @Override
    public List<int[]> getPseudoLegalMoves(Board board) {
        List<int[]> moves = basicMoves(board);
        addCastlingMoves(board, moves);
        return moves;
    }

    private void addCastlingMoves(Board board, List<int[]> moves) {
        if (hasMoved) return;
        // Can't castle out of check
        if (board.isSquareAttacked(row, col, !white)) return;

        int backRank = white ? 7 : 0;
        if (row != backRank) return;

        // King-side castling (rook on column 7)
        Piece kingSideRook = board.getPiece(backRank, 7);
        if (kingSideRook instanceof Rook && kingSideRook.isWhite() == white && !kingSideRook.hasMoved()) {
            if (board.getPiece(backRank, 5) == null && board.getPiece(backRank, 6) == null
                    && !board.isSquareAttacked(backRank, 5, !white)
                    && !board.isSquareAttacked(backRank, 6, !white)) {
                moves.add(new int[]{backRank, 6});
            }
        }

        // Queen-side castling (rook on column 0)
        Piece queenSideRook = board.getPiece(backRank, 0);
        if (queenSideRook instanceof Rook && queenSideRook.isWhite() == white && !queenSideRook.hasMoved()) {
            if (board.getPiece(backRank, 1) == null && board.getPiece(backRank, 2) == null
                    && board.getPiece(backRank, 3) == null
                    && !board.isSquareAttacked(backRank, 2, !white)
                    && !board.isSquareAttacked(backRank, 3, !white)) {
                moves.add(new int[]{backRank, 2});
            }
        }
    }

    @Override public String getType() { return "King"; }
    @Override public String getSymbol() { return white ? "\u2654" : "\u265A"; }
    @Override public Piece copy() {
        King k = new King(white, row, col);
        k.hasMoved = this.hasMoved;
        return k;
    }
}