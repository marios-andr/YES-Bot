package com.congueror.yesbot.command.shop;

import java.io.File;
import java.util.ArrayList;

public class Shop {

    private final String userId;
    private final ArrayList<ShopEntry> shopEntries = new ArrayList<>();

    public Shop(String userId) {
        this.userId = userId;
    }

    public File drawShop() {
        return null;
    }

    public interface ShopEntry {
        String name();

        String[] arguments();

        File draw(String arguments);

        int price();
    }
}
