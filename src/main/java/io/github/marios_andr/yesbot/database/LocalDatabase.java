package io.github.marios_andr.yesbot.database;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonWriter;
import io.github.marios_andr.yesbot.Constants;
import io.github.marios_andr.yesbot.command.announcements.Announcement;
import io.github.marios_andr.yesbot.command.chess.ChessBoardDecor;
import io.github.marios_andr.yesbot.command.chess.ChessPieceDecor;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static io.github.marios_andr.yesbot.Constants.GSON;

public class LocalDatabase implements Database {

    private static final File DB_FILE = new File("./database.json");

    private final JsonObject db;
    private final JsonArray users;
    private final JsonArray guilds;

    LocalDatabase() {
        JsonElement o = readFile(DB_FILE);

        if (!DB_FILE.exists() || !o.isJsonObject()) {
            this.db = new JsonObject();
            this.users = new JsonArray();
            this.guilds = new JsonArray();
            this.db.add("users", this.users);
            this.db.add("guilds", this.guilds);
        } else {
            this.db = o.getAsJsonObject();

            if (this.db.has("users") && this.db.get("users").isJsonArray()) {
                this.users = this.db.get("users").getAsJsonArray();
            } else {
                this.users = new JsonArray();
                this.db.add("users", this.users);
            }

            if (this.db.has("guilds") && this.db.get("guilds").isJsonArray()) {
                this.guilds = this.db.get("guilds").getAsJsonArray();
            } else {
                this.guilds = new JsonArray();
                this.db.add("guilds", this.guilds);
            }
        }

        save();
    }

    private static JsonElement readFile(File file) {
        JsonElement obj = null;
        try (FileReader fr = new FileReader(file)) {
            obj = JsonParser.parseReader(fr);
        } catch (Exception e) {
            //File not found so it will be created.
            //Constants.LOG.error("Something went wrong while trying to read file: {}", file.getName(), e);
        }
        return obj;
    }

    private void save() {
        try (FileWriter wr = new FileWriter(DB_FILE)) {
            JsonWriter jsonWriter = GSON.newJsonWriter(Streams.writerForAppendable(wr));
            jsonWriter.setIndent("   ");
            GSON.toJson(this.db, jsonWriter);
        } catch (Exception e) {
            Constants.LOG.error("Something went wrong while writing to {}.", DB_FILE.getName(), e);
        }
    }

    private JsonObject defaultUser(String snowflake) {
        JsonObject user = new JsonObject();
        user.addProperty("id", snowflake);
        user.addProperty("points", 0);
        user.addProperty("chessWins", 0);
        user.addProperty("chessLosses", 0);
        user.addProperty("chessTies", 0);
        user.addProperty("selectedBoard", "DEFAULT");
        user.addProperty("selectedPiece", "DEFAULT");
        return user;
    }

    public JsonObject getUserJson(String snowflake) {
        JsonObject user = null;

        for (JsonElement json : users) {
            if (json.getAsJsonObject().get("id").getAsString().equals(snowflake)) {
                user = json.getAsJsonObject();
                break;
            }
        }

        if (user == null) {
            user = defaultUser(snowflake);
            users.add(user);
            save();
            //JsonArray items = new JsonArray();
            //user.ownedItems.forEach(items::add);
            //obj.addProperty("ownedItems", items);
        }

        return user;
    }

    public DatabaseHandler.User getUser(String snowflake) {
        return GSON.fromJson(getUserJson(snowflake), DatabaseHandler.User.class);
    }

    public ChessBoardDecor getSelectedBoard(String snowflake) {
        return ChessBoardDecor.valueOf(getUser(snowflake).selectedBoard.toUpperCase());
    }

    public ChessPieceDecor getSelectedPiece(String snowflake) {
        return ChessPieceDecor.valueOf(getUser(snowflake).selectedPiece.toUpperCase());
    }

    public void addChessWin(String snowflake) {
        int wins = getUser(snowflake).chessWins;
        getUserJson(snowflake).addProperty("chessWins", ++wins);
        int points = getUser(snowflake).points;
        getUserJson(snowflake).addProperty("points", points + 100);
        save();
    }

    public void addChessLoss(String snowflake) {
        int losses = getUser(snowflake).chessLosses;
        getUserJson(snowflake).addProperty("chessLosses", ++losses);
        save();
    }

    @Override
    public void addChessTie(String snowflake) {
        int ties = getUser(snowflake).chessTies;
        getUserJson(snowflake).addProperty("chessTies", ++ties);
        int points = getUser(snowflake).points;
        getUserJson(snowflake).addProperty("points", points + 10);
        save();
    }

    @Override
    public boolean hasPromotions(String snowflake) {
        return !this.getGuild(snowflake).promotionsChannels.isEmpty();
    }

    @Override
    public Map<String, String> getPromotionsChannels(String snowflake) {
        return this.getGuild(snowflake).promotionsChannels;
    }

    public void addPromotionsChannel(String snowflake, String type, String id) {
        JsonObject obj = getGuildJson(snowflake);
        JsonObject channels = obj.getAsJsonObject("promotionsChannels");
        channels.addProperty(type, id);
        save();
    }

    @Override
    public List<Announcement> getLastPromotions(String snowflake) {
        return List.of();
    }

    public void setLastPromotions(String snowflake, List<Announcement> announcements) {
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

        save();
    }

    private JsonObject defaultGuild(String snowflake) {
        JsonObject guild = new JsonObject();
        guild.addProperty("id", snowflake);
        guild.add("promotionsChannels", new JsonObject());
        guild.add("lastPromotions", new JsonArray());
        return guild;
    }

    public JsonObject getGuildJson(String snowflake) {
        JsonObject guild = null;

        for (JsonElement json : guilds) {
            if (json.getAsJsonObject().get("id").getAsString().equals(snowflake)) {
                guild = json.getAsJsonObject();
                break;
            }
        }

        if (guild == null) {
            guild = defaultGuild(snowflake);
            guilds.add(guild);
            save();
        }

        return guild;
    }

    public DatabaseHandler.Guild getGuild(String snowflake) {
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

    public Announcement createPromotion(JsonObject obj) {
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
}
