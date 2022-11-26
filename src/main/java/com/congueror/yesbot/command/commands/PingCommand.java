package com.congueror.yesbot.command.commands;

import com.congueror.yesbot.command.Command;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class PingCommand implements Command {
    @Override
    public void handle(MessageReceivedEvent event) {
        String[] ping = getInput(event);
        if(check(ping)) {
            event.getJDA().getRestPing().queue(ping1 -> event.getChannel().sendMessageFormat("Reset ping: %sms\nWS ping: %sms", ping1, event.getJDA().getGatewayPing()).queue());
        }
    }

    @Override
    public String getName() {
        return "ping";
    }

    @Override
    public String[] getArgs() {
        return new String[] {};
    }

    @Override
    public String getDescription() {
        return "The ping of the bot.";
    }
}
