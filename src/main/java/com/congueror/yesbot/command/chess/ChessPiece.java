package com.congueror.yesbot.command.chess;

public enum ChessPiece {
    W_PAWN(0, 0, 146, 146),
    W_ROOK(146, 0, 292, 146),
    W_KNIGHT(292, 0, 438, 146),
    W_BISHOP(438, 0, 584, 146),
    W_QUEEN(584, 0, 730, 146),
    W_KING(730, 0, 876, 146),

    B_PAWN(0, 146, 146, 292),
    B_ROOK(146, 146, 292, 292),
    B_KNIGHT(292, 146, 438, 292),
    B_BISHOP(438, 146, 584, 292),
    B_QUEEN(584, 146, 730, 292),
    B_KING(730, 146, 876, 292)
    ;

    public final int x1;
    public final int y1;
    public final int x2;
    public final int y2;

    ChessPiece(int x1, int y1, int x2, int y2) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
    }

    public boolean isSameSide(ChessPiece piece) {
        if (this.isWhite() && piece.isWhite())
            return true;
        return this.isBlack() && piece.isBlack();
    }

    public boolean isWhite() {
        return equals(W_PAWN) || equals(W_ROOK) || equals(W_KNIGHT) || equals(W_BISHOP) || equals(W_QUEEN) || equals(W_KING);
    }

    public boolean isBlack() {
        return equals(B_PAWN) || equals(B_ROOK) || equals(B_KNIGHT) || equals(B_BISHOP) || equals(B_QUEEN) || equals(B_KING);
    }

    public boolean isPawn() {
        return this.equals(W_PAWN) || this.equals(B_PAWN);
    }

    public boolean isQueen() {
        return this.equals(W_QUEEN) || this.equals(B_QUEEN);
    }

    public boolean isKing() {
        return this.equals(W_KING) || this.equals(B_KING);
    }
}
