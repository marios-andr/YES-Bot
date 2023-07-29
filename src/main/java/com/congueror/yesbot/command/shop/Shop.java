package com.congueror.yesbot.command.shop;

import java.io.File;

public class Shop {

    private final String userId;

    public Shop(String userId) {
        this.userId = userId;
    }

    public File drawShop() {

        return null;
    }

    public interface ShopEntry {
        String name();

        String idGroup();

        String[] arguments();

        File draw(String arguments);

        int price();
    }
}
