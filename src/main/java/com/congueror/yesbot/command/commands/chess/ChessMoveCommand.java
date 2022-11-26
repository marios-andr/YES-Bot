package com.congueror.yesbot.command.commands.chess;

import com.congueror.yesbot.command.Command;
import com.congueror.yesbot.command.chess.ChessBoard;
import com.congueror.yesbot.command.chess.ChessPiece;
import com.congueror.yesbot.command.chess.ChessPosition;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.utils.FileUpload;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;

public class ChessMoveCommand implements Command {
    private Message message;
    private User mover;
    private ChessBoard game;

    @Override
    public void handle(MessageReceivedEvent event) {
        String[] move = getInput(event);
        if (check(move)) {
            Message reference = event.getMessage();

            if (ChessBoard.isInGame(event.getAuthor().getId())) {
                if (!(move[1].length() >= 4)) {
                    event.getChannel().sendMessage("What the hell is that?").setMessageReference(reference).queue();
                }

                int first = 8 - Integer.parseInt(move[1].substring(1, 2));
                int second = move[1].substring(0, 1).toCharArray()[0] - 97;
                int[] from = new int[]{first, second};

                int firstTo = 8 - Integer.parseInt(move[1].substring(3, 4));
                int secondTo = move[1].substring(2, 3).toCharArray()[0] - 97;
                int[] to = new int[]{firstTo, secondTo};
                if (!ChessPosition.isInBounds(from) || !ChessPosition.isInBounds(to)) {
                    event.getChannel().sendMessage("That is an invalid position.").setMessageReference(reference).queue();
                }

                game = ChessBoard.getGame(event.getAuthor().getId());
                if (game != null && game.isTurn(event.getAuthor().getId())) {
                    int error = game.move(event.getAuthor().getId(), from, to);
                    if (error == 0) {
                        String checked = "";
                        if (game.checkedPosition != null) {
                            checked = "Check! ";
                        }
                        if (game.winnerIndex == 0 || game.winnerIndex == 1) {
                            checked = "Checkmate! ";
                        }
                        event.getChannel().sendMessage(checked + User.fromId(game.getOpponent(event.getAuthor().getId())).getAsMention()).queue();
                        event.getChannel().sendFiles(FileUpload.fromData(game.drawBoard(null))).queue();
                    } else if (error == 1) {
                        event.getChannel().sendMessage("You do not own that tile.").setMessageReference(reference).queue();
                    } else if (error == 2) {
                        event.getChannel().sendMessage("You cannot move that piece there.").setMessageReference(reference).queue();
                    } else if (error == 3) {
                        event.getChannel().sendMessage("You are an imposter.").setMessageReference(reference).queue();
                    } else if (error == 4) {
                        event.getChannel().sendMessage("Awaiting promotion response from player.").setMessageReference(reference).queue();
                    }

                    if (game.winnerIndex == 0 || game.winnerIndex == 1) {
                        String winner = User.fromId(game.userIds[game.winnerIndex]).getAsMention();
                        String loser = User.fromId(game.userIds[game.winnerIndex == 1 ? 0 : 1]).getAsMention();
                        event.getChannel().sendMessage("Congratulations, " + winner + ", you have won the game of chess against " + loser).queue();
                    }

                    if (game.isStalemate) {
                        event.getChannel().sendMessage("No possible moves remain, the game has ended in a stalemate. How did you two manage this?").queue();
                    }

                    mover = event.getAuthor();
                    if (game.requiresPromotion != null) {
                        event.getChannel().sendMessage("""
                                        Your pawn can be promoted. Please choose what you'd like it to be promoted to.
                                        0: Remain the same
                                        1: Rook
                                        2: Knight
                                        3: Bishop
                                        4: Queen
                                        """).setMessageReference(reference)
                                .queue(message -> {
                                    message.addReaction(Emoji.fromFormatted("\u0030\ufe0f\u20e3")).queue();
                                    message.addReaction(Emoji.fromFormatted("\u0031\ufe0f\u20e3")).queue();
                                    message.addReaction(Emoji.fromFormatted("\u0032\ufe0f\u20e3")).queue();
                                    message.addReaction(Emoji.fromFormatted("\u0033\ufe0f\u20e3")).queue();
                                    message.addReaction(Emoji.fromFormatted("\u0034\ufe0f\u20e3")).queue();
                                    this.message = message;
                                });
                    } else if (error == 0) {
                        game.finishTurn();
                    }
                } else {
                    event.getChannel().sendMessage("It's not your turn!").setMessageReference(reference).queue();
                }
            } else {
                event.getChannel().sendMessage("You're not playing a game.").setMessageReference(reference).queue();
            }
        }
    }

    @Override
    public void handleMessageReaction(MessageReactionAddEvent event) {
        if (isReactionMessage(event, message, mover) && game.requiresPromotion != null) {
            if (event.getEmoji().getAsReactionCode().contains("\u0030")) {
                game.promote(null);
                event.getChannel().sendMessage(User.fromId(game.getOpponent(mover.getId())).getAsMention()).queue();
                event.getChannel().sendFiles(FileUpload.fromData(game.drawBoard(null))).queue();
                game.finishTurn();
            } else if (event.getEmoji().getAsReactionCode().contains("\u0031")) {
                game.promote(game.turn == 0 ? ChessPiece.W_ROOK : ChessPiece.B_ROOK);
                event.getChannel().sendMessage(User.fromId(game.getOpponent(mover.getId())).getAsMention()).queue();
                event.getChannel().sendFiles(FileUpload.fromData(game.drawBoard(null))).queue();
                game.finishTurn();
            } else if (event.getEmoji().getAsReactionCode().contains("\u0032")) {
                game.promote(game.turn == 0 ? ChessPiece.W_KNIGHT : ChessPiece.B_KNIGHT);
                event.getChannel().sendMessage(User.fromId(game.getOpponent(mover.getId())).getAsMention()).queue();
                event.getChannel().sendFiles(FileUpload.fromData(game.drawBoard(null))).queue();
                game.finishTurn();
            } else if (event.getEmoji().getAsReactionCode().contains("\u0033")) {
                game.promote(game.turn == 0 ? ChessPiece.W_BISHOP : ChessPiece.B_BISHOP);
                event.getChannel().sendMessage(User.fromId(game.getOpponent(mover.getId())).getAsMention()).queue();
                event.getChannel().sendFiles(FileUpload.fromData(game.drawBoard(null))).queue();
                game.finishTurn();
            } else if (event.getEmoji().getAsReactionCode().contains("\u0034")) {
                game.promote(game.turn == 0 ? ChessPiece.W_QUEEN : ChessPiece.B_QUEEN);
                event.getChannel().sendMessage(User.fromId(game.getOpponent(mover.getId())).getAsMention()).queue();
                event.getChannel().sendFiles(FileUpload.fromData(game.drawBoard(null))).queue();
                game.finishTurn();
            }
        }
    }

    @Override
    public String getName() {
        return "move";
    }

    @Override
    public String[] getArgs() {
        return new String[]{"position"};
    }

    @Override
    public String getDescription() {
        ArrayList<String> desc = new ArrayList<>();
        desc.add("Moves a chess piece in the active chess game of the player.");
        desc.add("position: The position to move to, must be written like: \"a2a4\", first two characters being the piece you want to move and second two the position to move to.");
        return StringUtils.join(desc, String.format("%n", ""));
    }

    @Override
    public String getCategory() {
        return CHESS;
    }
}
