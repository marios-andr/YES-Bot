package io.github.marios_andr.yesbot.database;

import io.github.marios_andr.yesbot.Constants;
import io.github.marios_andr.yesbot.command.announcements.Announcement;
import io.github.marios_andr.yesbot.command.chess.ChessBoardDecor;
import io.github.marios_andr.yesbot.command.chess.ChessPieceDecor;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.*;

public class DatabaseHandler {

    private static boolean isMongoInitialized = false;

    public static void initialize() {
        if (Constants.getSettings().mongo_link().isEmpty())
            Constants.LOG.info("No mongo link was provided in settings.json so data will be saved locally instead.");
        else {
            try {
                Mongo.initialize();
                isMongoInitialized = true;
                return;
            } catch (Exception e) {
                Constants.LOG.error("Something went wrong while initializing the Mongo connection. Data will be saved locally.", e);
            }
        }

        Local.initialize();

    }

    public static JsonObject getUserJson(String snowflake) {
        if (isMongoInitialized)
            return JsonParser.parseString(Mongo.getUserDocument(snowflake).toJson()).getAsJsonObject();
        return Local.getUserJson(snowflake);
    }

    public static ChessBoardDecor getSelectedBoard(String snowflake) {
        if (isMongoInitialized)
            return Mongo.getSelectedBoard(snowflake);
        return Local.getSelectedBoard(snowflake);
    }

    public static ChessPieceDecor getSelectedPiece(String snowflake) {
        if (isMongoInitialized)
            return Mongo.getSelectedPiece(snowflake);
        return Local.getSelectedPiece(snowflake);
    }

    public static void addChessWin(String snowflake) {
        if (isMongoInitialized)
            Mongo.addChessWin(snowflake);
        else
            Local.addChessWin(snowflake);
    }

    public static void addChessLoss(String snowflake) {
        if (isMongoInitialized)
            Mongo.addChessLoss(snowflake);
        else
            Local.addChessLoss(snowflake);
    }

    public static void addChessTie(String snowflake) {
        if (isMongoInitialized)
            Mongo.addChessTie(snowflake);
        else
            Local.addChessTie(snowflake);
    }

    public static boolean hasPromotions(String snowflake) {
        if (isMongoInitialized)
            return Mongo.getAnnouncements(snowflake) != null;
        return Local.getGuild(snowflake).promotionsChannels != null;
    }

    public static Map<String, String> getPromotionsChannels(String snowflake) {
        if (isMongoInitialized)
            return Mongo.getAnnouncements(snowflake);
        return Local.getGuild(snowflake).promotionsChannels;
    }

    public static void addPromotionsChannel(String snowflake, String type, String id) {
        if (isMongoInitialized)
            Mongo.addAnnouncements(snowflake, type, id);
        else
            Local.addPromotionsChannel(snowflake, type, id);
    }

    public static List<Announcement> getLastPromotions(String snowflake) {
        if (isMongoInitialized)
            return Mongo.getLastAnnouncements(snowflake);
        return Local.getGuild(snowflake).lastPromotions;
    }

    public static void setLastPromotions(String snowflake, List<Announcement> announcements) {
        if (isMongoInitialized)
            Mongo.setLastAnnouncements(snowflake, announcements);
        else
            Local.setLastPromotions(snowflake, announcements);
    }

    private DatabaseHandler() {}

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
