package io.github.marios_andr.yesbot.database;

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
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.Document;

import java.util.*;

public final class Mongo {

    private static MongoCollection<Document> users;
    private static MongoCollection<Document> guilds;

    public static void initialize() {
        ConnectionString connectionString = new ConnectionString(Constants.getSettings().mongo_link());
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .serverApi(ServerApi.builder()
                        .version(ServerApiVersion.V1)
                        .build())
                .build();
        MongoClient mongoClient = MongoClients.create(settings);
        MongoDatabase database = mongoClient.getDatabase("users");

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

    static Document getUserDocument(String snowflake) {
        Document doc = users.find(Filters.eq("id", snowflake)).first();
        if (doc == null) {
            doc = createUser(snowflake);
            users.insertOne(doc);
        }
        return doc;
    }

    private static <Item> Item getUser(String snowflake, String field) {
        Document doc = getUserDocument(snowflake);
        return (Item) doc.get(field);
    }

    private static <Item> void putUser(String snowflake, String field, Item item) {
        Document doc = getUserDocument(snowflake);
        doc.put(field, item);
        users.replaceOne(Filters.eq("id", snowflake), doc);
    }

    static ChessBoardDecor getSelectedBoard(String snowflake) {
        return ChessBoardDecor.valueOf(getUser(snowflake, "selectedBoard").toString().toUpperCase());
    }

    static void setSelectedBoard(String snowflake, String board) {
        try {
            ChessBoardDecor.valueOf(board.toUpperCase());
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return;
        }
        putUser(snowflake, "selectedBoard", board.toUpperCase());
    }

    static ChessPieceDecor getSelectedPiece(String snowflake) {
        return ChessPieceDecor.valueOf(getUser(snowflake, "selectedPiece").toString().toUpperCase());
    }

    static void setSelectedPiece(String snowflake, String piece) {
        try {
            ChessPieceDecor.valueOf(piece.toUpperCase());
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return;
        }
        putUser(snowflake, "selectedBoard", piece.toUpperCase());
    }

    static void addChessWin(String snowflake) {
        int wins = getUser(snowflake, "chessWins");
        putUser(snowflake, "chessWins", ++wins);
        int points = getUser(snowflake, "points");
        putUser(snowflake, "points", points + 100);
    }

    static void addChessLoss(String snowflake) {
        int losses = getUser(snowflake, "chessLosses");
        putUser(snowflake, "chessLosses", ++losses);
        int points = getUser(snowflake, "points");
        putUser(snowflake, "points", points);
    }

    static void addChessTie(String snowflake) {
        int ties = getUser(snowflake, "chessTies");
        putUser(snowflake, "chessTies", ++ties);
        int points = getUser(snowflake, "points");
        putUser(snowflake, "points", points + 10);
    }

    static HashSet<String> getOwnedItems(String snowflake) {
        return getUser(snowflake, "ownedItems");
    }

    static void addOwnedItem(String snowflake, String code) {
        HashSet<String> items = getUser(snowflake, "ownedItems");
        items.add(code);
        putUser(snowflake, "ownedItems", items);
    }

    private static Document createGuild(String snowflake) {
        return new Document("id", snowflake)
                .append("promotionsChannel", 0)
                .append("lastPromotions", null);
    }

    static boolean hasGuildDocument(String snowflake) {
        return guilds.find(Filters.eq("id", snowflake)).first() != null;
    }

    static Document getGuildDocument(String snowflake) {
        Document doc = guilds.find(Filters.eq("id", snowflake)).first();
        if (doc == null) {
            doc = createGuild(snowflake);
            guilds.insertOne(doc);
        }
        return doc;
    }

    static <Item> Item getGuild(String snowflake, String field) {
        Document doc = getGuildDocument(snowflake);
        return (Item) doc.get(field);
    }

    static <Item> Item getGuildOrDefault(String snowflake, String field, Item default_) {
        Item i = getGuild(snowflake, field);
        if (i == null) {
            putGuild(snowflake, field, default_);
            return default_;
        } else
            return i;
    }

    static <Item> void putGuild(String snowflake, String field, Item item) {
        Document doc = getGuildDocument(snowflake);
        doc.put(field, item);
        guilds.replaceOne(Filters.eq("id", snowflake), doc);
    }

    static long getPromotionsChannel(String snowflake) {
        return getGuildOrDefault(snowflake, "promotionsChannel", 0);
    }

    static Map<String, String> getAnnouncements(String snowflake) {
        return getGuildOrDefault(snowflake, "announcements", new HashMap<>());
    }

    static void addAnnouncements(String snowflake, String type, String channel) {
        var a = getAnnouncements(snowflake);
        a.put(type, channel);
        putGuild(snowflake, "announcements", a);
    }

    static List<Announcement> getLastAnnouncements(String snowflake) {
        ArrayList<Document> a = getGuildOrDefault(snowflake, "last_announcements", new ArrayList<>());
        return a.stream().map(Mongo::createAnnouncement).toList();
    }

    static Announcement createAnnouncement(Document doc) {
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

    static void setLastAnnouncements(String snowflake, List<Announcement> announcements) {
        var a = announcements.stream().map(announcement -> {
            Document d = new Document("class", announcement.getClass().getName());
            d.put("announcement", announcement);
            return d;
        }).toList();
        putGuild(snowflake, "last_announcements", a);
    }

    private Mongo() {
    }
}
