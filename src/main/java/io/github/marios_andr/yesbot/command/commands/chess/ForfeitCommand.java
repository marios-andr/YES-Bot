package io.github.marios_andr.yesbot.command.commands.chess;

import io.github.marios_andr.yesbot.command.AbstractCommand;
import io.github.marios_andr.yesbot.command.Command;
import io.github.marios_andr.yesbot.command.chess.ChessBoard;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;

@Command
public class ForfeitCommand extends AbstractCommand {
    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        String[] forfeit = getInput(event.getMessage());
        if (check(forfeit)) {
            Message reference = event.getMessage();

            if (ChessBoard.isInGame(event.getAuthor().getId())) {
                ChessBoard game = ChessBoard.getGame(event.getAuthor().getId());
                String[] players = game.userIds;
                if (players[0].equals(event.getAuthor().getId())) {
                    game.winnerIndex = 1;
                } else if (players[1].equals(event.getAuthor().getId())) {
                    game.winnerIndex = 0;
                }
                String winner = User.fromId(game.userIds[game.winnerIndex]).getAsMention();
                String loser = User.fromId(game.userIds[game.winnerIndex == 1 ? 0 : 1]).getAsMention();

                game.initiateWinnerSequence();
                event.getChannel().sendMessage(winner + ", " + loser + " has forfeited from the game, leaving you victorious. Congratulations.").setMessageReference(reference).queue();
            } else {
                event.getChannel().sendMessage("You're not playing a game.").setMessageReference(reference).queue();
            }
        }
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        var id = event.getUser().getId();
        if (ChessBoard.isInGame(id)) {
            ChessBoard game = ChessBoard.getGame(id);
            String[] players = game.userIds;
            if (players[0].equals(id)) {
                game.winnerIndex = 1;
            } else if (players[1].equals(id)) {
                game.winnerIndex = 0;
            }
            String winner = User.fromId(game.userIds[game.winnerIndex]).getAsMention();
            String loser = User.fromId(game.userIds[game.winnerIndex == 1 ? 0 : 1]).getAsMention();

            game.initiateWinnerSequence();
            event.getHook().sendMessage(winner + ", " + loser + " has forfeited from the game, leaving you victorious. Congratulations.").queue();
        } else {
            event.getHook().sendMessage("You're not playing a game.").queue();
        }
    }

    @Override
    public String getName() {
        return "forfeit";
    }

    @Override
    public OptionData[] getArgs() {
        return new OptionData[]{};
    }

    @Override
    public String getCommandDescription() {
        return "Forfeit from a match... you coward.";
    }

    @Override
    public Category getCategory() {
        return Category.CHESS;
    }
}
