package com.congueror.yesbot;

import com.congueror.yesbot.command.chess.ChessBoardType;
import com.congueror.yesbot.command.chess.ChessPieceType;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.Document;

import java.util.HashSet;
import java.util.List;

public final class MongoUser {

    private static MongoCollection<Document> collection;

    static void initialize() {
        MongoClient mongo = new MongoClient(new MongoClientURI("mongodb+srv://yesbot:" + Config.get("MONGO_PASSWORD") + "@yesbot.hd25z.mongodb.net/admin"));
        MongoDatabase database = mongo.getDatabase("users");
        collection = database.getCollection("users");
    }

    private static Document create(String snowflake) {
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

    public static Document getDocument(String snowflake) {
        Document doc = collection.find(Filters.eq("id", snowflake)).first();
        if (doc == null) {
            doc = create(snowflake);
            collection.insertOne(doc);
        }
        return doc;
    }

    public static <Item> Item get(String snowflake, String field) {
        Document doc = getDocument(snowflake);
        return (Item) doc.get(field);
    }

    public static ChessBoardType getSelectedBoard(String snowflake) {
        return ChessBoardType.valueOf(get(snowflake, "selectedBoard").toString().toUpperCase());
    }

    public static ChessPieceType getSelectedPiece(String snowflake) {
        return ChessPieceType.valueOf(get(snowflake, "selectedPiece").toString().toUpperCase());
    }

    public static <Item> void put(String snowflake, String field, Item item) {
        Document doc = getDocument(snowflake);
        doc.put(field, item);
        collection.replaceOne(Filters.eq("id", snowflake), doc);
    }

    public static void addChessWin(String snowflake) {
        int wins = get(snowflake, "chessWins");
        put(snowflake, "chessWins", ++wins);
        int points = get(snowflake, "points");
        put(snowflake, "points", points + 100);
    }

    public static void addChessLoss(String snowflake) {
        int losses = get(snowflake, "chessLosses");
        put(snowflake, "chessLosses", ++losses);
        int points = get(snowflake, "points");
        put(snowflake, "points", points);
    }

    public static void addChessTie(String snowflake) {
        int ties = get(snowflake, "chessTies");
        put(snowflake, "chessTies", ++ties);
        int points = get(snowflake, "points");
        put(snowflake, "points", points + 10);
    }

    public static void addBoard(String snowflake, String board) {
        HashSet<String> ownedBoards = get(snowflake, "ownedBoards");
        try {
            ChessBoardType.valueOf(board.toUpperCase());
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return;
        }
        ownedBoards.add(board.toUpperCase());
        put(snowflake, "ownedBoards", ownedBoards);
    }

    public static void changeBoard(String snowflake, String board) {
        try {
            ChessBoardType.valueOf(board.toUpperCase());
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return;
        }
        put(snowflake, "selectedBoard", board.toUpperCase());
    }

    public static void addPiece(String snowflake, String piece) {
        HashSet<String> ownedPieces = get(snowflake, "ownedPieces");
        try {
            ChessPieceType.valueOf(piece.toUpperCase());
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return;
        }
        ownedPieces.add(piece.toUpperCase());
        put(snowflake, "ownedPieces", ownedPieces);
    }

    public static void changePieces(String snowflake, String piece) {
        try {
            ChessPieceType.valueOf(piece.toUpperCase());
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return;
        }
        put(snowflake, "selectedBoard", piece.toUpperCase());
    }

    private MongoUser() {
    }
}
