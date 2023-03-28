package com.congueror.yesbot.command.chess;

public enum ChessPieceDecor {

    DEFAULT(0, "chess/pieces.png");

    final int points;
    final String location;

    ChessPieceDecor(int points, String location) {
        this.points = points;
        this.location = location;
    }
}
