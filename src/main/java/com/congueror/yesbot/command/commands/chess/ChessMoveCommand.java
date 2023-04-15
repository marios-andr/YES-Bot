package com.congueror.yesbot.command.commands.chess;

import com.congueror.yesbot.command.AbstractCommand;
import com.congueror.yesbot.command.Command;
import com.congueror.yesbot.command.chess.ChessBoard;
import com.congueror.yesbot.command.chess.ChessPiece;
import com.congueror.yesbot.command.chess.ChessPosition;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.utils.FileUpload;
import org.jetbrains.annotations.NotNull;

@Command
public class ChessMoveCommand extends AbstractCommand {

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        String[] move = getInput(event.getMessage());
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

                ChessBoard game = ChessBoard.getGame(event.getAuthor().getId());
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
                                    ChessBoard.createPendingPromotion(message.getId(), game);
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
    public void onMessageReactionAdd(MessageReactionAddEvent event) {
        reaction(event, "\u0030", null);
        reaction(event, "\u0031", ChessPiece.Type.ROOK);
        reaction(event, "\u0032", ChessPiece.Type.KNIGHT);
        reaction(event, "\u0033", ChessPiece.Type.BISHOP);
        reaction(event, "\u0034", ChessPiece.Type.QUEEN);
    }

    private void reaction(MessageReactionAddEvent event, String emoji, ChessPiece.Type piece) {
        if (event.getEmoji().getAsReactionCode().contains(emoji)) {
            ChessBoard.promotePending(event.getMessageId(), piece, game -> {
                event.getChannel().sendMessage(User.fromId(game.getTurn()).getAsMention()).queue();
                event.getChannel().sendFiles(FileUpload.fromData(game.drawBoard(null))).queue();
            });
        }
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        var id = event.getUser().getId();
        if (ChessBoard.isInGame(id)) {
            var pos = event.getOption("position").getAsString();
            if (!(pos.length() >= 4)) {
                event.getHook().sendMessage("What the hell is that?").queue();
            }

            int first = 8 - Integer.parseInt(pos.substring(1, 2));
            int second = pos.substring(0, 1).toCharArray()[0] - 97;
            int[] from = new int[]{first, second};

            int firstTo = 8 - Integer.parseInt(pos.substring(3, 4));
            int secondTo = pos.substring(2, 3).toCharArray()[0] - 97;
            int[] to = new int[]{firstTo, secondTo};
            if (!ChessPosition.isInBounds(from) || !ChessPosition.isInBounds(to)) {
                event.getHook().sendMessage("That is an invalid position.").queue();
            }

            ChessBoard game = ChessBoard.getGame(id);
            if (game != null && game.isTurn(id)) {
                int error = game.move(id, from, to);
                if (error == 0) {
                    String checked = "";
                    if (game.checkedPosition != null) {
                        checked = "Check! ";
                    }
                    if (game.winnerIndex == 0 || game.winnerIndex == 1) {
                        checked = "Checkmate! ";
                    }
                    event.getHook().sendMessage(checked + User.fromId(game.userIds[0]).getAsMention() + " vs" + User.fromId(game.userIds[1]).getAsMention())
                            .addFiles(FileUpload.fromData(game.drawBoard(null)))
                            .queue();
                } else if (error == 1) {
                    event.getHook().sendMessage("You do not own that tile.").queue();
                } else if (error == 2) {
                    event.getHook().sendMessage("You cannot move that piece there.").queue();
                } else if (error == 3) {
                    event.getHook().sendMessage("You are an imposter.").queue();
                } else if (error == 4) {
                    event.getHook().sendMessage("Awaiting promotion response from player.").queue();
                }

                if (game.winnerIndex == 0 || game.winnerIndex == 1) {
                    String winner = User.fromId(game.userIds[game.winnerIndex]).getAsMention();
                    String loser = User.fromId(game.userIds[game.winnerIndex == 1 ? 0 : 1]).getAsMention();
                    event.getHook().sendMessage("Congratulations, " + winner + ", you have won the game of chess against " + loser).queue();
                }

                if (game.isStalemate) {
                    event.getHook().sendMessage("No possible moves remain, the game has ended in a stalemate. How did you two manage this?").queue();
                }

                if (game.requiresPromotion != null) {
                    event.getHook().sendMessage("""
                                    Your pawn can be promoted. Please choose what you'd like it to be promoted to.
                                    0: Remain the same
                                    1: Rook
                                    2: Knight
                                    3: Bishop
                                    4: Queen
                                    """)
                            .queue(message -> {
                                message.addReaction(Emoji.fromFormatted("\u0030\ufe0f\u20e3")).queue();
                                message.addReaction(Emoji.fromFormatted("\u0031\ufe0f\u20e3")).queue();
                                message.addReaction(Emoji.fromFormatted("\u0032\ufe0f\u20e3")).queue();
                                message.addReaction(Emoji.fromFormatted("\u0033\ufe0f\u20e3")).queue();
                                message.addReaction(Emoji.fromFormatted("\u0034\ufe0f\u20e3")).queue();
                                ChessBoard.createPendingPromotion(message.getId(), game);
                            });
                } else if (error == 0) {
                    game.finishTurn();
                }
            } else {
                event.getHook().sendMessage("It's not your turn!").queue();
            }
        } else {
            event.getHook().sendMessage("You're not playing a game.").queue();
        }
    }

    @Override
    public String getName() {
        return "move";
    }

    @Override
    public OptionData[] getArgs() {
        return new OptionData[] {
                new OptionData(OptionType.STRING, "position", "The position to move to, must be written like: \"a2a4\"", true)
        };
    }

    @Override
    public String getCommandDescription() {
        return "Moves a chess piece in the active chess game of the player.";
    }

    @Override
    public String getCategory() {
        return CHESS;
    }
}
