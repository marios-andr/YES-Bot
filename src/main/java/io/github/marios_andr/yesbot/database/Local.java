package io.github.marios_andr.yesbot.database;

import io.github.marios_andr.yesbot.Constants;
import io.github.marios_andr.yesbot.command.announcements.Announcement;
import io.github.marios_andr.yesbot.command.chess.ChessBoardDecor;
import io.github.marios_andr.yesbot.command.chess.ChessPieceDecor;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonWriter;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static io.github.marios_andr.yesbot.Constants.GSON;

public class Local {

    private static final File USER_FILE = new File("./user_database.json");
    private static final File GUILD_FILE = new File("./guild_database.json");
    private static JsonArray users = new JsonArray();
    private static JsonArray guilds = new JsonArray();

    static void initialize() {

        if (!USER_FILE.exists()) {
            saveTo(users, USER_FILE);
            Constants.LOG.info("Local database file user_database.json was missing, so a new one was created.");
        } else {
            users = readFile(USER_FILE);
        }

        if (!GUILD_FILE.exists()) {
            saveTo(guilds, GUILD_FILE);
            Constants.LOG.info("Local database file guild_database.json was missing, so a new one was created.");
        } else {
            guilds = readFile(GUILD_FILE);
        }
    }

    private static JsonArray readFile(File file) {
        JsonArray obj = null;
        try (FileReader fr = new FileReader(file)) {
            obj = (JsonArray) JsonParser.parseReader(fr);
        } catch (Exception e) {
            Constants.LOG.error(e.getMessage());
        }
        return obj;
    }

    private static void saveTo(JsonElement json, File file) {
        try (FileWriter wr = new FileWriter(file)) {
            JsonWriter jsonWriter = GSON.newJsonWriter(Streams.writerForAppendable(wr));
            jsonWriter.setIndent("   ");
            GSON.toJson(json, jsonWriter);
        } catch (Exception e) {
            Constants.LOG.error("Something went wrong while writing to " + file.getName() + ".", e);
        }
    }

    static JsonObject getUserJson(String snowflake) {
        JsonObject user = null;

        for (JsonElement json : users) {
            if (json.getAsJsonObject().get("id").getAsString().equals(snowflake)) {
                user = json.getAsJsonObject();
                break;
            }
        }

        if (user == null) {
            user = new JsonObject();
            user.addProperty("id", snowflake);
            user.addProperty("points", 0);
            user.addProperty("chessWins", 0);
            user.addProperty("chessLosses", 0);
            user.addProperty("chessTies", 0);
            user.addProperty("selectedBoard", "DEFAULT");
            user.addProperty("selectedPiece", "DEFAULT");

            users.add(user);
            saveTo(users, USER_FILE);
            //JsonArray items = new JsonArray();
            //user.ownedItems.forEach(items::add);
            //obj.addProperty("ownedItems", items);
        }

        return user;
    }

    static DatabaseHandler.User getUser(String snowflake) {
        return GSON.fromJson(getUserJson(snowflake), DatabaseHandler.User.class);
    }

    static ChessBoardDecor getSelectedBoard(String snowflake) {
        return ChessBoardDecor.valueOf(getUser(snowflake).selectedBoard.toUpperCase());
    }

    static ChessPieceDecor getSelectedPiece(String snowflake) {
        return ChessPieceDecor.valueOf(getUser(snowflake).selectedPiece.toUpperCase());
    }

    static void addChessWin(String snowflake) {
        int wins = getUser(snowflake).chessWins;
        getUserJson(snowflake).addProperty("chessWins", ++wins);
        int points = getUser(snowflake).points;
        getUserJson(snowflake).addProperty("points", points + 100);
        saveTo(users, USER_FILE);
    }

    static void addChessLoss(String snowflake) {
        int losses = getUser(snowflake).chessLosses;
        getUserJson(snowflake).addProperty("chessLosses", ++losses);
        saveTo(users, USER_FILE);
    }

    static void addChessTie(String snowflake) {
        int ties = getUser(snowflake).chessTies;
        getUserJson(snowflake).addProperty("chessTies", ++ties);
        int points = getUser(snowflake).points;
        getUserJson(snowflake).addProperty("points", points + 10);
        saveTo(users, USER_FILE);
    }

    static JsonObject getGuildJson(String snowflake) {
        JsonObject guild = null;

        for (JsonElement json : guilds) {
            if (json.getAsJsonObject().get("id").getAsString().equals(snowflake)) {
                guild = json.getAsJsonObject();
                break;
            }
        }

        if (guild == null) {
            guild = new JsonObject();
            guild.addProperty("id", snowflake);
            guild.add("promotionsChannels", new JsonObject());
            guild.add("lastPromotions", new JsonArray());

            guilds.add(guild);
            saveTo(guilds, GUILD_FILE);
        }

        return guild;
    }

    static DatabaseHandler.Guild getGuild(String snowflake) {
        JsonObject guildJson = getGuildJson(snowflake);
        DatabaseHandler.Guild guild = new DatabaseHandler.Guild(snowflake);
        for (String entry : guildJson.keySet()) {
            if (entry.equals("id"))
                continue;

            if (entry.equals("lastPromotions")) {
                for (JsonElement promo : guildJson.getAsJsonArray(entry)) {
                    if (promo.isJsonObject()) {
                        guild.lastPromotions.add(createPromotion(promo.getAsJsonObject()));
                    }
                }
                continue;
            }

            try {
                Field field = DatabaseHandler.Guild.class.getDeclaredField(entry);
                field.set(guild, GSON.fromJson(guildJson.get(entry), field.getType()));
            } catch (Exception e) {
                Constants.LOG.error("Something went wrong while getting guild entry: ", e);
            }
        }


        return guild;
    }

    static Announcement createPromotion(JsonObject obj) {
        try {
            Class<?> clazz = Class.forName(obj.get("class").getAsString());
            JsonObject promo = obj.getAsJsonObject("promotion");

            if (Arrays.stream(clazz.getInterfaces()).noneMatch(aClass -> aClass.equals(Announcement.class)))
                return null;

            List<Object> fields = new ArrayList<>();
            Arrays.stream(clazz.getDeclaredFields()).forEach(field -> {
                fields.add(GSON.fromJson(promo.get(field.getName()), field.getType()));
            });
            return (Announcement) clazz.getDeclaredConstructors()[0].newInstance(fields.toArray());
        } catch (Exception e) {
            throw new RuntimeException("Something went wrong while creating local promotion", e);
        }
    }

    static void setLastPromotions(String snowflake, List<Announcement> announcements) {
        JsonObject guildJson = getGuildJson(snowflake);
        guildJson.remove("lastPromotions");

        JsonArray promotions = new JsonArray();
        for (Announcement ann : announcements) {
            JsonObject capsuleObj = new JsonObject();
            capsuleObj.addProperty("class", ann.getClass().getName());
            capsuleObj.add("promotion", JsonParser.parseString(GSON.toJson(ann)));
            promotions.add(capsuleObj);
        }
        guildJson.add("lastPromotions", promotions);

        saveTo(guilds, GUILD_FILE);
    }

    static void addPromotionsChannel(String snowflake, String type, String id) {
        JsonObject obj = getGuildJson(snowflake);
        JsonObject channels = obj.getAsJsonObject("promotionsChannels");
        channels.addProperty(type, id);
        saveTo(guilds, GUILD_FILE);
    }
}
