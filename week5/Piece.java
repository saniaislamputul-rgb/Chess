import java.util.List;

/**
 * Abstract base class for all chess pieces.
 * Each concrete piece (King, Queen, Rook, Bishop, Knight, Pawn) implements
 * its own movement rules in getPseudoLegalMoves().
 */
public abstract class Piece {

    protected boolean white;   // true = white piece, false = black piece
    protected int row, col;    // current board coordinates (0-7)
    protected boolean hasMoved; // used for castling / pawn double-step rules

    public Piece(boolean white, int row, int col) {
        this.white = white;
        this.row = row;
        this.col = col;
        this.hasMoved = false;
    }

    public boolean isWhite() { return white; }
    public int getRow() { return row; }
    public int getCol() { return col; }

    public void setPosition(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public boolean hasMoved() { return hasMoved; }
    public void setMoved(boolean moved) { this.hasMoved = moved; }

    /** All squares this piece could move to, ignoring whether the move would leave its own king in check. */
    public abstract List<int[]> getPseudoLegalMoves(Board board);

    /**
     * Squares this piece "attacks" (threatens capture on) - used for check detection.
     * For most pieces this equals getPseudoLegalMoves(); pawns and kings override it
     * because their normal moves include non-capturing moves (forward push, castling).
     */
    public List<int[]> getAttackSquares(Board board) {
        return getPseudoLegalMoves(board);
    }

    public abstract String getType();     // "King", "Queen", "Rook", "Bishop", "Knight", "Pawn"
    public abstract String getSymbol();   // Unicode chess glyph
    public abstract Piece copy();         // deep copy, used for board simulation

    /** Helper: is the given square on the board and not occupied by a friendly piece? */
    protected boolean canLandOn(Board board, int r, int c) {
        if (r < 0 || r > 7 || c < 0 || c > 7) return false;
        Piece p = board.getPiece(r, c);
        return p == null || p.isWhite() != this.white;
    }

    /** Helper used by sliding pieces (Rook/Bishop/Queen) to walk a direction until blocked. */
    protected void slide(Board board, List<int[]> moves, int dr, int dc) {
        int r = row + dr, c = col + dc;
        while (r >= 0 && r <= 7 && c >= 0 && c <= 7) {
            Piece p = board.getPiece(r, c);
            if (p == null) {
                moves.add(new int[]{r, c});
            } else {
                if (p.isWhite() != this.white) moves.add(new int[]{r, c});
                break;
            }
            r += dr;
            c += dc;
        }
    }
}