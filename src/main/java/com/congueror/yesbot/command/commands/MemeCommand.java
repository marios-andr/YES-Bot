package com.congueror.yesbot.command.commands;

import com.congueror.yesbot.command.AbstractCommand;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;

public class MemeCommand implements AbstractCommand {
    @Override
    public void handle(MessageReceivedEvent event) {

    }

    @Override
    public String getName() {
        return "meme";
    }

    @Override
    public String[] getArgs() {
        return new String[] {};
    }

    @Override
    public String getDescription() {
        ArrayList<String> desc = new ArrayList<>();
        desc.add("Send a random meme from reddit's r/DankMemes subreddit");
        return StringUtils.join(desc, String.format("%n", ""));
    }

    @Override
    public String getCategory() {
        return ":robot: Testing";
    }
}
