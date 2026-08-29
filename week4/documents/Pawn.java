import java.util.ArrayList;
import java.util.List;

public class Pawn extends Piece {
    public Pawn(boolean white, int row, int col) { super(white, row, col); }

    private int forward() { return white ? -1 : 1; }
    private int startRow() { return white ? 6 : 1; }

    @Override
    public List<int[]> getAttackSquares(Board board) {
        // The two diagonal squares a pawn threatens, regardless of what's there.
        List<int[]> attacks = new ArrayList<>();
        int r = row + forward();
        for (int dc : new int[]{-1, 1}) {
            int c = col + dc;
            if (r >= 0 && r <= 7 && c >= 0 && c <= 7) attacks.add(new int[]{r, c});
        }
        return attacks;
    }

    @Override
    public List<int[]> getPseudoLegalMoves(Board board) {
        List<int[]> moves = new ArrayList<>();
        int f = forward();
        int oneR = row + f;

        // One square forward
        if (oneR >= 0 && oneR <= 7 && board.getPiece(oneR, col) == null) {
            moves.add(new int[]{oneR, col});
            // Two squares forward from starting rank
            int twoR = row + 2 * f;
            if (row == startRow() && board.getPiece(twoR, col) == null) {
                moves.add(new int[]{twoR, col});
            }
        }

        // Diagonal captures
        for (int dc : new int[]{-1, 1}) {
            int c = col + dc;
            if (c < 0 || c > 7 || oneR < 0 || oneR > 7) continue;
            Piece target = board.getPiece(oneR, c);
            if (target != null && target.isWhite() != this.white) {
                moves.add(new int[]{oneR, c});
            } else if (target == null && board.getEnPassantTarget() != null
                    && board.getEnPassantTarget()[0] == oneR && board.getEnPassantTarget()[1] == c) {
                // En passant capture
                moves.add(new int[]{oneR, c});
            }
        }

        return moves;
    }

    @Override public String getType() { return "Pawn"; }
    @Override public String getSymbol() { return white ? "\u2659" : "\u265F"; }
    @Override public Piece copy() {
        Pawn p = new Pawn(white, row, col);
        p.hasMoved = this.hasMoved;
        return p;
    }
}