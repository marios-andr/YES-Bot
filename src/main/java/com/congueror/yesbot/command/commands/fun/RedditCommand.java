package com.congueror.yesbot.command.commands.fun;

import com.congueror.yesbot.command.Command;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;

public class RedditCommand implements Command {
    @Override
    public void handle(MessageReceivedEvent event) {
        String[] reddit = getInput(event);
        if (check(reddit)) {
            Message reference = event.getMessage();

            if (reddit[1].equals("nsfw"))
                sendRandomPost(event, reference, "nsfw", "bonermaterial", "iWantToFuckHer", "gonewild", "nudes", "legalteens");
            else
                sendRandomPost(event, reference, reddit[1]);
        }
    }

    @Override
    public String getName() {
        return "reddit";
    }

    @Override
    public String[] getArgs() {
        return new String[]{"subreddit"};
    }

    @Override
    public String getDescription() {
        ArrayList<String> desc = new ArrayList<>();
        desc.add("Send a random post from a subreddit");
        return StringUtils.join(desc, String.format("%n", ""));
    }

    @Override
    public String getCategory() {
        return FUN;
    }
}
