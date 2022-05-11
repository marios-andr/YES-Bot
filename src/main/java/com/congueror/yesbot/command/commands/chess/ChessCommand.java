package com.congueror.yesbot.command.commands.chess;

import com.congueror.yesbot.YESBot;
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

            final User player1 = event.getAuthor();
            if (chess.length == 2) {
                if (reference.getMentionedMembers().size() > 0) {
                    final User player2 = reference.getMentionedMembers().get(0).getUser();
                    if (!player1.getId().equals(player2.getId())) {
                        if (!(!player1.isBot() || player1.getId().equals(YESBot.SNOWFLAKE_ID)) || !(!player2.isBot() || player2.getId().equals(YESBot.SNOWFLAKE_ID))) {
                            event.getChannel().sendMessage("You need to play with a real person. Probably a weird concept to you...").reference(reference).queue();
                            return;
                        }
                        if (ChessBoard.isInGame(player1.getId()) || ChessBoard.isInGame(player2.getId())) {
                            event.getChannel().sendMessage("User is already playing.").reference(reference).queue();
                            return;
                        }

                        challenger = player1;
                        challenged = player2;
                        event.getChannel().sendMessage(chess[1] + ", " + event.getAuthor().getAsMention() + " has challenged you to a game of chess!" +
                                " Do you accept?").reference(reference).queue(message -> {
                            message.addReaction("U+2705").queue();
                            message.addReaction("U+274E").queue();
                            this.message = message;
                        });
                    } else {
                        event.getChannel().sendMessage("Can't play with yourself on this one chief.").reference(reference).queue();
                    }
                } else {
                    event.getChannel().sendMessage("You need to ping a friend... if you have one.").reference(reference).queue();
                }
            } else {
                if (ChessBoard.isInGame(player1.getId())) {
                    event.getChannel().sendFile(ChessBoard.getGame(player1.getId()).drawBoard(null)).reference(reference).queue();
                } else {
                    event.getChannel().sendFile(ChessBoard.newChessBoard(new String[]{player1.getId()}).drawBoard(null)).reference(reference).queue();
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
                event.getChannel().sendFile(board.drawBoard(null)).reference(message).queue();

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
