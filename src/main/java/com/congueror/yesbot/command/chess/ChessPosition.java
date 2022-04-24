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

    public ChessPosition move(Direction dir, int amount, boolean simulate) {
        int[] simulated = switch (dir) {
            case UP -> new int[]{pos[0] - amount, pos[1]};
            case DOWN -> new int[]{pos[0] + amount, pos[1]};
            case LEFT -> new int[]{pos[0], pos[1] - amount};
            case RIGHT -> new int[]{pos[0], pos[1] + amount};
            case LEFT_UP -> new int[]{pos[0] - amount, pos[1] - amount};
            case LEFT_DOWN -> new int[]{pos[0] + amount, pos[1] - amount};
            case RIGHT_UP -> new int[]{pos[0] - amount, pos[1] + amount};
            case RIGHT_DOWN -> new int[]{pos[0] + amount, pos[1] + amount};
        };
        return this.move(simulated, simulate);
    }

    public ChessPosition up(boolean simulate) {
        return move(Direction.UP, 1, simulate);
    }

    public ChessPosition down(boolean simulate) {
        return move(Direction.DOWN, 1, simulate);
    }

    public ChessPosition left(boolean simulate) {
        return move(Direction.LEFT, 1, simulate);
    }

    public ChessPosition right(boolean simulate) {
        return move(Direction.RIGHT, 1, simulate);
    }

    public ChessPosition leftUp(boolean simulate) {
        return move(Direction.LEFT_UP, 1, simulate);
    }

    public ChessPosition leftDown(boolean simulate) {
        return move(Direction.LEFT_DOWN, 1, simulate);
    }

    public ChessPosition rightUp(boolean simulate) {
        return move(Direction.RIGHT_UP, 1, simulate);
    }

    public ChessPosition rightDown(boolean simulate) {
        return move(Direction.RIGHT_DOWN, 1, simulate);
    }

    public enum Direction {
        UP,
        DOWN,
        LEFT,
        RIGHT,
        LEFT_UP,
        LEFT_DOWN,
        RIGHT_UP,
        RIGHT_DOWN

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
                    int[] kingPosition = new int[]{board.getKingPosition(pos.getPiece().isBlack() ? ChessPiece.W_KING : ChessPiece.B_KING).getPos()[0],
                            board.getKingPosition(pos.getPiece().isBlack() ? ChessPiece.W_KING : ChessPiece.B_KING).getPos()[1]};
                    //up up left
                    pos.up(true).up(true).left(true);
                    move(board, pos, positions, kingPosition);
                    pos.reset();
                    //up up right
                    pos.up(true).up(true).right(true);
                    move(board, pos, positions, kingPosition);
                    pos.reset();
                    //left left up
                    pos.left(true).left(true).up(true);
                    move(board, pos, positions, kingPosition);
                    pos.reset();
                    //left left down
                    pos.left(true).left(true).down(true);
                    move(board, pos, positions, kingPosition);
                    pos.reset();
                    //right right up
                    pos.right(true).right(true).up(true);
                    move(board, pos, positions, kingPosition);
                    pos.reset();
                    //right right down
                    pos.right(true).right(true).down(true);
                    move(board, pos, positions, kingPosition);
                    pos.reset();
                    //down down left
                    pos.down(true).down(true).left(true);
                    move(board, pos, positions, kingPosition);
                    pos.reset();
                    //down down right
                    pos.down(true).down(true).right(true);
                    move(board, pos, positions, kingPosition);
                    pos.reset();
                }
                case B_BISHOP, W_BISHOP -> diagonalMove(board, pos, positions);
                case B_QUEEN, W_QUEEN -> {
                    straightMove(board, pos, positions);
                    diagonalMove(board, pos, positions);
                }
                case B_KING, W_KING -> {
                    int[] kingPosition = new int[]{board.getKingPosition(pos.getPiece().isBlack() ? ChessPiece.W_KING : ChessPiece.B_KING).getPos()[0],
                            board.getKingPosition(pos.getPiece().isBlack() ? ChessPiece.W_KING : ChessPiece.B_KING).getPos()[1]};
                    //up
                    pos.up(true);
                    move(board, pos, positions, kingPosition);
                    pos.reset();
                    //down
                    pos.down(true);
                    move(board, pos, positions, kingPosition);
                    pos.reset();
                    //left
                    pos.left(true);
                    move(board, pos, positions, kingPosition);
                    pos.reset();
                    //right
                    pos.right(true);
                    move(board, pos, positions, kingPosition);
                    pos.reset();
                    //left up
                    pos.leftUp(true);
                    move(board, pos, positions, kingPosition);
                    pos.reset();
                    //left down
                    pos.leftDown(true);
                    move(board, pos, positions, kingPosition);
                    pos.reset();
                    //right up
                    pos.rightUp(true);
                    move(board, pos, positions, kingPosition);
                    pos.reset();
                    //right down
                    pos.rightDown(true);
                    move(board, pos, positions, kingPosition);
                    pos.reset();
                }
                case B_PAWN -> {
                    //move down one and move down two
                    if (board.getPosAt(pos.down(true)) == null) {
                        if (pos.checkmateAvoidancePos.isEmpty() || pos.checkmateAvoidancePos.contains(pos.getPos()))
                            positions.add(new ImmutablePair<>(pos.getPos(), null));
                        if (board.getPosAt(pos.down(true)) == null && pos.getMoves() < 1) {
                            if (pos.checkmateAvoidancePos.isEmpty() || pos.checkmateAvoidancePos.contains(pos.getPos()))
                                positions.add(new ImmutablePair<>(pos.getPos(), null));
                        }
                    }
                    pos.reset();
                    //left down capture
                    if (board.canBeCaptured(pos.getPiece(), board.getPosAt(pos.leftDown(true)))) {
                        if (pos.checkmateAvoidancePos.isEmpty() || pos.checkmateAvoidancePos.contains(pos.getPos()))
                            positions.add(new ImmutablePair<>(pos.getPos(), new int[]{pos.getPos()[0], pos.getPos()[1], board.getPosAt(pos).getPiece().isKing() ? 1 : 0}));
                    }
                    pos.reset();
                    //right down capture
                    if (board.canBeCaptured(pos.getPiece(), board.getPosAt(pos.rightDown(true)))) {
                        if (pos.checkmateAvoidancePos.isEmpty() || pos.checkmateAvoidancePos.contains(pos.getPos()))
                            positions.add(new ImmutablePair<>(pos.getPos(), new int[]{pos.getPos()[0], pos.getPos()[1], board.getPosAt(pos).getPiece().isKing() ? 1 : 0}));
                    }
                    pos.reset();
                    //en passant capture
                    if (board.getPosAt(pos.left(true)) != null) {
                        pos.reset();
                        if (board.getPosAt(pos.leftDown(true)) == null) {
                            pos.reset();
                            //noinspection ConstantConditions
                            if (board.getPosAt(pos.left(true)).getPiece().isPawn()) {
                                pos.reset();
                                //noinspection ConstantConditions
                                if (board.getPosAt(pos.left(true)).getMoves() == 1) {
                                    pos.reset();
                                    if (pos.checkmateAvoidancePos.isEmpty() || pos.checkmateAvoidancePos.contains(pos.getPos()))
                                        positions.add(new ImmutablePair<>(pos.leftDown(true).getPos(), new int[]{pos.up(true).getPos()[0], pos.getPos()[1], board.getPosAt(pos).getPiece().isKing() ? 1 : 0}));
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
        int[] kingPosition = new int[]{board.getKingPosition(pos.getPiece().isBlack() ? ChessPiece.W_KING : ChessPiece.B_KING).getPos()[0],
                board.getKingPosition(pos.getPiece().isBlack() ? ChessPiece.W_KING : ChessPiece.B_KING).getPos()[1]};
        //up
        for (int i = 1; i <= pos.getStartPos()[0]; i++) {
            pos.up(true);
            if (move(board, pos, positions, kingPosition)) break;
        }
        pos.reset();
        //down
        for (int i = 1; i <= 7 - pos.getStartPos()[0]; i++) {
            pos.down(true);
            if (move(board, pos, positions, kingPosition)) break;
        }
        pos.reset();
        //right
        for (int i = 1; i <= 7 - pos.getStartPos()[1]; i++) {
            pos.right(true);
            if (move(board, pos, positions, kingPosition)) break;
        }
        pos.reset();
        //left
        for (int i = 1; i <= pos.getStartPos()[1]; i++) {
            pos.left(true);
            if (move(board, pos, positions, kingPosition)) break;
        }
        pos.reset();
    }

    private static void diagonalMove(ChessBoard board, ChessPosition pos, List<ImmutablePair<int[], int[]>> positions) {
        int[] kingPosition = new int[]{board.getKingPosition(pos.getPiece().isBlack() ? ChessPiece.W_KING : ChessPiece.B_KING).getPos()[0],
                board.getKingPosition(pos.getPiece().isBlack() ? ChessPiece.W_KING : ChessPiece.B_KING).getPos()[1]};
        //right up
        for (int i = 1; i <= 7 - pos.getStartPos()[1]; i++) {
            pos.rightUp(true);
            if (move(board, pos, positions, kingPosition)) break;
        }
        pos.reset();
        //right down
        for (int i = 1; i <= 7 - pos.getStartPos()[1]; i++) {
            pos.rightDown(true);
            if (move(board, pos, positions, kingPosition)) break;
        }
        pos.reset();
        //left up
        for (int i = 1; i <= pos.getStartPos()[1]; i++) {
            pos.leftUp(true);
            if (move(board, pos, positions, kingPosition)) break;
        }
        pos.reset();
        //left down
        for (int i = 1; i <= pos.getStartPos()[1]; i++) {
            pos.leftDown(true);
            if (move(board, pos, positions, kingPosition)) break;
        }
        pos.reset();
    }

    private static boolean move(ChessBoard board, ChessPosition pos, List<ImmutablePair<int[], int[]>> positions, int[] kingPosition) {
        if (board.getPosAt(pos) == null && !Arrays.equals(kingPosition, pos.getPos())) {
            if (pos.checkmateAvoidancePos.isEmpty() || pos.checkmateAvoidancePos.contains(pos.getPos()))
                positions.add(new ImmutablePair<>(pos.getPos(), null));
        } else {
            ChessPosition boardPos = board.getPosAt(pos);
            if (board.canBeCaptured(pos.getPiece(), boardPos) && (boardPos.getPiece().isKing() && Arrays.equals(kingPosition, boardPos.getStartPos()))) {
                if (pos.checkmateAvoidancePos.isEmpty() || pos.checkmateAvoidancePos.contains(pos.getPos()))
                    positions.add(new ImmutablePair<>(pos.getPos(), new int[]{boardPos.getPos()[0], boardPos.getPos()[1],
                            boardPos.getPiece().isKing() ? 1 : 0}));
            } else if (Arrays.equals(kingPosition, pos.getPos())) {
                if (pos.checkmateAvoidancePos.isEmpty() || pos.checkmateAvoidancePos.contains(pos.getPos()))
                    positions.add(new ImmutablePair<>(pos.getPos(), new int[]{pos.getPos()[0], pos.getPos()[1], 1}));
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
