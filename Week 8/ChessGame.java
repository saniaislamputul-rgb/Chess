import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Entry point and screen flow:
 *   START screen -> choose White or Black, press PLAY -> GAME screen
 *   GAME screen  -> Pause button -> PAUSE screen (Resume / Restart / Exit)
 *   PAUSE screen -> Exit -> back to START screen
 */
public class ChessGame extends JFrame implements ChessBoardPanel.GameListener {

    private static final Color BG = new Color(28, 16, 14);
    private static final Color MAROON = new Color(94, 24, 24);
    private static final Color GOLD = new Color(212, 175, 100);
    private static final Color PINK = new Color(255, 105, 180);

    private final Board board = new Board();
    private final ChessBoardPanel boardPanel = new ChessBoardPanel(board);
    private final JLabel statusLabel = new JLabel("White's Turn", SwingConstants.CENTER);

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cards = new JPanel(cardLayout);

    private static final String CARD_START = "start";
    private static final String CARD_MODE = "mode";
    private static final String CARD_SIDE = "side";
    private static final String CARD_GAME = "game";
    private static final String CARD_PAUSE = "pause";

    private boolean playerIsWhite = true;
    private boolean singlePlayer = false;
    private final Random random = new Random();
    private RoundedButton whiteChoiceBtn, blackChoiceBtn;

    public ChessGame() {
        super("Chess");
        boardPanel.setListener(this);

        cards.add(buildStartScreen(), CARD_START);
        cards.add(buildModeScreen(), CARD_MODE);
        cards.add(buildSideScreen(), CARD_SIDE);
        cards.add(buildGameScreen(), CARD_GAME);
        cards.add(buildPauseScreen(), CARD_PAUSE);

        getContentPane().setBackground(BG);
        setLayout(new BorderLayout());
        add(cards, BorderLayout.CENTER);

        cardLayout.show(cards, CARD_START);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setResizable(false);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    // ---------------------------------------------------------- START SCREEN

    private JPanel buildStartScreen() {
        JPanel panel = new CheckeredBackdrop();
        panel.setLayout(new GridBagLayout());
        panel.setPreferredSize(new Dimension(576, 660));
        JPanel content = new JPanel(); content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("♔  Chess  ♚"); title.setFont(new Font("Serif", Font.BOLD, 40)); title.setForeground(GOLD); title.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel sub = new JLabel("Welcome to Chess"); sub.setForeground(new Color(230,220,210)); sub.setAlignmentX(Component.CENTER_ALIGNMENT);
        RoundedButton start = new RoundedButton("START GAME", MAROON); start.setAlignmentX(Component.CENTER_ALIGNMENT); start.setPreferredSize(new Dimension(220,58)); start.setMaximumSize(new Dimension(220,58));
        start.addActionListener(e -> cardLayout.show(cards, CARD_MODE));
        content.add(title); content.add(Box.createVerticalStrut(12)); content.add(sub); content.add(Box.createVerticalStrut(35)); content.add(start); panel.add(content); return panel;
    }

    private JPanel buildModeScreen() {
        JPanel panel = new CheckeredBackdrop(); panel.setLayout(new GridBagLayout());
        JPanel content = new JPanel(); content.setOpaque(false); content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("Choose Game Mode"); title.setFont(new Font("SansSerif", Font.BOLD, 28)); title.setForeground(GOLD); title.setAlignmentX(Component.CENTER_ALIGNMENT);
        RoundedButton one = new RoundedButton("ONE PLAYER", MAROON); RoundedButton two = new RoundedButton("TWO PLAYERS", MAROON);
        one.setAlignmentX(Component.CENTER_ALIGNMENT); two.setAlignmentX(Component.CENTER_ALIGNMENT); one.setMaximumSize(new Dimension(240,55)); two.setMaximumSize(new Dimension(240,55));
        one.addActionListener(e -> { singlePlayer=true; cardLayout.show(cards, CARD_SIDE); });
        two.addActionListener(e -> { singlePlayer=false; cardLayout.show(cards, CARD_SIDE); });
        content.add(title); content.add(Box.createVerticalStrut(30)); content.add(one); content.add(Box.createVerticalStrut(15)); content.add(two); panel.add(content); return panel;
    }

    private JPanel buildSideScreen() {
        JPanel panel = new CheckeredBackdrop(); panel.setLayout(new GridBagLayout());
        JPanel content = new JPanel(); content.setOpaque(false); content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("Choose Your Side"); title.setFont(new Font("SansSerif", Font.BOLD, 28)); title.setForeground(GOLD); title.setAlignmentX(Component.CENTER_ALIGNMENT);
        whiteChoiceBtn = new RoundedButton("♔  WHITE", MAROON); blackChoiceBtn = new RoundedButton("♚  BLACK", MAROON);
        whiteChoiceBtn.setAlignmentX(Component.CENTER_ALIGNMENT); blackChoiceBtn.setAlignmentX(Component.CENTER_ALIGNMENT); whiteChoiceBtn.setMaximumSize(new Dimension(240,55)); blackChoiceBtn.setMaximumSize(new Dimension(240,55));
        whiteChoiceBtn.addActionListener(e -> selectSide(true)); blackChoiceBtn.addActionListener(e -> selectSide(false));
        RoundedButton play = new RoundedButton("PLAY", MAROON); play.setAlignmentX(Component.CENTER_ALIGNMENT); play.setMaximumSize(new Dimension(240,58)); play.addActionListener(e -> startNewGame());
        content.add(title); content.add(Box.createVerticalStrut(25)); content.add(whiteChoiceBtn); content.add(Box.createVerticalStrut(12)); content.add(blackChoiceBtn); content.add(Box.createVerticalStrut(25)); content.add(play); panel.add(content); selectSide(true); return panel;
    }

    private void selectSide(boolean white) {
        playerIsWhite = white;
        whiteChoiceBtn.setSelectedStyle(white);
        blackChoiceBtn.setSelectedStyle(!white);
    }

    private void startNewGame() {
        board.setupBoard();
        boardPanel.setFlipped(!playerIsWhite);
        boardPanel.setGameOver(false);
        boardPanel.clearSelection();
        updateStatus();
        cardLayout.show(cards, CARD_GAME);
        if (singlePlayer && !playerIsWhite) SwingUtilities.invokeLater(this::computerMove);
    }

    // ----------------------------------------------------------- GAME SCREEN

    private JPanel buildGameScreen() {
        JPanel screen = new JPanel(new BorderLayout());
        screen.setBackground(BG);

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(MAROON);
        topBar.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));

        JLabel title = new JLabel("\u265A  Chess  \u2654", SwingConstants.CENTER);
        title.setFont(new Font("Serif", Font.BOLD, 22));
        title.setForeground(GOLD);

        JPanel westSpacer = new JPanel();
        westSpacer.setOpaque(false);
        westSpacer.setPreferredSize(new Dimension(120, 40));

        RoundedButton pauseBtn = new RoundedButton("||  Pause", MAROON.brighter());
        pauseBtn.setPreferredSize(new Dimension(120, 40));
        pauseBtn.setFont(new Font("SansSerif", Font.BOLD, 15));
        pauseBtn.addActionListener(e -> cardLayout.show(cards, CARD_PAUSE));

        topBar.add(westSpacer, BorderLayout.WEST);
        topBar.add(title, BorderLayout.CENTER);
        topBar.add(pauseBtn, BorderLayout.EAST);

        statusLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        statusLabel.setForeground(PINK);
        statusLabel.setOpaque(true);
        statusLabel.setBackground(new Color(60, 63, 65));
        statusLabel.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));

        JPanel north = new JPanel(new BorderLayout());
        north.add(topBar, BorderLayout.NORTH);
        north.add(statusLabel, BorderLayout.SOUTH);

        JPanel center = new JPanel(new GridBagLayout());
        center.setBackground(BG);
        center.add(boardPanel);

        screen.add(north, BorderLayout.NORTH);
        screen.add(center, BorderLayout.CENTER);
        return screen;
    }

    private void restartGame() {
        board.setupBoard();
        boardPanel.setGameOver(false);
        boardPanel.clearSelection();
        updateStatus();
        boardPanel.repaint();
    }

    private void updateStatus() {
        boolean whiteTurn = board.isWhiteToMove();
        boolean inCheck = board.isKingInCheck(whiteTurn);
        String turnName = whiteTurn ? "White's Turn" : "Black's Turn";

        statusLabel.setForeground(PINK);
        if (inCheck) {
            statusLabel.setText(turnName + " \u2014 CHECK!");
            statusLabel.setBackground(new Color(140, 40, 40));
        } else {
            statusLabel.setText(turnName);
            statusLabel.setBackground(new Color(60, 63, 65));
        }
    }

    @Override
    public void onMoveCompleted(Board.MoveInfo info, Piece movedPiece) {
        boolean nowWhiteTurn = board.isWhiteToMove();

        if (board.isCheckmate(nowWhiteTurn)) {
            String winner = nowWhiteTurn ? "Black" : "White";
            statusLabel.setForeground(PINK);
            statusLabel.setText("CHECKMATE \u2014 " + winner + " Wins!");
            statusLabel.setBackground(new Color(120, 30, 30));
            boardPanel.setGameOver(true);
            boardPanel.repaint();
            announceGameOver("Checkmate!", winner + " wins the game.");
            return;
        }

        if (board.isStalemate(nowWhiteTurn)) {
            statusLabel.setForeground(PINK);
            statusLabel.setText("STALEMATE \u2014 Draw");
            statusLabel.setBackground(new Color(90, 90, 40));
            boardPanel.setGameOver(true);
            boardPanel.repaint();
            announceGameOver("Stalemate", "No legal moves remain. The game is a draw.");
            return;
        }

        updateStatus();
        if (singlePlayer && board.isWhiteToMove() != playerIsWhite) {
            SwingUtilities.invokeLater(this::computerMove);
        }
    }

    private void computerMove() {
        if (!singlePlayer || board.isWhiteToMove() == playerIsWhite || boardPanel == null) return;
        boolean computerWhite = !playerIsWhite;
        List<Piece> pieces = board.getPieces(computerWhite);
        List<Object[]> moves = new ArrayList<>();
        for (Piece p : pieces) {
            for (int[] m : board.getLegalMoves(p))
                moves.add(new Object[]{p, m});
        }
        if (moves.isEmpty()) return;
        Object[] chosen = moves.get(random.nextInt(moves.size()));
        Piece p = (Piece) chosen[0];
        int[] m = (int[]) chosen[1];
        board.makeMove(p.getRow(), p.getCol(), m[0], m[1], white -> "Queen");
        boardPanel.repaint();
        onMoveCompleted(null, p);
    }

    @Override
    public void onIllegalSelection() {
        // Selected a piece with no legal moves (e.g. it's pinned) - nothing to do,
        // the board simply shows no highlighted destinations.
    }

    private void announceGameOver(String title, String message) {
        JDialog dialog = new JDialog(this, true);
        dialog.setUndecorated(true);

        GradientBanner banner = new GradientBanner();
        banner.setOpaque(false);
        banner.setLayout(new BoxLayout(banner, BoxLayout.Y_AXIS));
        banner.setBorder(BorderFactory.createEmptyBorder(34, 40, 34, 40));
        banner.setPreferredSize(new Dimension(380, 320));

        JLabel titleLabel = new JLabel(title.toUpperCase());
        titleLabel.setFont(new Font("Serif", Font.BOLD, 26));
        titleLabel.setForeground(new Color(245, 230, 200));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel rule = new JLabel("\u2022\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2022");
        rule.setForeground(GOLD);
        rule.setFont(new Font("Serif", Font.PLAIN, 16));
        rule.setAlignmentX(Component.CENTER_ALIGNMENT);
        rule.setBorder(BorderFactory.createEmptyBorder(4, 0, 12, 0));

        JLabel messageLabel = new JLabel(message);
        messageLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        messageLabel.setForeground(new Color(230, 220, 210));
        messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        messageLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 24, 0));

        RoundedButton newGameBtn = new RoundedButton("NEW GAME", new Color(120, 84, 40));
        RoundedButton closeBtn = new RoundedButton("CLOSE", new Color(120, 84, 40));
        for (RoundedButton b : new RoundedButton[]{newGameBtn, closeBtn}) {
            b.setAlignmentX(Component.CENTER_ALIGNMENT);
            b.setPreferredSize(new Dimension(220, 50));
            b.setMaximumSize(new Dimension(220, 50));
        }
        newGameBtn.addActionListener(e -> {
            restartGame();
            dialog.dispose();
        });
        closeBtn.addActionListener(e -> {
            dialog.dispose();
            cardLayout.show(cards, CARD_START);
        });

        banner.add(titleLabel);
        banner.add(rule);
        banner.add(messageLabel);
        banner.add(newGameBtn);
        banner.add(Box.createVerticalStrut(14));
        banner.add(closeBtn);

        dialog.setContentPane(banner);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    // ---------------------------------------------------------- PAUSE SCREEN

    private JPanel buildPauseScreen() {
        JPanel panel = new CheckeredBackdrop();
        panel.setLayout(new GridBagLayout());
        panel.setPreferredSize(new Dimension(576, 660));

        JPanel banner = new GradientBanner();
        banner.setOpaque(false);
        banner.setLayout(new BoxLayout(banner, BoxLayout.Y_AXIS));
        banner.setBorder(BorderFactory.createEmptyBorder(34, 40, 34, 40));
        banner.setPreferredSize(new Dimension(320, 360));
        banner.setMaximumSize(new Dimension(320, 360));

        JLabel pausedLabel = new JLabel("PAUSED");
        pausedLabel.setFont(new Font("Serif", Font.BOLD, 30));
        pausedLabel.setForeground(new Color(245, 230, 200));
        pausedLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel rule = new JLabel("\u2022\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2022");
        rule.setForeground(GOLD);
        rule.setFont(new Font("Serif", Font.PLAIN, 16));
        rule.setAlignmentX(Component.CENTER_ALIGNMENT);
        rule.setBorder(BorderFactory.createEmptyBorder(4, 0, 24, 0));

        RoundedButton resumeBtn = new RoundedButton("RESUME", new Color(120, 84, 40));
        RoundedButton restartBtn = new RoundedButton("RESTART", new Color(120, 84, 40));
        RoundedButton exitBtn = new RoundedButton("EXIT", new Color(120, 84, 40));
        for (RoundedButton b : new RoundedButton[]{resumeBtn, restartBtn, exitBtn}) {
            b.setAlignmentX(Component.CENTER_ALIGNMENT);
            b.setPreferredSize(new Dimension(220, 50));
            b.setMaximumSize(new Dimension(220, 50));
        }

        resumeBtn.addActionListener(e -> cardLayout.show(cards, CARD_GAME));
        restartBtn.addActionListener(e -> {
            restartGame();
            cardLayout.show(cards, CARD_GAME);
        });
        exitBtn.addActionListener(e -> cardLayout.show(cards, CARD_START));

        banner.add(pausedLabel);
        banner.add(rule);
        banner.add(resumeBtn);
        banner.add(Box.createVerticalStrut(14));
        banner.add(restartBtn);
        banner.add(Box.createVerticalStrut(14));
        banner.add(exitBtn);

        panel.add(banner);
        return panel;
    }

    /** A rounded maroon/gold gradient banner used for the Pause screen and game-over popups. */
    private static class GradientBanner extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            RoundRectangle2D shape = new RoundRectangle2D.Float(4, 4, w - 8, h - 8, 26, 26);
            GradientPaint gp = new GradientPaint(0, 0, new Color(120, 32, 32), 0, h, new Color(70, 16, 16));
            g2.setPaint(gp);
            g2.fill(shape);
            g2.setColor(GOLD);
            g2.setStroke(new BasicStroke(3f));
            g2.draw(shape);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /** A dim checkerboard-pattern background used behind the Start and Pause screens. */
    private static class CheckeredBackdrop extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            int size = 36;
            int w = getWidth(), h = getHeight();
            for (int y = 0; y * size < h; y++) {
                for (int x = 0; x * size < w; x++) {
                    g2.setColor(((x + y) % 2 == 0) ? new Color(48, 30, 26) : new Color(34, 20, 18));
                    g2.fillRect(x * size, y * size, size, size);
                }
            }
            g2.setColor(new Color(20, 10, 8, 140));
            g2.fillRect(0, 0, w, h);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ChessGame::new);
    }
}