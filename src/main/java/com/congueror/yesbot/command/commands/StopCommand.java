package com.congueror.yesbot.command.commands;

import com.congueror.yesbot.Constants;
import com.congueror.yesbot.command.Command;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class StopCommand implements Command {
    @Override
    public void handle(MessageReceivedEvent event) {
        if (check(getInput(event))) {
            Constants.STOP = true;
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
