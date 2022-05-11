package com.congueror.yesbot.command.chess;

import java.awt.*;

public enum ChessBoardType {

    DEFAULT(0, new Color(125, 148, 93), new Color(238, 238, 213));


    final int points;
    final String tileLocation;
    final Color firstColor;
    final Color secondColor;

    ChessBoardType(int points, String tileLocation) {
        this.points = points;
        this.tileLocation = tileLocation;
        this.firstColor = null;
        this.secondColor = null;
    }

    ChessBoardType(int points, Color firstColor, Color secondColor) {
        this.points = points;
        this.tileLocation = null;
        this.firstColor = firstColor;
        this.secondColor = secondColor;
    }
}
