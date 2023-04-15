package com.congueror.yesbot.command.chess;

import com.congueror.yesbot.command.shop.Shop;

import java.io.File;

public enum ChessPieceDecor implements Shop.ShopEntry {

    DEFAULT(0, "chess/pieces.png");

    final int points;
    final String location;

    ChessPieceDecor(int points, String location) {
        this.points = points;
        this.location = location;
    }

    @Override
    public String idGroup() {
        return "CP#";
    }

    @Override
    public String[] arguments() {
        return new String[0];
    }

    @Override
    public File draw(String arguments) {
        return null;
    }

    @Override
    public int price() {
        return 0;
    }
}
