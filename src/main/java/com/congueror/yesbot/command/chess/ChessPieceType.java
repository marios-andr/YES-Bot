package com.congueror.yesbot.command.chess;

public enum ChessPieceType {

    DEFAULT("chess/pieces.png");

    final String location;

    ChessPieceType(String location) {
        this.location = location;
    }
}
