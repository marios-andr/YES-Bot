package com.congueror.yesbot.command.commands;

import com.congueror.yesbot.command.Command;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class TestCommand implements Command {
    @Override
    public void handle(MessageReceivedEvent event) {
        String[] test = getInput(event);
        if (check(test)) {
            //event.getChannel().sendMessage("Fuck you \u0030\ufe0f\u20e3").queue(message -> message.addReaction("\u0030\ufe0f\u20e3").queue());

        }
    }

    @Override
    public String getCategory() {
        return TESTING;
    }

    @Override
    public String getName() {
        return "test";
    }

    @Override
    public String[] getArgs() {
        return new String[]{};
    }

    @Override
    public String getDescription() {
        return "go away";
    }
}
