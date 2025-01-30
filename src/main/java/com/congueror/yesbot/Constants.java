package com.congueror.yesbot;

import com.congueror.yesbot.command.AbstractCommand;
import com.congueror.yesbot.command.shop.Shop;
import com.congueror.yesbot.database.DatabaseHandler;
import com.congueror.yesbot.util.ConsolePrintStream;
import com.congueror.yesbot.util.HtmlDocument;
import com.congueror.yesbot.util.LogFile;
import com.google.gson.*;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonWriter;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public final class Constants {

    public static final org.slf4j.Logger LOG = LoggerFactory.getLogger(YESBot.class);
    public static final LogFile LOG_FILE = new LogFile();
    public static final Gson GSON = new Gson();
    private static Settings SETTINGS;

    public static final String PREFIX = "!";
    public static boolean LOCKED = false;
    public static boolean STOP = false;
    public static final ArrayList<AbstractCommand> COMMANDS = new ArrayList<>();
    public static final Map<String, Shop.ShopEntry> SHOP_ENTRIES = new HashMap<>();

    private Constants() {
    }

    public static Settings getSettings() {
        return SETTINGS;
    }

    public static void init() {
        System.setErr(new ConsolePrintStream(System.err));
        System.setOut(new ConsolePrintStream(System.out));

        Runtime.getRuntime().addShutdownHook(new Thread(() -> LOG.info("System was shutdown.")));

        File file = new File("./settings.json");
        if (!file.exists()) {
            JsonObject obj = new JsonObject();

            for (var f : Settings.class.getDeclaredFields()) {
                if (f.getType().equals(String.class)) {
                    obj.addProperty(f.getName(), "");
                }
            }

            JsonArray arr = new JsonArray();
            JsonObject admin = new JsonObject();
            admin.addProperty("name", "admin");
            admin.addProperty("password", "admin");
            arr.add(admin);
            obj.add("credentials", arr);

            try (FileWriter wr = new FileWriter(file)) {
                JsonWriter jsonWriter = GSON.newJsonWriter(Streams.writerForAppendable(wr));
                jsonWriter.setIndent("   ");
                GSON.toJson(obj, jsonWriter);
            } catch (Exception e) {
                LOG.error("Something went wrong while creating settings.json, Program will exit.", e);
                System.exit(-1);
            }

            LOG.error("Settings json was missing and was therefore created, fill out the necessary information and launch the application again.");
            System.exit(0);
        }

        try (FileReader fr = new FileReader(file)) {

            JsonObject obj = (JsonObject) JsonParser.parseReader(fr);
            SETTINGS = GSON.fromJson(obj, Settings.class);


            var name = getNullField(Settings.class, SETTINGS);
            if (!name.isEmpty())
                throw new JsonParseException("There was an error parsing settings.json, field \"" + name + "\" was either null or not specified.");
        } catch (Exception e) {
            Constants.LOG.error(e.getMessage());
            System.exit(-1);
        }
    }

    private static String getNullField(Class<?> clazz, Object type) throws IllegalAccessException {
        var fields = clazz.getDeclaredFields();
        for (var f : fields) {
            if (f.getType().isArray()) {
                var field = getNullField(f.getType().arrayType(), f.get(type));
                if (!field.isEmpty())
                    return field;
            }

            if (f.get(type) == null)
                return f.getName();
        }
        return "";
    }

    public static JsonObject getJson(String url) {
        try (InputStream input = new URL(url).openStream()) {
            InputStreamReader reader = new InputStreamReader(input, StandardCharsets.UTF_8);
            var json = JsonParser.parseReader(reader);
            return json.getAsJsonObject();
        } catch (Exception e) {
            LOG.error("Something went wrong while retrieving json " + url, e);
            return null;
        }
    }

    public static HtmlDocument getHtml(String url) {
        try (InputStream input = new URL(url).openStream()) {
            InputStreamReader reader = new InputStreamReader(input, StandardCharsets.UTF_8);
            return HtmlDocument.parse(reader);
        } catch (Exception e) {
            LOG.error("Something went wrong while retrieving html " + url, e);
            return null;
        }
    }

    @Nullable
    public static String optionalString(JsonElement element) {
        return element == null ? null : element.getAsString();
    }

    public static int tryGetInt(JsonElement e, int def, Function<JsonElement, Integer> get) {
        int num;
        try {
            num = get.apply(e);
        } catch (NullPointerException ex) {
            num = def;
        }
        return num;
    }

    static boolean checkCredentials(String username, String password) {
        for (var c : SETTINGS.credentials()) {
            if (c.name.equals(username) && c.password.equals(password))
                return true;
        }
        return false;
    }

    public record Settings(String token, String bot_snowflake, String owner_snowflake, String mongo_link,
                           String reddit_username, String reddit_password, String reddit_client, String reddit_secret,
                           String steam_token, Admin[] credentials) {}

    public record Admin(String name, String password) {}
}
