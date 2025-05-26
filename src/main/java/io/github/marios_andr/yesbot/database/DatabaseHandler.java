package io.github.marios_andr.yesbot.database;

import io.github.marios_andr.yesbot.Constants;
import io.github.marios_andr.yesbot.command.announcements.Announcement;
import io.github.marios_andr.yesbot.command.chess.ChessBoardDecor;
import io.github.marios_andr.yesbot.command.chess.ChessPieceDecor;
import com.google.gson.JsonObject;

import java.util.*;

public final class DatabaseHandler {

    private static Database DATABASE;

    public static void initialize() {
        if (Constants.getSettings().mongo_link().isEmpty()) {
            Constants.LOG.info("No mongo link was provided in settings.json so data will be saved locally instead.");
            DATABASE = new LocalDatabase();
        } else {
            try {
                DATABASE = new MongoDatabase();
            } catch (Exception e) {
                Constants.LOG.error("Something went wrong while initializing the Mongo connection. Data will be saved locally.", e);
                DATABASE = new LocalDatabase();
            }
        }
    }

    public static JsonObject getUserJson(String snowflake) {
        return DATABASE.getUserJson(snowflake);
    }

    public static ChessBoardDecor getSelectedBoard(String snowflake) {
        return DATABASE.getSelectedBoard(snowflake);
    }

    public static void setSelectedBoard(String snowflake, String board) {

    }

    public static ChessPieceDecor getSelectedPiece(String snowflake) {
        return DATABASE.getSelectedPiece(snowflake);
    }

    public static void addChessWin(String snowflake) {
        DATABASE.addChessWin(snowflake);
    }

    public static void addChessLoss(String snowflake) {
        DATABASE.addChessLoss(snowflake);
    }

    public static void addChessTie(String snowflake) {
        DATABASE.addChessTie(snowflake);
    }

    public static boolean hasPromotions(String snowflake) {
        return DATABASE.hasPromotions(snowflake);
    }

    public static Map<String, String> getPromotionsChannels(String snowflake) {
        return DATABASE.getPromotionsChannels(snowflake);
    }

    public static void addPromotionsChannel(String snowflake, String type, String id) {
        DATABASE.addPromotionsChannel(snowflake, type, id);
    }

    public static List<Announcement> getLastPromotions(String snowflake) {
        return DATABASE.getLastPromotions(snowflake);
    }

    public static void setLastPromotions(String snowflake, List<Announcement> announcements) {
        DATABASE.setLastPromotions(snowflake, announcements);
    }

    private DatabaseHandler() {
    }

    public static class User {
        final String id;
        int points = 0;
        int chessWins = 0;
        int chessLosses = 0;
        int chessTies = 0;
        String selectedBoard = "DEFAULT";
        String selectedPiece = "DEFAULT";
        Set<String> ownedItems = new HashSet<>(List.of("CP#0", "CB#0"));

        public User(String id) {
            this.id = id;
        }
    }

    public static class Guild {
        final String id;
        HashMap<String, String> promotionsChannels = new HashMap<>();
        List<Announcement> lastPromotions = new ArrayList<>();

        public Guild(String id) {
            this.id = id;
        }
    }
}
