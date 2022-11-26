package com.congueror.yesbot.command.chess;

import com.congueror.yesbot.command.shop.Shop;

import java.awt.*;
import java.io.File;

public enum ChessBoardType implements Shop.ShopEntry {

    DEFAULT(0, new Color(125, 148, 93), new Color(238, 238, 213)),
    CUSTOM(1300, ""),
    ;


    final int price;
    final String tileLocation;
    final Color firstColor;
    final Color secondColor;

    ChessBoardType(int price, String tileLocation) {
        this.price = price;
        this.tileLocation = tileLocation;
        this.firstColor = null;
        this.secondColor = null;
    }

    ChessBoardType(int price, Color firstColor, Color secondColor) {
        this.price = price;
        this.tileLocation = null;
        this.firstColor = firstColor;
        this.secondColor = secondColor;
    }

    @Override
    public String[] arguments() {
        if (this == CUSTOM)
            return new String[]{"-firstColor", "-secondColor"};
        return new String[0];
    }

    @Override
    public File draw(String arguments) {
        if (this == CUSTOM) {
            if (arguments.contains(this.arguments()[0]) && arguments.contains(this.arguments()[1])) {
                int firstI = arguments.indexOf(this.arguments()[0] + "=");
                //String first = arguments.substring()
            } else {
                throw new RuntimeException("Missing required arguments");
            }
        }
        return null;
    }

    @Override
    public int price() {
        return this.price;
    }
}
