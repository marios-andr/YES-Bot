package com.congueror.yesbot.command.commands.chess;

import com.congueror.yesbot.command.Command;
import com.congueror.yesbot.command.chess.ChessBoard;
import com.congueror.yesbot.command.chess.ChessPosition;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.utils.FileUpload;

public class ChessMovesCommand implements Command {
    @Override
    public void handle(MessageReceivedEvent event) {
        String[] moves = getInput(event);
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
        return CHESS;
    }
}
