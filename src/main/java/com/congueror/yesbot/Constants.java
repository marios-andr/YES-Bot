package com.congueror.yesbot;

import com.congueror.yesbot.command.AbstractCommand;
import com.congueror.yesbot.command.shop.Shop;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.cdimascio.dotenv.Dotenv;

import javax.annotation.Nullable;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class Constants {

    private static final Dotenv dotenv = Dotenv.configure().directory("/.env").load();

    public static String getEnv(String key) {
        return dotenv.get(key.toUpperCase());
    }

    public static final String PREFIX = "!";
    public static final String SNOWFLAKE_ID = getEnv("SNOWFLAKE");
    public static boolean LOCKED = false;
    public static boolean STOP = false;
    public static final ArrayList<AbstractCommand> COMMANDS = new ArrayList<>();
    public static final ArrayList<Shop.ShopEntry> SHOP_ENTRIES = new ArrayList<>();

    private Constants() {}

    public static JsonObject getJson(String url) {
        try (InputStream input = new URL(url).openStream()) {
            InputStreamReader reader = new InputStreamReader(input, StandardCharsets.UTF_8);
            var json = JsonParser.parseReader(reader);
            return json.getAsJsonObject();
        } catch (Exception e) {
            return null;
        }
    }

    @Nullable
    public static String optionalString(JsonElement element) {
        return element == null ? null : element.getAsString();
    }
}
