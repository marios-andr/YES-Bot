package io.github.marios_andr.yesbot.database;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.marios_andr.yesbot.Constants;
import io.github.marios_andr.yesbot.command.announcements.Announcement;
import io.github.marios_andr.yesbot.command.chess.ChessBoardDecor;
import io.github.marios_andr.yesbot.command.chess.ChessPieceDecor;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.ServerApi;
import com.mongodb.ServerApiVersion;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import org.bson.Document;

import java.util.*;

public final class MongoDatabase implements Database {

    private final MongoCollection<Document> users;
    private final MongoCollection<Document> guilds;

    MongoDatabase() {
        ConnectionString connectionString = new ConnectionString(Constants.getSettings().mongo_link());
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .serverApi(ServerApi.builder()
                        .version(ServerApiVersion.V1)
                        .build())
                .build();
        MongoClient mongoClient = MongoClients.create(settings);
        com.mongodb.client.MongoDatabase database = mongoClient.getDatabase("users");

        users = database.getCollection("users");
        guilds = database.getCollection("guilds");
    }

    private static Document createUser(String snowflake) {
        return new Document("id", snowflake)
                .append("points", 0)
                .append("chessWins", 0)
                .append("chessLosses", 0)
                .append("chessTies", 0)
                .append("selectedBoard", "DEFAULT")
                .append("selectedPiece", "DEFAULT")
                .append("ownedItems", new HashSet<>(List.of("CP#0", "CB#0")));
    }

    Document getUserDocument(String snowflake) {
        Document doc = users.find(Filters.eq("id", snowflake)).first();
        if (doc == null) {
            doc = createUser(snowflake);
            users.insertOne(doc);
        }
        return doc;
    }

    <Item> Item getUser(String snowflake, String field) {
        Document doc = getUserDocument(snowflake);
        return (Item) doc.get(field);
    }

    <Item> void putUser(String snowflake, String field, Item item) {
        Document doc = getUserDocument(snowflake);
        doc.put(field, item);
        users.replaceOne(Filters.eq("id", snowflake), doc);
    }

    @Override
    public JsonObject getUserJson(String snowflake) {
        return JsonParser.parseString(this.getUserDocument(snowflake).toJson()).getAsJsonObject();
    }

    @Override
    public ChessBoardDecor getSelectedBoard(String snowflake) {
        return ChessBoardDecor.valueOf(getUser(snowflake, "selectedBoard").toString().toUpperCase());
    }

    public void setSelectedBoard(String snowflake, String board) {
        try {
            ChessBoardDecor.valueOf(board.toUpperCase());
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return;
        }
        putUser(snowflake, "selectedBoard", board.toUpperCase());
    }

    @Override
    public ChessPieceDecor getSelectedPiece(String snowflake) {
        return ChessPieceDecor.valueOf(getUser(snowflake, "selectedPiece").toString().toUpperCase());
    }

    public void setSelectedPiece(String snowflake, String piece) {
        try {
            ChessPieceDecor.valueOf(piece.toUpperCase());
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return;
        }
        putUser(snowflake, "selectedBoard", piece.toUpperCase());
    }

    @Override
    public void addChessWin(String snowflake) {
        int wins = getUser(snowflake, "chessWins");
        putUser(snowflake, "chessWins", ++wins);
        int points = getUser(snowflake, "points");
        putUser(snowflake, "points", points + 100);
    }

    @Override
    public void addChessLoss(String snowflake) {
        int losses = getUser(snowflake, "chessLosses");
        putUser(snowflake, "chessLosses", ++losses);
        int points = getUser(snowflake, "points");
        putUser(snowflake, "points", points);
    }

    @Override
    public void addChessTie(String snowflake) {
        int ties = getUser(snowflake, "chessTies");
        putUser(snowflake, "chessTies", ++ties);
        int points = getUser(snowflake, "points");
        putUser(snowflake, "points", points + 10);
    }

    public HashSet<String> getOwnedItems(String snowflake) {
        return getUser(snowflake, "ownedItems");
    }

    public void addOwnedItem(String snowflake, String code) {
        HashSet<String> items = getUser(snowflake, "ownedItems");
        items.add(code);
        putUser(snowflake, "ownedItems", items);
    }

    public static Document createGuild(String snowflake) {
        return new Document("id", snowflake)
                .append("promotionsChannel", 0)
                .append("lastPromotions", null);
    }

    public boolean hasGuildDocument(String snowflake) {
        return guilds.find(Filters.eq("id", snowflake)).first() != null;
    }

    public Document getGuildDocument(String snowflake) {
        Document doc = guilds.find(Filters.eq("id", snowflake)).first();
        if (doc == null) {
            doc = createGuild(snowflake);
            guilds.insertOne(doc);
        }
        return doc;
    }

    public <Item> Item getGuild(String snowflake, String field) {
        Document doc = getGuildDocument(snowflake);
        return (Item) doc.get(field);
    }

    public <Item> Item getGuildOrDefault(String snowflake, String field, Item default_) {
        Item i = getGuild(snowflake, field);
        if (i == null) {
            putGuild(snowflake, field, default_);
            return default_;
        } else
            return i;
    }

    public <Item> void putGuild(String snowflake, String field, Item item) {
        Document doc = getGuildDocument(snowflake);
        doc.put(field, item);
        guilds.replaceOne(Filters.eq("id", snowflake), doc);
    }

    @Override
    public boolean hasPromotions(String snowflake) {
        return this.getPromotionsChannels(snowflake) != null;
    }

    @Override
    public Map<String, String> getPromotionsChannels(String snowflake) {
        return getGuildOrDefault(snowflake, "announcements", new HashMap<>());
    }

    @Override
    public void addPromotionsChannel(String snowflake, String type, String channel) {
        var a = getPromotionsChannels(snowflake);
        a.put(type, channel);
        putGuild(snowflake, "announcements", a);
    }

    @Override
    public List<Announcement> getLastPromotions(String snowflake) {
        ArrayList<Document> a = getGuildOrDefault(snowflake, "last_announcements", new ArrayList<>());
        return a.stream().map(MongoDatabase::createAnnouncement).toList();
    }

    @Override
    public void setLastPromotions(String snowflake, List<Announcement> announcements) {
        var a = announcements.stream().map(announcement -> {
            Document d = new Document("class", announcement.getClass().getName());
            d.put("announcement", announcement);
            return d;
        }).toList();
        putGuild(snowflake, "last_announcements", a);
    }

    public static Announcement createAnnouncement(Document doc) {
        try {
            Class<?> clazz = Class.forName(doc.getString("class"));
            Document ann = doc.get("announcement", Document.class);

            if (!Arrays.stream(clazz.getInterfaces()).anyMatch(aClass -> aClass.equals(Announcement.class)))
                return null;

            List<Object> fields = new ArrayList<>();
            Arrays.stream(clazz.getDeclaredFields()).forEach(field -> {
                fields.add(ann.get(field.getName(), field.getType()));
            });
            return (Announcement) clazz.getDeclaredConstructors()[0].newInstance(fields.toArray());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
