package com.congueror.yesbot.command.commands.chess;

import com.congueror.yesbot.command.Command;
import com.congueror.yesbot.command.chess.ChessBoard;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class ForfeitCommand implements Command {
    @Override
    public void handle(MessageReceivedEvent event) {
        String[] forfeit = getInput(event);
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
    public String getName() {
        return "forfeit";
    }

    @Override
    public String[] getArgs() {
        return new String[]{};
    }

    @Override
    public String getDescription() {
        return "Forfeit from a match... you coward.";
    }

    @Override
    public String getCategory() {
        return CHESS;
    }
}
