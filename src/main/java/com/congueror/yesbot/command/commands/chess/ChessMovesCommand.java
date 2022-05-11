package com.congueror.yesbot.command.commands.chess;

import com.congueror.yesbot.command.AbstractCommand;
import com.congueror.yesbot.command.chess.ChessBoard;
import com.congueror.yesbot.command.chess.ChessPosition;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class ChessMovesCommand implements AbstractCommand {
    @Override
    public void handle(MessageReceivedEvent event) {
        String[] move = getInput(event);
        if (check(move)) {
            Message reference = event.getMessage();

            if (ChessBoard.isInGame(event.getAuthor().getId())) {
                if (!(move[1].length() >= 2)) {
                    event.getChannel().sendMessage("What the hell is that?").reference(reference).queue();
                }

                int first = 8 - Integer.parseInt(move[1].substring(1, 2));
                int second = move[1].substring(0, 1).toCharArray()[0] - 97;
                int[] from = new int[]{first, second};

                if (!ChessPosition.isInBounds(from)) {
                    event.getChannel().sendMessage("That is an invalid position.").reference(reference).queue();
                }

                ChessBoard game = ChessBoard.getGame(event.getAuthor().getId());
                if (game != null) {
                    event.getChannel().sendFile(game.drawBoard(from)).reference(reference).queue();
                }
            }
        }
    }

    @Override
    public String getName() {
        return "moves";
    }

    @Override
    public String[] getArgs() {
        return new String[]{"position"};
    }

    @Override
    public String getDescription() {
        return "Test";
    }

    @Override
    public String getCategory() {
        return ":robot: Testing";
    }
}
