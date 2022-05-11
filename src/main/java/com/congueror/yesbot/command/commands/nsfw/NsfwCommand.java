package com.congueror.yesbot.command.commands.nsfw;

import com.congueror.yesbot.command.AbstractCommand;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;

public class NsfwCommand implements AbstractCommand {
    @Override
    public void handle(MessageReceivedEvent event) {//https://old.reddit.com/r/ListOfSubreddits/wiki/nsfw
        String[] nsfw = getInput(event);
        if (check(nsfw)) {
            Message reference = event.getMessage();

            String[] subreddits = new String[]{"nsfw", "bonermaterial", "iWantToFuckHer", "gonewild", "nudes", "legalteens"};
            sendRandomPost(event, reference, subreddits);
        }
    }

    @Override
    public String getName() {
        return "nsfw";
    }

    @Override
    public String[] getArgs() {
        return new String[] {};
    }

    @Override
    public String getDescription() {
        ArrayList<String> desc = new ArrayList<>();
        desc.add("Send a random meme from a meme subreddit");
        return StringUtils.join(desc, String.format("%n", ""));
    }

    @Override
    public String getCategory() {
        return ":underage: NSFW";
    }
}