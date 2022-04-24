package com.congueror.yesbot.command.commands;

import com.congueror.yesbot.BotListenerAdapter;
import com.congueror.yesbot.command.AbstractCommand;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class StopCommand implements AbstractCommand {
    @Override
    public void handle(MessageReceivedEvent event) {
        if (check(getInput(event))) {
            BotListenerAdapter.shouldStop = true;
        }
    }

    @Override
    public String getName() {
        return "stop";
    }

    @Override
    public String[] getArgs() {
        return new String[] {};
    }

    @Override
    public String getDescription() {
        return "Stop any repeating commands";
    }
}
