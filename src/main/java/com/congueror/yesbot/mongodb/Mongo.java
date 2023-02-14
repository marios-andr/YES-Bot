package com.congueror.yesbot.mongodb;

import com.congueror.yesbot.Constants;
import com.congueror.yesbot.MessageScheduler;
import com.congueror.yesbot.command.chess.ChessBoardType;
import com.congueror.yesbot.command.chess.ChessPieceType;
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

import javax.annotation.Nullable;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

public final class Mongo {

    private static MongoCollection<Document> users;
    private static MongoCollection<Document> guilds;

    public static void initialize() {
        ConnectionString connectionString = new ConnectionString("mongodb+srv://yesbot:" + Constants.getEnv("MONGO_PASSWORD") + "@yesbot.hd25z.mongodb.net/?retryWrites=true&w=majority");
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
                .append("ownedBoards", new HashSet<>(List.of("DEFAULT")))
                .append("selectedBoard", "DEFAULT")
                .append("ownedPieces", new HashSet<>(List.of("DEFAULT")))
                .append("selectedPiece", "DEFAULT");
    }

    public static Document getUserDocument(String snowflake) {
        Document doc = users.find(Filters.eq("id", snowflake)).first();
        if (doc == null) {
            doc = createUser(snowflake);
            users.insertOne(doc);
        }
        return doc;
    }

    public static <Item> Item getUser(String snowflake, String field) {
        Document doc = getUserDocument(snowflake);
        return (Item) doc.get(field);
    }

    public static <Item> void putUser(String snowflake, String field, Item item) {
        Document doc = getUserDocument(snowflake);
        doc.put(field, item);
        users.replaceOne(Filters.eq("id", snowflake), doc);
    }

    public static ChessBoardType getSelectedBoard(String snowflake) {
        return ChessBoardType.valueOf(getUser(snowflake, "selectedBoard").toString().toUpperCase());
    }

    public static ChessPieceType getSelectedPiece(String snowflake) {
        return ChessPieceType.valueOf(getUser(snowflake, "selectedPiece").toString().toUpperCase());
    }

    public static void addChessWin(String snowflake) {
        int wins = getUser(snowflake, "chessWins");
        putUser(snowflake, "chessWins", ++wins);
        int points = getUser(snowflake, "points");
        putUser(snowflake, "points", points + 100);
    }

    public static void addChessLoss(String snowflake) {
        int losses = getUser(snowflake, "chessLosses");
        putUser(snowflake, "chessLosses", ++losses);
        int points = getUser(snowflake, "points");
        putUser(snowflake, "points", points);
    }

    public static void addChessTie(String snowflake) {
        int ties = getUser(snowflake, "chessTies");
        putUser(snowflake, "chessTies", ++ties);
        int points = getUser(snowflake, "points");
        putUser(snowflake, "points", points + 10);
    }

    public static void addBoard(String snowflake, String board) {
        HashSet<String> ownedBoards = getUser(snowflake, "ownedBoards");
        try {
            ChessBoardType.valueOf(board.toUpperCase());
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return;
        }
        ownedBoards.add(board.toUpperCase());
        putUser(snowflake, "ownedBoards", ownedBoards);
    }

    public static void changeBoard(String snowflake, String board) {
        try {
            ChessBoardType.valueOf(board.toUpperCase());
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return;
        }
        putUser(snowflake, "selectedBoard", board.toUpperCase());
    }

    public static void addPiece(String snowflake, String piece) {
        HashSet<String> ownedPieces = getUser(snowflake, "ownedPieces");
        try {
            ChessPieceType.valueOf(piece.toUpperCase());
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return;
        }
        ownedPieces.add(piece.toUpperCase());
        putUser(snowflake, "ownedPieces", ownedPieces);
    }

    public static void changePieces(String snowflake, String piece) {
        try {
            ChessPieceType.valueOf(piece.toUpperCase());
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return;
        }
        putUser(snowflake, "selectedBoard", piece.toUpperCase());
    }

    private static Document createGuild(String snowflake) {
        return new Document("id", snowflake)
                .append("promotions_channel", 0)
                .append("last_sent", null)
                .append("last_promotions", null);
    }

    public static boolean hasGuildDocument(String snowflake) {
        return guilds.find(Filters.eq("id", snowflake)).first() != null;
    }

    public static Document getGuildDocument(String snowflake) {
        Document doc = guilds.find(Filters.eq("id", snowflake)).first();
        if (doc == null) {
            doc = createGuild(snowflake);
            guilds.insertOne(doc);
        }
        return doc;
    }

    public static <Item> Item getGuild(String snowflake, String field) {
        Document doc = getGuildDocument(snowflake);
        return (Item) doc.get(field);
    }

    public static <Item> void putGuild(String snowflake, String field, Item item) {
        Document doc = getGuildDocument(snowflake);
        doc.put(field, item);
        guilds.replaceOne(Filters.eq("id", snowflake), doc);
    }

    public static long getPromotionsChannel(String snowflake) {
        return getGuild(snowflake, "promotions_channel");
    }

    public static void setPromotionsChannel(String snowflake, long id) {
        putGuild(snowflake, "promotions_channel", id);
    }

    @Nullable
    public static Date getLastSent(String snowflake) {
        return getGuild(snowflake, "last_sent");
    }

    public static void setLastSent(String snowflake, Date date) {
        putGuild(snowflake, "last_sent", date);
    }

    @Nullable
    public static List<MessageScheduler.EpicStorePromotion> getLastPromotions(String snowflake) {
        return getGuild(snowflake, "last_promotions");
    }

    public static void setLastPromotions(String snowflake, List<MessageScheduler.EpicStorePromotion> promos) {
        putGuild(snowflake, "last_promotions", promos);
    }

    private Mongo() {
    }
}
