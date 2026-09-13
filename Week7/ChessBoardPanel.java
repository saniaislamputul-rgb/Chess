import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.List;

/**
 * Draws the 8x8 board and pieces, and turns mouse clicks into moves.
 * The board can be flipped via setFlipped() so the chosen color sits at the bottom.
 */
public class ChessBoardPanel extends JPanel {

    public interface GameListener {
        void onMoveCompleted(Board.MoveInfo info, Piece movedPiece);
        void onIllegalSelection();
    }

    private static final int SQUARE = 72;
    // Black & white board theme
    private static final Color LIGHT = new Color(250, 250, 250);
    private static final Color DARK = new Color(20, 20, 20);
    private static final Color SELECTED = new Color(212, 175, 100, 210);
    private static final Color LAST_MOVE = new Color(150, 150, 150, 140);
    private static final Color LEGAL_DOT = new Color(212, 175, 100, 190);
    private static final Color CAPTURE_RING = new Color(200, 40, 40, 190);
    private static final Color CHECK_GLOW = new Color(220, 30, 30, 180);

    private final Board board;
    private Piece selected = null;
    private List<int[]> legalMoves = List.of();
    private GameListener listener;
    private boolean gameOver = false;
    private boolean flipped = false; // true = Black is drawn at the bottom

    public ChessBoardPanel(Board board) {
        this.board = board;
        setPreferredSize(new Dimension(SQUARE * 8, SQUARE * 8));
        setBackground(Color.DARK_GRAY);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                handleClick(e.getX(), e.getY());
            }
        });
    }

    public void setListener(GameListener l) { this.listener = l; }
    public void setGameOver(boolean over) { this.gameOver = over; }
    public void clearSelection() { selected = null; legalMoves = List.of(); repaint(); }

    /** When true, Black's side is drawn at the bottom of the board (chosen on the Play screen). */
    public void setFlipped(boolean flipped) {
        this.flipped = flipped;
        repaint();
    }

    private int screenRow(int boardRow) { return flipped ? 7 - boardRow : boardRow; }
    private int screenCol(int boardCol) { return flipped ? 7 - boardCol : boardCol; }

    private void handleClick(int x, int y) {
        if (gameOver) return;
        int screenCol = x / SQUARE;
        int screenRow = y / SQUARE;
        if (screenRow < 0 || screenRow > 7 || screenCol < 0 || screenCol > 7) return;
        int row = flipped ? 7 - screenRow : screenRow;
        int col = flipped ? 7 - screenCol : screenCol;

        Piece clicked = board.getPiece(row, col);

        if (selected == null) {
            if (clicked != null && clicked.isWhite() == board.isWhiteToMove()) {
                selected = clicked;
                legalMoves = board.getLegalMoves(clicked);
                if (legalMoves.isEmpty() && listener != null) listener.onIllegalSelection();
            }
            repaint();
            return;
        }

        // A piece is already selected
        boolean isTargetLegal = false;
        for (int[] m : legalMoves) {
            if (m[0] == row && m[1] == col) { isTargetLegal = true; break; }
        }

        if (isTargetLegal) {
            Board.PromotionChooser chooser = (white) -> promptPromotion(white);
            Piece movedPiece = selected;
            Board.MoveInfo info = board.makeMove(selected.getRow(), selected.getCol(), row, col, chooser);
            selected = null;
            legalMoves = List.of();
            repaint();
            if (listener != null && info != null) listener.onMoveCompleted(info, movedPiece);
            return;
        }

        // Clicking another one of the current player's own pieces re-selects it
        if (clicked != null && clicked.isWhite() == board.isWhiteToMove()) {
            selected = clicked;
            legalMoves = board.getLegalMoves(clicked);
        } else {
            selected = null;
            legalMoves = List.of();
        }
        repaint();
    }

    private String promptPromotion(boolean white) {
        String colorName = white ? "White" : "Black";
        String[] result = {"Queen"};

        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(owner, Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setUndecorated(true);

        JPanel banner = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();
                RoundRectangle2D shape = new RoundRectangle2D.Float(4, 4, w - 8, h - 8, 26, 26);
                GradientPaint gp = new GradientPaint(0, 0, new Color(120, 32, 32), 0, h, new Color(70, 16, 16));
                g2.setPaint(gp);
                g2.fill(shape);
                g2.setColor(new Color(212, 175, 100));
                g2.setStroke(new BasicStroke(3f));
                g2.draw(shape);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        banner.setOpaque(false);
        banner.setLayout(new BoxLayout(banner, BoxLayout.Y_AXIS));
        banner.setBorder(BorderFactory.createEmptyBorder(30, 36, 30, 36));

        JLabel titleLabel = new JLabel("PAWN PROMOTION");
        titleLabel.setFont(new Font("Serif", Font.BOLD, 22));
        titleLabel.setForeground(new Color(245, 230, 200));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Promote " + colorName + " pawn to:");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 14));
        subtitle.setForeground(new Color(230, 220, 210));
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitle.setBorder(BorderFactory.createEmptyBorder(6, 0, 18, 0));

        banner.add(titleLabel);
        banner.add(subtitle);

        String[] types = {"Queen", "Rook", "Bishop", "Knight"};
        String[] symbols = white
                ? new String[]{"\u2655", "\u2656", "\u2657", "\u2658"}
                : new String[]{"\u265B", "\u265C", "\u265D", "\u265E"};

        for (int i = 0; i < types.length; i++) {
            String type = types[i];
            RoundedButton btn = new RoundedButton(symbols[i] + "   " + type, new Color(120, 84, 40));
            btn.setAlignmentX(Component.CENTER_ALIGNMENT);
            btn.setPreferredSize(new Dimension(220, 48));
            btn.setMaximumSize(new Dimension(220, 48));
            btn.addActionListener(e -> {
                result[0] = type;
                dialog.dispose();
            });
            banner.add(btn);
            if (i < types.length - 1) banner.add(Box.createVerticalStrut(10));
        }

        dialog.setContentPane(banner);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
        return result[0];
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int[] kingInCheckSq = null;
        if (board.isKingInCheck(board.isWhiteToMove())) {
            kingInCheckSq = board.findKing(board.isWhiteToMove());
        }

        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                int x = screenCol(c) * SQUARE, y = screenRow(r) * SQUARE;
                g2.setColor(((r + c) % 2 == 0) ? LIGHT : DARK);
                g2.fillRect(x, y, SQUARE, SQUARE);

                // Last move highlight
                if (board.lastMoveFrom != null && board.lastMoveFrom[0] == r && board.lastMoveFrom[1] == c
                        || board.lastMoveTo != null && board.lastMoveTo[0] == r && board.lastMoveTo[1] == c) {
                    g2.setColor(LAST_MOVE);
                    g2.fillRect(x, y, SQUARE, SQUARE);
                }

                // Selected square highlight
                if (selected != null && selected.getRow() == r && selected.getCol() == c) {
                    g2.setColor(SELECTED);
                    g2.fillRect(x, y, SQUARE, SQUARE);
                }

                // King in check glow
                if (kingInCheckSq != null && kingInCheckSq[0] == r && kingInCheckSq[1] == c) {
                    g2.setColor(CHECK_GLOW);
                    g2.fillOval(x + 4, y + 4, SQUARE - 8, SQUARE - 8);
                }

                // Coordinate labels along the edges
                g2.setFont(new Font("SansSerif", Font.BOLD, 11));
                g2.setColor(((r + c) % 2 == 0) ? DARK : LIGHT);
                if (c == 0) g2.drawString(String.valueOf(8 - r), x + 3, y + 13);
                if (r == 7) g2.drawString(String.valueOf((char) ('a' + c)), x + SQUARE - 12, y + SQUARE - 4);

                Piece p = board.getPiece(r, c);
                if (p != null) {
                    drawPiece(g2, p, x, y);
                }

                // Legal move indicator
                for (int[] m : legalMoves) {
                    if (m[0] == r && m[1] == c) {
                        if (board.getPiece(r, c) != null) {
                            g2.setColor(CAPTURE_RING);
                            g2.setStroke(new BasicStroke(4f));
                            g2.drawOval(x + 5, y + 5, SQUARE - 10, SQUARE - 10);
                        } else {
                            g2.setColor(LEGAL_DOT);
                            int d = SQUARE / 3;
                            g2.fillOval(x + (SQUARE - d) / 2, y + (SQUARE - d) / 2, d, d);
                        }
                    }
                }
            }
        }
    }

    private void drawPiece(Graphics2D g2, Piece p, int x, int y) {
        Font font = new Font("Serif", Font.BOLD, SQUARE - 14);
        g2.setFont(font);
        String symbol = p.getSymbol();
        FontMetrics fm = g2.getFontMetrics();
        int tx = x + (SQUARE - fm.stringWidth(symbol)) / 2;
        int ty = y + (SQUARE - fm.getHeight()) / 2 + fm.getAscent();

        // Outline for readability on any square color, fill for piece color
        Color fill = p.isWhite() ? Color.WHITE : new Color(25, 25, 25);
        Color outline = p.isWhite() ? new Color(40, 40, 40) : new Color(225, 225, 225);

        g2.setColor(outline);
        int[] dx = {-1, 1, 0, 0, -1, -1, 1, 1};
        int[] dy = {0, 0, -1, 1, -1, 1, -1, 1};
        for (int i = 0; i < dx.length; i++) {
            g2.drawString(symbol, tx + dx[i], ty + dy[i]);
        }
        g2.setColor(fill);
        g2.drawString(symbol, tx, ty);
    }
}