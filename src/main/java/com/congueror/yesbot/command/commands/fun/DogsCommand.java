package com.congueror.yesbot.command.commands.fun;

import com.congueror.yesbot.command.AbstractCommand;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class DogsCommand implements AbstractCommand {
    @Override
    public void handle(MessageReceivedEvent event) {
        String[] dogs = getInput(event);
        if (check(dogs)) {
            Message reference = event.getMessage();

            String[] subreddits = new String[]{"dogpictures"};
            sendRandomPost(event, reference, subreddits);
        }
    }

    @Override
    public String getName() {
        return "dog";
    }

    @Override
    public String[] getArgs() {
        return new String[] {};
    }

    @Override
    public String getDescription() {
        return "Dogs!";
    }

    @Override
    public String getCategory() {
        return ":frog: Fun";
    }
}
