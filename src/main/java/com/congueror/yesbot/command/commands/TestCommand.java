package com.congueror.yesbot.command.commands;

import com.congueror.yesbot.command.AbstractCommand;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.io.*;

public class TestCommand implements AbstractCommand {
    @Override
    public void handle(MessageReceivedEvent event) {
        String[] test = getInput(event);
        if (check(test)) {
            //event.getChannel().sendMessage("Fuck you \u0030\ufe0f\u20e3").queue(message -> message.addReaction("\u0030\ufe0f\u20e3").queue());
        }
    }

    @Override
    public String getCategory() {
        return ":robot: Testing";
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
