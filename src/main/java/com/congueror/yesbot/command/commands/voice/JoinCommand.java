package com.congueror.yesbot.command.commands.voice;

import com.congueror.yesbot.command.Command;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class JoinCommand implements Command {
    @Override
    public void handle(MessageReceivedEvent event) {
        String[] join = getInput(event);
        if (check(join)) {
            var channel = joinVC(event);
            if (channel != null)
                event.getChannel().sendMessage("Connected to the voice channel!").queue();
        }
    }

    @Override
    public String getCategory() {
        return VOICE;
    }

    @Override
    public String getName() {
        return "join";
    }

    @Override
    public String[] getArgs() {
        return new String[] {};
    }

    @Override
    public String getDescription() {
        return "Make the bot join the voice channel you're in.";
    }
}
