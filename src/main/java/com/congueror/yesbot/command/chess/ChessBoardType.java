package com.congueror.yesbot.command.chess;

import java.awt.*;

public enum ChessBoardType {

    DEFAULT(new Color(125, 148, 93), new Color(238, 238, 213));


    final String tileLocation;
    final Color firstColor;
    final Color secondColor;

    ChessBoardType(String tileLocation) {
        this.tileLocation = tileLocation;
        this.firstColor = null;
        this.secondColor = null;
    }

    ChessBoardType(Color firstColor, Color secondColor) {
        this.tileLocation = null;
        this.firstColor = firstColor;
        this.secondColor = secondColor;
    }
}
