package com.congueror.yesbot.command.chess;

import net.dv8tion.jda.internal.utils.tuple.ImmutablePair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;

public class ChessPosition {
    private static final BiFunction<ChessBoard, ChessPosition, List<ImmutablePair<int[], int[]>>> MOVES_FUNCTION = createMoves();
    private ChessPiece piece;
    private int[] pos;
    private int moves = 0;
    private int[] startPos;

    /**
     * Positions which any piece can go to when a king is under heavy attack. Empty when king is under no threat and pieces can move freely.
     */
    protected final ArrayList<int[]> checkmateAvoidancePos = new ArrayList<>();

    public ChessPosition(ChessPiece piece, int[] pos) {
        this.piece = piece;
        this.pos = pos;
        this.startPos = pos;

        //char first = (char) (pos[0] + 96);
        //int second = pos[1];
    }

    public ChessPiece getPiece() {
        return piece;
    }

    /**
     * @return 0: board x position, 1: board y position
     */
    public int[] getPos() {
        return pos;
    }

    public int[] getStartPos() {
        return startPos;
    }

    public int getMoves() {
        return moves;
    }

    public void setPiece(ChessPiece piece) {
        this.piece = piece;
    }

    public static boolean isInBounds(int[] position) {
        return position[0] < 8 && position[0] >= 0 && position[1] < 8 && position[1] >= 0;
    }

    public void reset() {
        this.pos = this.startPos;
    }

    public ChessPosition move(int[] to, boolean simulate) {
        if (isInBounds(to)) {
            pos = to;
            if (!simulate) {
                startPos = pos;
                moves++;
            }
        }
        return this;
    }

    /**
     * @param simulate   Whether this action will move the piece reset its start position.
     * @param directions A series of directions to the location that the piece will be moved to.
     * @return true if piece has moved, false if directed outside playable area or directions are empty
     * @see #move(int[], boolean)
     */
    public boolean move(boolean simulate, Direction... directions) {
        if (directions.length == 0) {
            return false;
        }
        int[] to = pos.clone();
        for (Direction dir : directions) {
            to = switch (dir) {
                case UP -> new int[]{to[0] - 1, to[1]};
                case DOWN -> new int[]{to[0] + 1, to[1]};
                case LEFT -> new int[]{to[0], to[1] - 1};
                case RIGHT -> new int[]{to[0], to[1] + 1};
            };
            if (!isInBounds(to)) {
                return false;
            }
        }
        this.move(to, simulate);
        return true;
    }

    public enum Direction {
        UP,
        DOWN,
        LEFT,
        RIGHT
    }

    /**
     * @return A BiFunction of the following types: <br>
     * T: The current chess game <br>
     * U: The position that wants to move <br>
     * R: A list of immutable pairs that contains all the possible moves the given position can make and the position to be captured by that movement.
     * More Specifically: <br>
     * &emsp Left: Position to move to <br>
     * &emsp Right: Position to be captured + whether king (e.g. new int[] {posX, posY, isKing}) <br>
     * NOTE: Right int[] length can be either 2 or 3
     */
    public static BiFunction<ChessBoard, ChessPosition, List<ImmutablePair<int[], int[]>>> getPossibleMoves() {
        return MOVES_FUNCTION;
    }

    @SuppressWarnings("ConstantConditions")
    private static BiFunction<ChessBoard, ChessPosition, List<ImmutablePair<int[], int[]>>> createMoves() {
        return (board, pos) -> {
            List<ImmutablePair<int[], int[]>> positions = new ArrayList<>();
            switch (pos.getPiece()) {
                case B_ROOK, W_ROOK -> straightMove(board, pos, positions);
                case B_KNIGHT, W_KNIGHT -> {
                    //up up left
                    if (pos.move(true, Direction.UP, Direction.UP, Direction.LEFT))
                        move(board, pos, positions);
                    pos.reset();
                    //up up right
                    if (pos.move(true, Direction.UP, Direction.UP, Direction.RIGHT))
                        move(board, pos, positions);
                    pos.reset();
                    //left left up
                    if (pos.move(true, Direction.LEFT, Direction.LEFT, Direction.UP))
                        move(board, pos, positions);
                    pos.reset();
                    //left left down
                    if (pos.move(true, Direction.LEFT, Direction.LEFT, Direction.DOWN))
                        move(board, pos, positions);
                    pos.reset();
                    //right right up
                    if (pos.move(true, Direction.RIGHT, Direction.RIGHT, Direction.UP))
                        move(board, pos, positions);
                    pos.reset();
                    //right right down
                    if (pos.move(true, Direction.RIGHT, Direction.RIGHT, Direction.DOWN))
                        move(board, pos, positions);
                    pos.reset();
                    //down down left
                    if (pos.move(true, Direction.DOWN, Direction.DOWN, Direction.LEFT))
                        move(board, pos, positions);
                    pos.reset();
                    //down down right
                    if (pos.move(true, Direction.DOWN, Direction.DOWN, Direction.RIGHT))
                        move(board, pos, positions);
                    pos.reset();
                }
                case B_BISHOP, W_BISHOP -> diagonalMove(board, pos, positions);
                case B_QUEEN, W_QUEEN -> {
                    straightMove(board, pos, positions);
                    diagonalMove(board, pos, positions);
                }
                case B_KING, W_KING -> {
                    //up
                    if (pos.move(true, Direction.UP))
                        move(board, pos, positions);
                    pos.reset();
                    //down
                    if (pos.move(true, Direction.DOWN))
                        move(board, pos, positions);
                    pos.reset();
                    //left
                    if (pos.move(true, Direction.LEFT))
                        move(board, pos, positions);
                    pos.reset();
                    //right
                    if (pos.move(true, Direction.RIGHT))
                        move(board, pos, positions);
                    pos.reset();
                    //left up
                    if (pos.move(true, Direction.LEFT, Direction.UP))
                        move(board, pos, positions);
                    pos.reset();
                    //left down
                    if (pos.move(true, Direction.LEFT, Direction.DOWN))
                        move(board, pos, positions);
                    pos.reset();
                    //right up
                    if (pos.move(true, Direction.RIGHT, Direction.UP))
                        move(board, pos, positions);
                    pos.reset();
                    //right down
                    if (pos.move(true, Direction.RIGHT, Direction.DOWN))
                        move(board, pos, positions);
                    pos.reset();
                }
                case B_PAWN -> {
                    //move down one and move down two
                    if (pos.move(true, Direction.DOWN))
                        if (board.getPosAt(pos) == null) {
                            if (canAddPos(pos))
                                positions.add(new ImmutablePair<>(pos.getPos(), null));
                            if (pos.move(true, Direction.DOWN))
                                if (board.getPosAt(pos) == null && pos.getMoves() < 1) {
                                    if (canAddPos(pos))
                                        positions.add(new ImmutablePair<>(pos.getPos(), null));
                                }
                        }
                    pos.reset();

                    //left down capture
                    if (pos.move(true, Direction.LEFT, Direction.DOWN) && board.canBeCaptured(pos.getPiece(), board.getPosAt(pos)))
                        if (canAddPos(pos))
                            //noinspection ConstantConditions
                            positions.add(new ImmutablePair<>(pos.getPos(), new int[]{pos.getPos()[0], pos.getPos()[1], board.getPosAt(pos).getPiece().isKing() ? 1 : 0}));
                    pos.reset();

                    //right down capture
                    if (pos.move(true, Direction.RIGHT, Direction.DOWN) && board.canBeCaptured(pos.getPiece(), board.getPosAt(pos))) {
                        if (canAddPos(pos))
                            //noinspection ConstantConditions
                            positions.add(new ImmutablePair<>(pos.getPos(), new int[]{pos.getPos()[0], pos.getPos()[1], board.getPosAt(pos).getPiece().isKing() ? 1 : 0}));
                    }
                    pos.reset();

                    //en passant capture left
                    if (pos.move(true, Direction.LEFT, Direction.DOWN) && board.getPosAt(pos) == null) {
                        pos.reset();
                        if (pos.move(true, Direction.LEFT) && board.canBeCaptured(pos.getPiece(), board.getPosAt(pos)) && board.getPosAt(pos).getPiece().isPawn()) {
                            if (board.getPosAt(pos).getMoves() == 1) {
                                pos.reset();
                                if (canAddPos(pos)) {
                                    pos.move(true, Direction.LEFT, Direction.DOWN);
                                    int[] left = pos.getPos().clone();
                                    pos.move(true, Direction.DOWN);
                                    int[] right = pos.getPos().clone();
                                    positions.add(new ImmutablePair<>(left, new int[]{right[0], right[1], board.getPosAt(pos).getPiece().isKing() ? 1 : 0}));
                                }
                            }
                        }
                    }
                    pos.reset();

                    //en passant capture right
                    if (pos.move(true, Direction.RIGHT, Direction.DOWN) && board.getPosAt(pos) == null) {
                        pos.reset();
                        if (pos.move(true, Direction.RIGHT) && board.canBeCaptured(pos.getPiece(), board.getPosAt(pos)) && board.getPosAt(pos).getPiece().isPawn()) {
                            if (board.getPosAt(pos).getMoves() == 1) {
                                pos.reset();
                                if (canAddPos(pos)) {
                                    pos.move(true, Direction.RIGHT, Direction.DOWN);
                                    int[] left = pos.getPos().clone();
                                    pos.move(true, Direction.DOWN);
                                    int[] right = pos.getPos().clone();
                                    positions.add(new ImmutablePair<>(left, new int[]{right[0], right[1], board.getPosAt(pos).getPiece().isKing() ? 1 : 0}));
                                }
                            }
                        }
                    }
                    pos.reset();
                }
                case W_PAWN -> {
                    //move up one and move up two
                    if (pos.move(true, Direction.UP))
                        if (board.getPosAt(pos) == null) {
                            if (canAddPos(pos))
                                positions.add(new ImmutablePair<>(pos.getPos(), null));
                            if (pos.move(true, Direction.UP))
                                if (board.getPosAt(pos) == null && pos.getMoves() < 1) {
                                    if (canAddPos(pos))
                                        positions.add(new ImmutablePair<>(pos.getPos(), null));
                                }
                        }
                    pos.reset();

                    //left up capture
                    if (pos.move(true, Direction.LEFT, Direction.UP) && board.canBeCaptured(pos.getPiece(), board.getPosAt(pos)))
                        if (canAddPos(pos))
                            //noinspection ConstantConditions
                            positions.add(new ImmutablePair<>(pos.getPos(), new int[]{pos.getPos()[0], pos.getPos()[1], board.getPosAt(pos).getPiece().isKing() ? 1 : 0}));
                    pos.reset();

                    //right up capture
                    if (pos.move(true, Direction.RIGHT, Direction.UP) && board.canBeCaptured(pos.getPiece(), board.getPosAt(pos)))
                        if (canAddPos(pos))
                            //noinspection ConstantConditions
                            positions.add(new ImmutablePair<>(pos.getPos(), new int[]{pos.getPos()[0], pos.getPos()[1], board.getPosAt(pos).getPiece().isKing() ? 1 : 0}));
                    pos.reset();

                    //en passant capture left
                    if (pos.move(true, Direction.LEFT, Direction.UP) && board.getPosAt(pos) == null) {
                        pos.reset();
                        if (pos.move(true, Direction.LEFT) && board.canBeCaptured(pos.getPiece(), board.getPosAt(pos)) && board.getPosAt(pos).getPiece().isPawn()) {
                            if (board.getPosAt(pos).getMoves() == 1) {
                                pos.reset();
                                if (canAddPos(pos)) {
                                    pos.move(true, Direction.LEFT, Direction.UP);
                                    int[] left = pos.getPos().clone();
                                    pos.move(true, Direction.DOWN);
                                    int[] right = pos.getPos().clone();
                                    positions.add(new ImmutablePair<>(left, new int[]{right[0], right[1], board.getPosAt(pos).getPiece().isKing() ? 1 : 0}));
                                }
                            }
                        }
                    }

                    /* old en passant capture
                    if (pos.move(true, Direction.LEFT))
                        if (board.canBeCaptured(pos.getPiece(), board.getPosAt(pos))) {
                            pos.reset();
                            if (pos.move(true, Direction.LEFT, Direction.UP))
                                if (board.getPosAt(pos) == null) {
                                    pos.reset();
                                    if (pos.move(true, Direction.LEFT))
                                        if (board.getPosAt(pos).getPiece().isPawn()) {
                                            pos.reset();
                                            if (pos.move(true, Direction.LEFT))
                                                if (board.getPosAt(pos).getMoves() == 1) {
                                                    pos.reset();
                                                    if (canAddPos(pos))
                                                        pos.move(true, Direction.LEFT, Direction.UP);
                                                    int[] left = pos.getPos().clone();
                                                    pos.move(true, Direction.DOWN);
                                                    int[] right = pos.getPos().clone();
                                                    positions.add(new ImmutablePair<>(left, new int[]{right[0], right[1], board.getPosAt(pos).getPiece().isKing() ? 1 : 0}));
                                                }
                                        }
                                }
                        }*/
                    pos.reset();

                    //en passant capture right
                    if (pos.move(true, Direction.RIGHT, Direction.UP) && board.getPosAt(pos) == null) {
                        pos.reset();
                        if (pos.move(true, Direction.RIGHT) && board.canBeCaptured(pos.getPiece(), board.getPosAt(pos)) && board.getPosAt(pos).getPiece().isPawn()) {
                            if (board.getPosAt(pos).getMoves() == 1) {
                                pos.reset();
                                if (canAddPos(pos)) {
                                    pos.move(true, Direction.RIGHT, Direction.UP);
                                    int[] left = pos.getPos().clone();
                                    pos.move(true, Direction.DOWN);
                                    int[] right = pos.getPos().clone();
                                    positions.add(new ImmutablePair<>(left, new int[]{right[0], right[1], board.getPosAt(pos).getPiece().isKing() ? 1 : 0}));
                                }
                            }
                        }
                    }
                    pos.reset();
                }
            }
            return positions;
        };
    }

    private static boolean canAddPos(ChessPosition pos) {
        return pos.checkmateAvoidancePos.isEmpty() || pos.checkmateAvoidancePos.contains(pos.getPos());
    }

    private static void straightMove(ChessBoard board, ChessPosition pos, List<ImmutablePair<int[], int[]>> positions) {
        //up
        for (int i = 1; i <= pos.getStartPos()[0]; i++) {
            if (pos.move(true, Direction.UP))
                if (move(board, pos, positions)) break;
        }
        pos.reset();
        //down
        for (int i = 1; i <= 7 - pos.getStartPos()[0]; i++) {
            if (pos.move(true, Direction.DOWN))
                if (move(board, pos, positions)) break;
        }
        pos.reset();
        //right
        for (int i = 1; i <= 7 - pos.getStartPos()[1]; i++) {
            if (pos.move(true, Direction.RIGHT))
                if (move(board, pos, positions)) break;
        }
        pos.reset();
        //left
        for (int i = 1; i <= pos.getStartPos()[1]; i++) {
            if (pos.move(true, Direction.LEFT))
                if (move(board, pos, positions)) break;
        }
        pos.reset();
    }

    private static void diagonalMove(ChessBoard board, ChessPosition pos, List<ImmutablePair<int[], int[]>> positions) {
        //right up
        for (int i = 1; i <= 7 - pos.getStartPos()[1]; i++) {
            if (pos.move(true, Direction.RIGHT, Direction.UP))
                if (move(board, pos, positions)) break;
        }
        pos.reset();
        //right down
        for (int i = 1; i <= 7 - pos.getStartPos()[1]; i++) {
            if (pos.move(true, Direction.RIGHT, Direction.DOWN))
                if (move(board, pos, positions)) break;
        }
        pos.reset();
        //left up
        for (int i = 1; i <= pos.getStartPos()[1]; i++) {
            if (pos.move(true, Direction.LEFT, Direction.UP))
                if (move(board, pos, positions)) break;
        }
        pos.reset();
        //left down
        for (int i = 1; i <= pos.getStartPos()[1]; i++) {
            if (pos.move(true, Direction.LEFT, Direction.DOWN))
                if (move(board, pos, positions)) break;
        }
        pos.reset();
    }

    private static boolean move(ChessBoard board, ChessPosition pos, List<ImmutablePair<int[], int[]>> positions) {
        ChessPosition pdksfg = board.getKingPosition(pos.getPiece().isBlack() ? ChessPiece.W_KING : ChessPiece.B_KING);
        int[] kingPosition = pdksfg.getPos().clone();

        ChessPosition boardPos = board.getPosAt(pos);
        if (boardPos == null && !Arrays.equals(kingPosition, pos.getPos())) {
            if (canAddPos(pos))
                positions.add(new ImmutablePair<>(pos.getPos(), null));
        } else {
            //noinspection ConstantConditions
            if (board.canBeCaptured(pos.getPiece(), boardPos) && (!boardPos.getPiece().isKing() || Arrays.equals(kingPosition, boardPos.getStartPos()))) {
                if (canAddPos(pos)) {
                    positions.add(new ImmutablePair<>(pos.getPos(), new int[]{boardPos.getPos()[0], boardPos.getPos()[1],
                            boardPos.getPiece().isKing() ? 1 : 0}));
                    return true;
                }
            } else if (Arrays.equals(kingPosition, pos.getPos())) {
                if (canAddPos(pos)) {
                    positions.add(new ImmutablePair<>(pos.getPos(), new int[]{pos.getPos()[0], pos.getPos()[1], 1}));
                    return true;
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChessPosition that = (ChessPosition) o;
        return Arrays.equals(pos, that.pos);
    }

    @Override
    public String toString() {
        return piece +
                ", pos=" + Arrays.toString(pos) +
                ", startPos=" + Arrays.toString(startPos);
    }
}
