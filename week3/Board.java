import java.util.ArrayList;
import java.util.List;

/**
 * Holds the 8x8 board state and all chess rule logic:
 * move generation, check/checkmate/stalemate detection,
 * castling, en passant, and pawn promotion.
 */
public class Board {

    public interface PromotionChooser {
        /** Return "Queen", "Rook", "Bishop", or "Knight". */
        String choosePromotion(boolean white);
    }

    public static class MoveInfo {
        public final Piece captured;
        public final boolean castle;
        public final boolean enPassant;
        public final boolean promotion;
        MoveInfo(Piece captured, boolean castle, boolean enPassant, boolean promotion) {
            this.captured = captured;
            this.castle = castle;
            this.enPassant = enPassant;
            this.promotion = promotion;
        }
    }

    private Piece[][] grid = new Piece[8][8];
    private boolean whiteToMove = true;
    private int[] enPassantTarget = null; // square a pawn can be captured on-passant this move

    public int[] lastMoveFrom = null;
    public int[] lastMoveTo = null;

    public List<Piece> capturedByWhite = new ArrayList<>(); // black pieces white has captured
    public List<Piece> capturedByBlack = new ArrayList<>(); // white pieces black has captured

    public Board() { setupBoard(); }

    public void setupBoard() {
        grid = new Piece[8][8];
        whiteToMove = true;
        enPassantTarget = null;
        lastMoveFrom = null;
        lastMoveTo = null;
        capturedByWhite.clear();
        capturedByBlack.clear();

        // Pawns
        for (int c = 0; c < 8; c++) {
            grid[6][c] = new Pawn(true, 6, c);
            grid[1][c] = new Pawn(false, 1, c);
        }
        // Back ranks
        grid[7][0] = new Rook(true, 7, 0);
        grid[7][7] = new Rook(true, 7, 7);
        grid[0][0] = new Rook(false, 0, 0);
        grid[0][7] = new Rook(false, 0, 7);

        grid[7][1] = new Knight(true, 7, 1);
        grid[7][6] = new Knight(true, 7, 6);
        grid[0][1] = new Knight(false, 0, 1);
        grid[0][6] = new Knight(false, 0, 6);

        grid[7][2] = new Bishop(true, 7, 2);
        grid[7][5] = new Bishop(true, 7, 5);
        grid[0][2] = new Bishop(false, 0, 2);
        grid[0][5] = new Bishop(false, 0, 5);

        grid[7][3] = new Queen(true, 7, 3);
        grid[0][3] = new Queen(false, 0, 3);

        grid[7][4] = new King(true, 7, 4);
        grid[0][4] = new King(false, 0, 4);
    }

    public Piece getPiece(int r, int c) {
        if (r < 0 || r > 7 || c < 0 || c > 7) return null;
        return grid[r][c];
    }

    public boolean isWhiteToMove() { return whiteToMove; }
    public int[] getEnPassantTarget() { return enPassantTarget; }

    /** All squares of the given color attacking (r,c). */
    public boolean isSquareAttacked(int r, int c, boolean byWhite) {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece p = grid[row][col];
                if (p == null || p.isWhite() != byWhite) continue;
                for (int[] sq : p.getAttackSquares(this)) {
                    if (sq[0] == r && sq[1] == c) return true;
                }
            }
        }
        return false;
    }

    public int[] findKing(boolean white) {
        for (int r = 0; r < 8; r++)
            for (int c = 0; c < 8; c++)
                if (grid[r][c] instanceof King && grid[r][c].isWhite() == white)
                    return new int[]{r, c};
        return null; // should never happen in a valid game
    }

    public boolean isKingInCheck(boolean white) {
        int[] kp = findKing(white);
        if (kp == null) return false;
        return isSquareAttacked(kp[0], kp[1], !white);
    }

    /** Pseudo-legal moves filtered to exclude any that leave the mover's own king in check. */
    public List<int[]> getLegalMoves(Piece piece) {
        List<int[]> legal = new ArrayList<>();
        if (piece == null) return legal;
        for (int[] move : piece.getPseudoLegalMoves(this)) {
            Board clone = this.copy();
            Piece clonedPiece = clone.getPiece(piece.getRow(), piece.getCol());
            clone.executeRawMove(clonedPiece, move[0], move[1], "Queen");
            if (!clone.isKingInCheck(piece.isWhite())) {
                legal.add(move);
            }
        }
        return legal;
    }

    public boolean hasAnyLegalMove(boolean white) {
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = grid[r][c];
                if (p != null && p.isWhite() == white && !getLegalMoves(p).isEmpty()) return true;
            }
        }
        return false;
    }

    public boolean isCheckmate(boolean white) {
        return isKingInCheck(white) && !hasAnyLegalMove(white);
    }

    public boolean isStalemate(boolean white) {
        return !isKingInCheck(white) && !hasAnyLegalMove(white);
    }

    /**
     * Executes a move on the raw grid: handles capture, castling rook movement,
     * en passant capture, and promotion. Does NOT check legality and does NOT
     * flip whiteToMove - used both for real moves and for check-simulation clones.
     */
    private MoveInfo executeRawMove(Piece piece, int toR, int toC, String promotionType) {
        int fromR = piece.getRow();
        int fromC = piece.getCol();
        Piece captured = grid[toR][toC];
        boolean isEnPassant = false;

        if (piece instanceof Pawn && captured == null && fromC != toC) {
            // diagonal move onto an empty square = en passant capture
            captured = grid[fromR][toC];
            grid[fromR][toC] = null;
            isEnPassant = true;
        }

        grid[fromR][fromC] = null;
        grid[toR][toC] = piece;
        piece.setPosition(toR, toC);
        piece.setMoved(true);

        boolean isCastle = false;
        if (piece instanceof King && Math.abs(toC - fromC) == 2) {
            isCastle = true;
            int rookFromC = (toC > fromC) ? 7 : 0;
            int rookToC = (toC > fromC) ? toC - 1 : toC + 1;
            Piece rook = grid[toR][rookFromC];
            grid[toR][rookFromC] = null;
            grid[toR][rookToC] = rook;
            if (rook != null) {
                rook.setPosition(toR, rookToC);
                rook.setMoved(true);
            }
        }

        boolean isPromotion = false;
        if (piece instanceof Pawn) {
            int lastRank = piece.isWhite() ? 0 : 7;
            if (toR == lastRank) {
                isPromotion = true;
                Piece promoted = createPiece(promotionType, piece.isWhite(), toR, toC);
                promoted.setMoved(true);
                grid[toR][toC] = promoted;
            }
        }

        // en passant target only exists for the move immediately after a double pawn step
        enPassantTarget = null;
        if (piece instanceof Pawn && Math.abs(toR - fromR) == 2) {
            enPassantTarget = new int[]{(fromR + toR) / 2, fromC};
        }

        return new MoveInfo(captured, isCastle, isEnPassant, isPromotion);
    }

    private Piece createPiece(String type, boolean white, int r, int c) {
        switch (type) {
            case "Rook": return new Rook(white, r, c);
            case "Bishop": return new Bishop(white, r, c);
            case "Knight": return new Knight(white, r, c);
            default: return new Queen(white, r, c);
        }
    }

    /**
     * Public entry point used by the GUI to actually make a move.
     * Validates that (toR,toC) is among the piece's legal moves, executes it,
     * records captures, updates whose turn it is, and returns move info
     * (or null if the move was illegal).
     */
    public MoveInfo makeMove(int fromR, int fromC, int toR, int toC, PromotionChooser chooser) {
        Piece piece = getPiece(fromR, fromC);
        if (piece == null || piece.isWhite() != whiteToMove) return null;

        boolean legal = false;
        for (int[] m : getLegalMoves(piece)) {
            if (m[0] == toR && m[1] == toC) { legal = true; break; }
        }
        if (!legal) return null;

        String promotionType = "Queen";
        if (piece instanceof Pawn) {
            int lastRank = piece.isWhite() ? 0 : 7;
            if (toR == lastRank && chooser != null) {
                promotionType = chooser.choosePromotion(piece.isWhite());
            }
        }

        MoveInfo info = executeRawMove(piece, toR, toC, promotionType);

        if (info.captured != null) {
            if (piece.isWhite()) capturedByWhite.add(info.captured);
            else capturedByBlack.add(info.captured);
        }

        lastMoveFrom = new int[]{fromR, fromC};
        lastMoveTo = new int[]{toR, toC};
        whiteToMove = !whiteToMove;
        return info;
    }

    /** Deep copy of the board, used to simulate moves without touching the real game state. */
    public Board copy() {
        Board b = new Board();
        b.grid = new Piece[8][8];
        for (int r = 0; r < 8; r++)
            for (int c = 0; c < 8; c++)
                b.grid[r][c] = (grid[r][c] == null) ? null : grid[r][c].copy();
        b.whiteToMove = this.whiteToMove;
        b.enPassantTarget = (this.enPassantTarget == null) ? null
                : new int[]{this.enPassantTarget[0], this.enPassantTarget[1]};
        return b;
    }
}