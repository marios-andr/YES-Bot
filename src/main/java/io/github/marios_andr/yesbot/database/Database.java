package io.github.marios_andr.yesbot.database;

import com.google.gson.JsonObject;
import io.github.marios_andr.yesbot.command.announcements.Announcement;
import io.github.marios_andr.yesbot.command.chess.ChessBoardDecor;
import io.github.marios_andr.yesbot.command.chess.ChessPieceDecor;

import java.util.List;
import java.util.Map;

public interface Database {

    JsonObject getUserJson(String snowflake);

    ChessBoardDecor getSelectedBoard(String snowflake);

    ChessPieceDecor getSelectedPiece(String snowflake);

    void addChessWin(String snowflake);

    void addChessLoss(String snowflake);

    void addChessTie(String snowflake);

    boolean hasPromotions(String snowflake);

    Map<String, String> getPromotionsChannels(String snowflake);

    void addPromotionsChannel(String snowflake, String type, String id);

    List<Announcement> getLastPromotions(String snowflake);

    void setLastPromotions(String snowflake, List<Announcement> announcements);


}
