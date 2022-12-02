package com.congueror.yesbot.command.chess;

import com.congueror.yesbot.MongoUser;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.internal.utils.tuple.ImmutablePair;

import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static com.congueror.yesbot.command.chess.ChessPiece.*;

public class ChessBoard {
    private static final Map<UUID, ChessBoard> CHESS_GAMES = new HashMap<>();

    public static boolean isInGame(String userId) {
        return CHESS_GAMES.values().stream().anyMatch(chessBoard -> chessBoard.userIds[0].equals(userId) || chessBoard.userIds[1].equals(userId));
    }

    @Nullable
    public static ChessBoard getGame(String userId) {
        return CHESS_GAMES.values().stream().filter(chessBoard -> chessBoard.userIds[0].equals(userId) || chessBoard.userIds[1].equals(userId)).findFirst().orElse(null);
    }

    @Nullable
    public static ChessBoard getGame(UUID uuid) {
        return CHESS_GAMES.get(uuid);
    }

    private final UUID uuid;
    private final ChessPosition[][] board;
    //White Player, Black Player
    public final String[] userIds;
    //fromX, fromY, toX, toY
    private final int[] lastMove = new int[]{-1, -1, -1, -1};
    private boolean isSimulation;

    public int turn = 0;

    public int[] requiresPromotion;
    /**
     * king-position, rook-position: from and to
     */
    public HashMap<ImmutablePair<Integer, Integer>, int[]> castlingPositions = new HashMap<>();
    public int[] checkedPosition;
    public int winnerIndex = 3;
    public boolean isStalemate;

    public static ChessBoard newChessBoard(String[] userIds) {
        if (true) {
            return newTestChessBoard(userIds);
        } else
            return new ChessBoard(new ChessPiece[][]
                    {
                            {B_ROOK, B_KNIGHT, B_BISHOP, B_QUEEN, B_KING, B_BISHOP, B_KNIGHT, B_ROOK},
                            {B_PAWN, B_PAWN, B_PAWN, B_PAWN, B_PAWN, B_PAWN, B_PAWN, B_PAWN},
                            {null, null, null, null, null, null, null, null},
                            {null, null, null, null, null, null, null, null},
                            {null, null, null, null, null, null, null, null},
                            {null, null, null, null, null, null, null, null},
                            {W_PAWN, W_PAWN, W_PAWN, W_PAWN, W_PAWN, W_PAWN, W_PAWN, W_PAWN},
                            {W_ROOK, W_KNIGHT, W_BISHOP, W_QUEEN, W_KING, W_BISHOP, W_KNIGHT, W_ROOK},
                    }, userIds);
    }

    public static ChessBoard newTestChessBoard(String[] userIds) {
        return new ChessBoard(new ChessPiece[][]
                {
                        {null, null, null, null, null, null, B_KING, null},
                        {null, null, null, null, null, null, null, null},
                        {null, null, null, null, null, W_KING, null, null},
                        {null, null, null, null, null, null, null, null},
                        {null, null, null, null, null, null, null, null},
                        {null, null, null, null, null, null, null, null},
                        {null, null, null, null, null, null, null, null},
                        {W_ROOK, null, null, null, null, null, null, W_ROOK},
                }, userIds);
    }

    public ChessBoard(ChessPiece[][] board, String[] userIds) {
        this.userIds = userIds;
        this.uuid = UUID.randomUUID();
        ChessPosition[][] pieces = new ChessPosition[8][8];
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                pieces[i][j] = (board[i][j] == null) ? null : new ChessPosition(board[i][j], new int[]{i, j});
            }
        }
        this.board = pieces;
        save();
    }

    public ChessBoard(ChessPosition[][] board, String[] userIds) {
        this.userIds = userIds;
        this.uuid = UUID.randomUUID();
        this.board = board;
        save();
    }

    public UUID getUUID() {
        return uuid;
    }

    public ChessPosition[][] getBoard() {
        return board;
    }

    private void setSimulation() {
        this.isSimulation = true;
    }

    public String getOpponent(String userId) {
        return userIds[0].equals(userId) ? userIds[1] : userIds[0];
    }

    public void save() {
        if (userIds.length == 2 && userIds[0] != null && userIds[1] != null)
            CHESS_GAMES.put(uuid, this);
    }

    /**
     * Gets the position on the board of the simulated position(pos, not startPos) of the given ChessPosition. <br>
     *
     * @param pos simulated ChessPosition you wish to get.
     * @return
     * @see #getPosAt(int[])
     */
    @Nullable
    public ChessPosition getPosAt(ChessPosition pos) {
        int[] pos1 = pos.getPos();
        return getPosAt(pos1);
    }

    /**
     * Gets the position on the board on the given coordinates.
     *
     * @param pos Array of coordinates representing x and y on the board.
     * @return
     * @see #getPosAt(ChessPosition)
     */
    @Nullable
    public ChessPosition getPosAt(int[] pos) {
        try {
            return board[pos[0]][pos[1]];
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    public boolean isTurn(String userId) {
        return userIds[turn].equals(userId);
    }

    /**
     * Whether the given position can be captured.<br>
     * Specifically:<br>
     * Checks if position to be captured is null<br>
     * Checks if the capturing piece is the same color as the to-be-captured position.
     *
     * @param piece    The piece that is capturing
     * @param position The position to be captured.
     */
    public boolean canBeCaptured(ChessPiece piece, ChessPosition position) {
        return position != null && !piece.isSameSide(position.getPiece());
    }

    /**
     * Checks if given king position is checked. Specifically: <br>
     * It iterates through every chess piece on board and determines whether given pos is a target to any.
     *
     * @param pos
     * @return
     */
    public boolean isChecked(ChessPosition pos) {
        if (pos.getPiece().isKing()) {
            AtomicBoolean checked = new AtomicBoolean();

            forEachBreakable((p, b) -> {
                if (p != null && !p.getPiece().isSameSide(pos.getPiece())) {
                    if (p.getPiece().isKing()) {
                        int x1 = pos.getPos()[0], y1 = pos.getPos()[1], x2 = p.getPos()[0], y2 = p.getPos()[1];
                        double distance = Math.sqrt( Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2) );
                        if (distance < Math.sqrt(4)) {
                            checked.set(true);
                            b.set(true);
                        }
                    } else {
                        var moves = ChessPosition.getPossibleMoves().apply(this, p);
                        for (var a : moves) {
                            if (pos.getPos()[0] == a.left[0] && pos.getPos()[1] == a.left[1]) {
                                checked.set(true);
                                b.set(true);
                                break;
                            } else if (a.right != null && a.right[0] == pos.getPos()[0] && a.right[1] == pos.getPos()[1]) {
                                checked.set(true);
                                b.set(true);
                                break;
                            }
                        }
                    }
                }
            });

            return checked.get();
        }
        return false;
    }

    /**
     * Attempt to move a piece.
     *
     * @return 0: if piece successfully moved.
     * 1: if piece cannot move due to invalid 'from' position.
     * 2: if piece cannot move due to invalid 'to' position.
     * 3: if player is invalid.
     * 4: if it requires pawn promotion.
     */
    public int move(String userId, int[] fromPos, int[] to) {
        if (requiresPromotion == null) {
            ChessPosition from = board[fromPos[0]][fromPos[1]];

            if (userIds[0].equals(userId)) {
                if (from != null && from.getPiece().isWhite()) {
                    return moveInternal(to, from, W_KING);
                }
                return 1;
            } else if (userIds[1].equals(userId)) {
                if (from != null && from.getPiece().isBlack()) {
                    return moveInternal(to, from, B_KING);
                }
                return 1;
            }
            return 3;
        }
        return 4;
    }

    private int moveInternal(int[] to, ChessPosition from, ChessPiece king) {
        if (ChessPosition.isInBounds(to)) {
            var pair = ChessPosition.getPossibleMoves().apply(this, from).stream()
                    .filter(a -> Arrays.equals(a.getLeft(), to)).findAny().orElse(null);
            if (pair != null) {
                if (pair.right != null && pair.right.length == 3 && pair.right[2] == 1) {
                    return 2;
                }
                from.move(to, true);
                if (!isChecked(getKingPosition(king))) {
                    from.reset();
                    if (this.move(from, to, pair)) {
                        return 0;
                    }
                }
            }
        }
        from.reset();
        return 2;
    }

    /**
     * @return true if successfully moved, false if checkmate.
     */
    private boolean move(ChessPosition from, int[] to, ImmutablePair<int[], int[]> pair) {
        lastMove[0] = from.getPos()[0];
        lastMove[1] = from.getPos()[1];
        lastMove[2] = to[0];
        lastMove[3] = to[1];

        board[from.getPos()[0]][from.getPos()[1]] = null;
        if (pair.right != null)
            board[pair.right[0]][pair.right[1]] = null;
        board[to[0]][to[1]] = from.move(to, false);

        //pawn promotion
        if (from.getPiece().isPawn() && from.getPiece().isWhite() && to[0] == 0) {
            requiresPromotion = to;
        } else if (from.getPiece().isPawn() && from.getPiece().isBlack() && to[0] == 7) {
            requiresPromotion = to;
        }

        //castling
        var castling = castlingPositions.get(ImmutablePair.of(to[0], to[1]));
        if (from.getPiece().isKing() && castling != null) {
            var rook = getPosAt(castling);
            assert rook != null;
            move(rook, new int[]{castling[2], castling[3]}, new ImmutablePair<>(null, null));
        }
        castlingPositions.clear();

        //is checked
        if (isChecked(getKingPosition(W_KING))) {
            checkedPosition = getKingPosition(W_KING).getPos();
        } else if (isChecked(getKingPosition(B_KING))) {
            checkedPosition = getKingPosition(B_KING).getPos();
        } else {
            checkedPosition = null;
        }

        //is stalemate(only kings remain)
        isStalemate = true;
        forEach(p -> {
            if (p != null && !p.getPiece().isKing()) {
                isStalemate = false;
            }
        });

        //is checkmate
        if (isCheckmate(W_KING)) {
            winnerIndex = 1;
        } else if (isCheckmate(B_KING)) {
            winnerIndex = 0;
        }

        //is stalemate(no possible moves)
        AtomicBoolean hasMoves = new AtomicBoolean(false);
        forEach(position -> {
            if (position != null) {
                if (!ChessPosition.getPossibleMoves().apply(this, position).isEmpty()) {
                    hasMoves.set(true);
                }
            }
        });
        if (!hasMoves.get()) {
            isStalemate = true;
        }

        save();

        if (isStalemate) {
            initiateStalemateSequence();
        }
        if (winnerIndex != 3) {
            initiateWinnerSequence();
        }
        return true;
    }

    /**
     * Checks whether a king has been checkmated. Specifically: <br>
     * If king has possible moves which are not checked (hasMoves == true), then false is returned. <br>
     * If king has no possible moves and is checked, then all friendly piece moves are iterated determining whether the check can be broken. (canMove)
     *
     * @param kingPiece
     * @return
     */
    private boolean isCheckmate(ChessPiece kingPiece) {
        AtomicBoolean hasMoves = new AtomicBoolean();
        ChessPosition king = getKingPosition(kingPiece);

        ChessPosition.getPossibleMoves().apply(this, king).forEach(p -> {
            king.move(p.left, true);
            if (!isChecked(king)) {
                hasMoves.set(true);
            }
            king.reset();
        });
        AtomicBoolean canMove = new AtomicBoolean();
        if (!hasMoves.get() && isChecked(king)) {
            forEach(position -> {
                if (position != null && position.getPiece().isSameSide(kingPiece)) {
                    ChessPosition.getPossibleMoves().apply(this, position).forEach(p -> {
                        ChessBoard simulatedBoard = new ChessBoard(this.board.clone(), new String[]{"simulation0", "simulation1", ""});
                        simulatedBoard.setSimulation();
                        simulatedBoard.move(position.getPiece().isWhite() ? "simulation0" : "simulation1", position.getPos().clone(), p.left.clone());
                        //simulatedBoard.moveInternal(p.left, position, position.getPiece());
                        if (!simulatedBoard.isChecked(king)) {
                            position.checkmateAvoidancePos.add(p.left);
                            canMove.set(true);
                        }
                    });
                }
            });
        }
        return !canMove.get() && !hasMoves.get() && isChecked(king);
    }

    public ChessPosition getKingPosition(ChessPiece kingColor) {
        AtomicReference<ChessPosition> p = new AtomicReference<>();
        forEach(position -> {
            if (position != null && position.getPiece().isKing() && position.getPiece().isSameSide(kingColor)) {
                p.set(position);
            }
        });
        return p.get();
    }

    /**
     * Iterates through every position in the chess board and accepts the given consumer.
     *
     * @param action A Consumer that takes in the current ChessPosition of the iteration process. <br>
     *               WARNING: Can be nullable.
     */
    public void forEach(Consumer<ChessPosition> action) {
        for (int i = 0; i < getBoard().length; i++) {
            for (int j = 0; j < getBoard()[i].length; j++) {
                ChessPosition p = getBoard()[i][j];
                action.accept(p);
            }
        }
    }

    public void forEachBreakable(BiConsumer<ChessPosition, AtomicBoolean> action) {
        AtomicBoolean breakz = new AtomicBoolean();
        hello:
        {
            for (int i = 0; i < getBoard().length; i++) {
                for (int j = 0; j < getBoard()[i].length; j++) {
                    ChessPosition p = getBoard()[i][j];
                    action.accept(p, breakz);
                    if (breakz.get())
                        break hello;
                }
            }
        }
    }

    public void promote(ChessPiece piece) {
        if (requiresPromotion != null) {
            ChessPosition pos = board[requiresPromotion[0]][requiresPromotion[1]];
            if (piece != null)
                pos.setPiece(piece);
            requiresPromotion = null;
        }
    }

    public void finishTurn() {
        turn = turn == 0 ? 1 : 0;
    }

    public void initiateWinnerSequence() {
        if (!isSimulation) {
            String winner = User.fromId(userIds[winnerIndex]).getId();
            String loser = User.fromId(userIds[winnerIndex == 1 ? 0 : 1]).getId();
            MongoUser.addChessWin(winner);
            MongoUser.addChessLoss(loser);

            CHESS_GAMES.remove(uuid);
        }
    }

    public void initiateStalemateSequence() {
        if (!isSimulation) {
            String user1 = User.fromId(userIds[0]).getId();
            String user2 = User.fromId(userIds[1]).getId();
            MongoUser.addChessTie(user1);
            MongoUser.addChessTie(user2);

            CHESS_GAMES.remove(uuid);
        }
    }

    @SuppressWarnings("ConstantConditions")
    public File drawBoard(@Nullable int[] drawnMove) {
        ChessBoardType boardType = MongoUser.getSelectedBoard(userIds[0]);
        ChessPieceType pieceType = MongoUser.getSelectedPiece(userIds[0]);

        try {

            BufferedImage main = new BufferedImage(1168 + 146 * 2, 1166 + 146 * 2, BufferedImage.TYPE_INT_ARGB);

            Graphics2D a = main.createGraphics();


            if (boardType.tileLocation == null) {
                for (int i = 0; i < this.board.length; i++) {
                    a.setColor(a.getColor().equals(boardType.firstColor) ? boardType.secondColor : boardType.firstColor);
                    for (int j = 0; j < this.board[i].length; j++) {
                        a.setColor(a.getColor().equals(boardType.secondColor) ? boardType.firstColor : boardType.secondColor);
                        a.fillRect((j + 1) * 146, (i + 1) * 146, 146, 146);
                    }
                }
            }
            //BufferedImage board = ImageIO.read(getClass().getClassLoader().getResource("chess/board.png"));
            //a.drawImage(board, 146, 146, null);


            a.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 80));
            a.setColor(new Color(165, 42, 42));
            for (int i = 1; i <= 8; i++) {
                a.drawString(i + "", 146 / 2, (10 - i) * 146 - 146 / 4);
                a.drawString(i + "", main.getWidth() - 146 + 20, (10 - i) * 146 - 146 / 4);
                a.drawString((char) (i + 96) + "", (i) * 146 + 146 / 2 - 20, main.getHeight() - 146 + 70);
                a.drawString((char) (i + 96) + "", (i) * 146 + 146 / 2 - 20, 146 / 2 + 47);
            }

            if (lastMove[0] != -1 && lastMove[1] != -1 && lastMove[2] != -1 && lastMove[3] != -1) {
                a.fillRect(146 * lastMove[1] + 146, 146 * lastMove[0] + 146, 146, 146);
                a.fillRect(146 * lastMove[3] + 146, 146 * lastMove[2] + 146, 146, 146);
            }

            BufferedImage pieces = ImageIO.read(getClass().getClassLoader().getResource(pieceType.location));
            for (int i = 0; i < this.board.length; i++) {
                for (int j = 0; j < this.board[i].length; j++) {
                    ChessPosition pos = this.board[i][j];
                    if (pos != null) {
                        a.drawImage(pieces,
                                (j + 1) * 146, (i + 1) * 146, (j + 1) * 146 + 146, (i + 1) * 146 + 146,
                                pos.getPiece().x1, pos.getPiece().y1, pos.getPiece().x2, pos.getPiece().y2, null);
                    }
                }
            }

            if (drawnMove != null) {
                ChessPosition pos = board[drawnMove[0]][drawnMove[1]];
                ChessPosition.getPossibleMoves().apply(this, pos).forEach(p -> {
                    if (p.right == null) {
                        a.fillOval(146 * p.left[1] + 146 + 146 / 2 - 146 / 8, 146 * p.left[0] + 146 + 146 / 2 - 146 / 8, 146 / 4, 146 / 4);
                    } else {
                        a.drawLine(146 * p.left[1] + 146, 146 * p.left[0] + 146, 146 * p.left[1] + 146 * 2, 146 * p.left[0] + 146 * 2);
                        a.drawLine(146 * p.left[1] + 146 + 146, 146 * p.left[0] + 146, 146 * p.left[1] + 146, 146 * p.left[0] + 146 * 2);
                    }
                });
            }

            a.dispose();

            ImageIO.write(main, "png", new File(getClass().getClassLoader().getResource("chess/temp.png").toURI()));
            return new File(getClass().getClassLoader().getResource("chess/temp.png").toURI());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}