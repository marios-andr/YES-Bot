package com.congueror.yesbot.command.commands.chess;

import com.congueror.yesbot.Constants;
import com.congueror.yesbot.command.AbstractCommand;
import com.congueror.yesbot.command.Command;
import com.congueror.yesbot.command.chess.ChessBoard;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.internal.utils.tuple.ImmutablePair;
import org.jetbrains.annotations.NotNull;

@Command
public class ChessCommand extends AbstractCommand {

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        String[] chess = getInput(event.getMessage());
        if (check(chess)) {
            Message reference = event.getMessage();

            final User player1 = event.getAuthor();
            if (chess.length == 2) {
                if (reference.getMentions().getMembers().size() > 0) {
                    final User player2 = reference.getMentions().getMembers().get(0).getUser();
                    if (!player1.getId().equals(player2.getId())) {
                        final String SNOWFLAKE_ID = Constants.getSettings().bot_snowflake();
                        if (!(!player1.isBot() || player1.getId().equals(SNOWFLAKE_ID)) || !(!player2.isBot() || player2.getId().equals(SNOWFLAKE_ID))) {
                            event.getChannel().sendMessage("You need to play with a real person. Probably a weird concept to you...").setMessageReference(reference).queue();
                            return;
                        }
                        if (ChessBoard.isInGame(player1.getId()) || ChessBoard.isInGame(player2.getId())) {
                            event.getChannel().sendMessage("User is already playing.").setMessageReference(reference).queue();
                            return;
                        }

                        event.getChannel().sendMessage(chess[1] + ", " + event.getAuthor().getAsMention() + " has challenged you to a game of chess!" +
                                " Do you accept?").setMessageReference(reference).queue(message -> {
                            message.addReaction(Emoji.fromFormatted("U+2705")).queue();
                            message.addReaction(Emoji.fromFormatted("U+274E")).queue();
                            ChessBoard.createRequest(message.getId(), player1.getId(), player2.getId());
                        });
                    } else {
                        event.getChannel().sendMessage("Can't play with yourself on this one chief.").setMessageReference(reference).queue();
                    }
                } else {
                    event.getChannel().sendMessage("You need to ping a friend... if you have one.").setMessageReference(reference).queue();
                }
            } else {
                if (ChessBoard.isInGame(player1.getId())) {
                    event.getChannel().sendFiles(FileUpload.fromData(ChessBoard.getGame(player1.getId()).drawBoard(null))).setMessageReference(reference).queue();
                } else {
                    event.getChannel().sendFiles(FileUpload.fromData(ChessBoard.newChessBoard(new String[]{player1.getId()}).drawBoard(null))).setMessageReference(reference).queue();
                }
            }
        }
    }

    @Override
    public void onMessageReactionAdd(@NotNull MessageReactionAddEvent event) {
        if (event.getReaction().getEmoji().getName().equals("\u2705")) {
            ChessBoard board = ChessBoard.acceptRequest(event.getMessageId(), event.getUser().getId(), (s, s2) -> {
                event.getChannel().sendMessage(mention(s) + ", " + mention(s2) + " accepted your challenge! Good luck.").setMessageReference(event.getMessageId()).queue();
            });
            event.getChannel().sendFiles(FileUpload.fromData(board.drawBoard(null))).setMessageReference(event.getMessageId()).queue();
        } else if (event.getReaction().getEmoji().getName().equals("\u274E")) {
            ChessBoard.declineRequest(event.getMessageId(), event.getUser().getId(), (s, s2) -> {
                event.getChannel().sendMessage(mention(s) + ", " + mention(s2) + " declined your challenge.").setMessageReference(event.getMessageId()).queue();
            });
        }
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        final User player1 = event.getUser();
        var op = event.getOption("player");
        if (op != null) {
            final User player2 = op.getAsUser();
            if (!player1.getId().equals(player2.getId())) {
                final String SNOWFLAKE_ID = Constants.getSettings().bot_snowflake();
                if (!(!player1.isBot() || player1.getId().equals(SNOWFLAKE_ID)) || !(!player2.isBot() || player2.getId().equals(SNOWFLAKE_ID))) {
                    event.getHook().sendMessage("You need to play with a real person. Probably a weird concept to you...").queue();
                    return;
                }
                if (ChessBoard.isInGame(player1.getId()) || ChessBoard.isInGame(player2.getId())) {
                    event.getHook().sendMessage("User is already playing.").queue();
                    return;
                }

                event.getHook().sendMessage(player2.getAsMention() + ", " + player1.getAsMention() + " has challenged you to a game of chess!" +
                        " Do you accept?").queue(message -> {
                    message.addReaction(Emoji.fromFormatted("U+2705")).queue();
                    message.addReaction(Emoji.fromFormatted("U+274E")).queue();
                    ChessBoard.createRequest(message.getId(), player1.getId(), player2.getId());
                });
            } else {
                event.getHook().sendMessage("Can't play with yourself on this one chief.").queue();
            }
        } else {
            if (ChessBoard.isInGame(player1.getId())) {
                event.getHook().sendFiles(FileUpload.fromData(ChessBoard.getGame(player1.getId()).drawBoard(null))).queue();
            } else {
                event.getHook().sendFiles(FileUpload.fromData(ChessBoard.newChessBoard(new String[]{player1.getId()}).drawBoard(null))).queue();
            }
        }
    }

    @Override
    public String getName() {
        return "chess";
    }

    @Override
    public OptionData[] getArgs() {
        return new OptionData[]{
                new OptionData(OptionType.USER, "player", "The player to play against", false)
        };
    }

    @Override
    public String getCommandDescription() {
        return "Challenge someone to a game of chess!";
    }

    @Override
    public String getCategory() {
        return CHESS;
    }
}
