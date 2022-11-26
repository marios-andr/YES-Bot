package com.congueror.yesbot.command.commands.fun;

import com.congueror.yesbot.command.Command;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class CatsCommand implements Command {
    @Override
    public void handle(MessageReceivedEvent event) {
        String[] cats = getInput(event);
        if (check(cats)) {
            Message reference = event.getMessage();

            String[] subreddits = new String[]{"cats"};
            sendRandomPost(event, reference, subreddits);
        }
    }

    @Override
    public String getName() {
        return "cat";
    }

    @Override
    public String[] getArgs() {
        return new String[] {};
    }

    @Override
    public String getDescription() {
        return "Cats!";
    }

    @Override
    public String getCategory() {
        return FUN;
    }
}
