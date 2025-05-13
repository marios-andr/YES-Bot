package io.github.marios_andr.yesbot.command.commands.chess;

import io.github.marios_andr.yesbot.command.AbstractCommand;
import io.github.marios_andr.yesbot.command.Command;
import io.github.marios_andr.yesbot.command.chess.ChessBoard;
import io.github.marios_andr.yesbot.command.chess.ChessPosition;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.utils.FileUpload;
import org.jetbrains.annotations.NotNull;

@Command
public class ChessMovesCommand extends AbstractCommand {
    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        String[] moves = getInput(event.getMessage());
        if (check(moves)) {
            Message reference = event.getMessage();

            if (ChessBoard.isInGame(event.getAuthor().getId())) {
                if (!(moves[1].length() >= 2)) {
                    event.getChannel().sendMessage("What the hell is that?").setMessageReference(reference).queue();
                }

                int first = 8 - Integer.parseInt(moves[1].substring(1, 2));
                int second = moves[1].substring(0, 1).toCharArray()[0] - 97;
                int[] from = new int[]{first, second};

                if (!ChessPosition.isInBounds(from)) {
                    event.getChannel().sendMessage("That is an invalid position.").setMessageReference(reference).queue();
                }

                ChessBoard game = ChessBoard.getGame(event.getAuthor().getId());
                if (game != null) {
                    event.getChannel().sendFiles(FileUpload.fromData(game.drawBoard(from))).setMessageReference(reference).queue();
                }
            } else {
                event.getChannel().sendMessage("You're not playing a game.").setMessageReference(reference).queue();
            }
        }
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (ChessBoard.isInGame(event.getUser().getId())) {
            var pos = event.getOption("position").getAsString();
            if (!(pos.length() >= 2)) {
                event.getHook().sendMessage("What the hell is that?").queue();
            }

            int first = 8 - Integer.parseInt(pos.substring(1, 2));
            int second = pos.substring(0, 1).toCharArray()[0] - 97;
            int[] from = new int[]{first, second};

            if (!ChessPosition.isInBounds(from)) {
                event.getHook().sendMessage("That is an invalid position.").queue();
            }

            ChessBoard game = ChessBoard.getGame(event.getUser().getId());
            if (game != null) {
                event.getHook().sendFiles(FileUpload.fromData(game.drawBoard(from))).queue();
            }
        } else {
            event.getHook().sendMessage("You're not playing a game.").queue();
        }
    }

    @Override
    public String getName() {
        return "moves";
    }

    @Override
    public OptionData[] getArgs() {
        return new OptionData[] {
                new OptionData(OptionType.STRING, "position", "The position of the piece, must be written like: \"a2\".", true)
        };
    }

    @Override
    public String getCommandDescription() {
        return "Displays the moves a piece can make.";
    }

    @Override
    public String getCategory() {
        return CHESS;
    }
}
