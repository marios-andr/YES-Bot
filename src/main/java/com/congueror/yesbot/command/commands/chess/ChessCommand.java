package com.congueror.yesbot.command.commands.chess;

import com.congueror.yesbot.command.AbstractCommand;
import com.congueror.yesbot.command.chess.ChessBoard;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;

public class ChessCommand implements AbstractCommand {

    private Message message;
    private User challenger;
    private User challenged;

    @Override
    public void handle(MessageReceivedEvent event) {
        String[] chess = getInput(event);
        if (check(chess)) {
            Message reference = event.getMessage();

            final String player1 = event.getAuthor().getId();
            if (chess.length == 2) {
                final String player2 = reference.getMentionedMembers().get(0).getId();
                if (!player1.equals(player2)) {
                    if (ChessBoard.isInGame(player1) || ChessBoard.isInGame(player2)) {
                        event.getChannel().sendMessage("User is already playing.").reference(reference).queue();
                        return;
                    }

                    challenger = event.getAuthor();
                    challenged = reference.getMentionedMembers().get(0).getUser();
                    event.getChannel().sendMessage(chess[1] + ", " + event.getAuthor().getAsMention() + " has challenged you to a game of chess!" +
                            " Do you accept?").reference(reference).queue(message -> {
                        message.addReaction("U+2705").queue();
                        message.addReaction("U+274E").queue();
                        this.message = message;
                    });
                } else {
                    event.getChannel().sendMessage("You need to ping a friend... if you have one.").reference(reference).queue();
                }
            } else {
                if (ChessBoard.isInGame(player1)) {
                    event.getChannel().sendFile(ChessBoard.getGame(player1).drawBoard()).reference(reference).queue();
                } else {
                    event.getChannel().sendFile(ChessBoard.newChessBoard(new String[]{player1}).drawBoard()).reference(reference).queue();
                }

            }
        }
    }

    @Override
    public void handleMessageReaction(MessageReactionAddEvent event) {
        if (isReactionMessage(event, message, challenged)) {
            if (event.getReactionEmote().getEmoji().equals("\u2705")) {
                event.getChannel().sendMessage(challenger.getAsMention() + ", " + challenged.getAsMention() + " accepted your challenge! Good luck.").reference(this.message).queue();
                ChessBoard board = ChessBoard.newTestChessBoard(new String[]{challenger.getId(), challenged.getId()});
                event.getChannel().sendFile(board.drawBoard()).reference(message).queue();

                message = null;
            } else if (event.getReactionEmote().getEmoji().equals("\u274E")) {
                event.getChannel().sendMessage(challenger.getAsMention() + ", " + challenged.getAsMention() + " declined your challenge. What a pussy.").reference(this.message).queue();
                message = null;
            }
        }
    }

    @Override
    public String getName() {
        return "chess";
    }

    @Override
    public String[] getArgs() {
        return new String[]{"player"};
    }

    @Override
    public String getDescription() {
        return "for temporary testing";
    }

    @Override
    public String getCategory() {
        return ":robot: Testing";
    }
}
