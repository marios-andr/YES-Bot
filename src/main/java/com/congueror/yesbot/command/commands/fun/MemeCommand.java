package com.congueror.yesbot.command.commands.fun;

import com.congueror.yesbot.command.AbstractCommand;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;

public class MemeCommand implements AbstractCommand {
    @Override
    public void handle(MessageReceivedEvent event) {
        String[] meme = getInput(event);
        if (check(meme)) {
            Message reference = event.getMessage();

            String[] subreddits = new String[]{"memes", "dankmemes"};
            sendRandomPost(event, reference, subreddits);
        }
    }

    @Override
    public String getName() {
        return "meme";
    }

    @Override
    public String[] getArgs() {
        return new String[]{};
    }

    @Override
    public String getDescription() {
        ArrayList<String> desc = new ArrayList<>();
        desc.add("Send a random meme from a meme subreddit");
        return StringUtils.join(desc, String.format("%n", ""));
    }

    @Override
    public String getCategory() {
        return ":frog: Fun";
    }
}
