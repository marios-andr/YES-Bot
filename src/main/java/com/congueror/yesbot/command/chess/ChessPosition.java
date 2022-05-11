package com.congueror.yesbot.command.chess;

import net.dv8tion.jda.internal.utils.tuple.ImmutablePair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;

public class ChessPosition {
    private ChessPiece piece;
    private int[] pos;
    private int moves = 0;
    private int[] startPos;

    protected final List<int[]> checkmateAvoidancePos = new ArrayList<>();

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
     * The current chess game <br>
     * The position that wants to move <br>
     * A list of immutable pairs that contains all the possible moves the given position can make and the position to be captured by that movement.
     * More Specifically: <br>
     * &emsp Left: Position to move to <br>
     * &emsp Right: Position to be captured + whether king (e.g. new int[] {posX, posY, isKing}) <br>
     * NOTE: Right int[] length can be either 2 or 3
     */
    public static BiFunction<ChessBoard, ChessPosition, List<ImmutablePair<int[], int[]>>> getPossibleMoves() {
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
                            if (pos.checkmateAvoidancePos.isEmpty() || pos.checkmateAvoidancePos.contains(pos.getPos()))
                                positions.add(new ImmutablePair<>(pos.getPos(), null));
                            if (pos.move(true, Direction.DOWN))
                                if (board.getPosAt(pos) == null && pos.getMoves() < 1) {
                                    if (pos.checkmateAvoidancePos.isEmpty() || pos.checkmateAvoidancePos.contains(pos.getPos()))
                                        positions.add(new ImmutablePair<>(pos.getPos(), null));
                                }
                        }
                    pos.reset();
                    //left down capture
                    if (pos.move(true, Direction.LEFT, Direction.DOWN))
                        if (board.canBeCaptured(pos.getPiece(), board.getPosAt(pos))) {
                            if (pos.checkmateAvoidancePos.isEmpty() || pos.checkmateAvoidancePos.contains(pos.getPos()))
                                positions.add(new ImmutablePair<>(pos.getPos(), new int[]{pos.getPos()[0], pos.getPos()[1], board.getPosAt(pos).getPiece().isKing() ? 1 : 0}));
                        }
                    pos.reset();
                    //right down capture
                    if (pos.move(true, Direction.RIGHT, Direction.DOWN))
                        if (board.canBeCaptured(pos.getPiece(), board.getPosAt(pos))) {
                            if (pos.checkmateAvoidancePos.isEmpty() || pos.checkmateAvoidancePos.contains(pos.getPos()))
                                positions.add(new ImmutablePair<>(pos.getPos(), new int[]{pos.getPos()[0], pos.getPos()[1], board.getPosAt(pos).getPiece().isKing() ? 1 : 0}));
                        }
                    pos.reset();
                    //en passant capture
                    if (pos.move(true, Direction.LEFT))
                        if (board.getPosAt(pos) != null) {
                            pos.reset();
                            if (pos.move(true, Direction.LEFT, Direction.DOWN))
                                if (board.getPosAt(pos) == null) {
                                    pos.reset();
                                    if (pos.move(true, Direction.LEFT))
                                        //noinspection ConstantConditions
                                        if (board.getPosAt(pos).getPiece().isPawn()) {
                                            pos.reset();
                                            if (pos.move(true, Direction.LEFT))
                                                //noinspection ConstantConditions
                                                if (board.getPosAt(pos).getMoves() == 1) {
                                                    pos.reset();
                                                    if (pos.checkmateAvoidancePos.isEmpty() || pos.checkmateAvoidancePos.contains(pos.getPos()))
                                                        pos.move(true, Direction.LEFT, Direction.DOWN);
                                                        int[] left = pos.getPos().clone();
                                                        pos.move(true, Direction.UP);
                                                        int[] right = pos.getPos().clone();
                                                        positions.add(new ImmutablePair<>(left, new int[]{right[0], right[1], board.getPosAt(pos).getPiece().isKing() ? 1 : 0}));
                                                }
                                        }
                                }
                        }
                    pos.reset();
                    if (board.getPosAt(pos.right(true)) != null) {
                        pos.reset();
                        if (board.getPosAt(pos.rightDown(true)) == null) {
                            pos.reset();
                            //noinspection ConstantConditions
                            if (board.getPosAt(pos.right(true)).getPiece().isPawn()) {
                                pos.reset();
                                //noinspection ConstantConditions
                                if (board.getPosAt(pos.right(true)).getMoves() == 1) {
                                    pos.reset();
                                    if (pos.checkmateAvoidancePos.isEmpty() || pos.checkmateAvoidancePos.contains(pos.getPos()))
                                        positions.add(new ImmutablePair<>(pos.rightDown(true).getPos(), new int[]{pos.up(true).getPos()[0], pos.getPos()[1], board.getPosAt(pos).getPiece().isKing() ? 1 : 0}));
                                }
                            }
                        }
                    }
                    pos.reset();
                }
                case W_PAWN -> {
                    //move up one and move up two
                    if (board.getPosAt(pos.up(true)) == null) {
                        if (pos.checkmateAvoidancePos.isEmpty() || pos.checkmateAvoidancePos.contains(pos.getPos()))
                            positions.add(new ImmutablePair<>(pos.getPos(), null));
                        if (board.getPosAt(pos.up(true)) == null && pos.getMoves() < 1) {
                            if (pos.checkmateAvoidancePos.isEmpty() || pos.checkmateAvoidancePos.contains(pos.getPos()))
                                positions.add(new ImmutablePair<>(pos.getPos(), null));
                        }
                    }
                    pos.reset();
                    //left up capture
                    if (board.canBeCaptured(pos.getPiece(), board.getPosAt(pos.leftUp(true)))) {
                        if (pos.checkmateAvoidancePos.isEmpty() || pos.checkmateAvoidancePos.contains(pos.getPos()))
                            positions.add(new ImmutablePair<>(pos.getPos(), new int[]{pos.getPos()[0], pos.getPos()[1], board.getPosAt(pos).getPiece().isKing() ? 1 : 0}));
                    }
                    pos.reset();
                    //right up capture
                    if (board.canBeCaptured(pos.getPiece(), board.getPosAt(pos.rightUp(true)))) {
                        if (pos.checkmateAvoidancePos.isEmpty() || pos.checkmateAvoidancePos.contains(pos.getPos()))
                            positions.add(new ImmutablePair<>(pos.getPos(), new int[]{pos.getPos()[0], pos.getPos()[1], board.getPosAt(pos).getPiece().isKing() ? 1 : 0}));
                    }
                    pos.reset();
                    //en passant capture
                    if (board.canBeCaptured(pos.getPiece(), board.getPosAt(pos.left(true)))) {
                        pos.reset();
                        if (board.getPosAt(pos.leftUp(true)) == null) {
                            pos.reset();
                            //noinspection ConstantConditions
                            if (board.getPosAt(pos.left(true)).getPiece().isPawn()) {
                                pos.reset();
                                //noinspection ConstantConditions
                                if (board.getPosAt(pos.left(true)).getMoves() == 1) {
                                    pos.reset();
                                    if (pos.checkmateAvoidancePos.isEmpty() || pos.checkmateAvoidancePos.contains(pos.getPos()))
                                        positions.add(new ImmutablePair<>(pos.leftUp(true).getPos(), new int[]{pos.down(true).getPos()[0], pos.getPos()[1], board.getPosAt(pos).getPiece().isKing() ? 1 : 0}));
                                }
                            }
                        }
                    }
                    pos.reset();
                    if (board.canBeCaptured(pos.getPiece(), board.getPosAt(pos.right(true)))) {
                        pos.reset();
                        if (board.getPosAt(pos.rightUp(true)) == null) {
                            pos.reset();
                            //noinspection ConstantConditions
                            if (board.getPosAt(pos.right(true)).getPiece().isPawn()) {
                                pos.reset();
                                //noinspection ConstantConditions
                                if (board.getPosAt(pos.right(true)).getMoves() == 1) {
                                    pos.reset();
                                    if (pos.checkmateAvoidancePos.isEmpty() || pos.checkmateAvoidancePos.contains(pos.getPos()))
                                        positions.add(new ImmutablePair<>(pos.rightUp(true).getPos(), new int[]{pos.down(true).getPos()[0], pos.getPos()[1], board.getPosAt(pos).getPiece().isKing() ? 1 : 0}));
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
        int[] kingPosition = new int[]{board.getKingPosition(pos.getPiece().isBlack() ? ChessPiece.W_KING : ChessPiece.B_KING).getPos()[0],
                board.getKingPosition(pos.getPiece().isBlack() ? ChessPiece.W_KING : ChessPiece.B_KING).getPos()[1]};

        if (board.getPosAt(pos) == null && !Arrays.equals(kingPosition, pos.getPos())) {
            if (pos.checkmateAvoidancePos.isEmpty() || pos.checkmateAvoidancePos.contains(pos.getPos()))
                positions.add(new ImmutablePair<>(pos.getPos(), null));
        } else {
            ChessPosition boardPos = board.getPosAt(pos);
            if (board.canBeCaptured(pos.getPiece(), boardPos) && (!boardPos.getPiece().isKing() || Arrays.equals(kingPosition, boardPos.getStartPos()))) {
                if (pos.checkmateAvoidancePos.isEmpty() || pos.checkmateAvoidancePos.contains(pos.getPos())) {
                    positions.add(new ImmutablePair<>(pos.getPos(), new int[]{boardPos.getPos()[0], boardPos.getPos()[1],
                            boardPos.getPiece().isKing() ? 1 : 0}));
                    return true;
                }
            } else if (Arrays.equals(kingPosition, pos.getPos())) {
                if (pos.checkmateAvoidancePos.isEmpty() || pos.checkmateAvoidancePos.contains(pos.getPos())) {
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
}
