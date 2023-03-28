package com.congueror.yesbot.command.commands.fun;

import com.congueror.yesbot.command.AbstractCommand;
import com.congueror.yesbot.command.Command;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;

@Command
public class DogsCommand extends AbstractCommand {

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        String[] dogs = getInput(event.getMessage());
        if (check(dogs)) {
            Message reference = event.getMessage();

            String[] subreddits = new String[]{"dogpictures"};
            sendRandomPost(event, reference, subreddits);
        }
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        String[] subreddits = new String[]{"dogpictures"};
        sendRandomPost(event, subreddits);
    }

    @Override
    public String getName() {
        return "dog";
    }

    @Override
    public OptionData[] getArgs() {
        return new OptionData[] {};
    }

    @Override
    public String getCommandDescription() {
        return "Dogs!";
    }

    @Override
    public String getCategory() {
        return FUN;
    }
}
