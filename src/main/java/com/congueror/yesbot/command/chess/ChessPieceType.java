package com.congueror.yesbot.command.chess;

public enum ChessPieceType {

    DEFAULT(0, "chess/pieces.png");

    final int points;
    final String location;

    ChessPieceType(int points, String location) {
        this.points = points;
        this.location = location;
    }
}
