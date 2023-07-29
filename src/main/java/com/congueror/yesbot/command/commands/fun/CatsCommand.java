package com.congueror.yesbot.command.commands.fun;

import com.congueror.yesbot.command.AbstractCommand;
import com.congueror.yesbot.command.Command;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;

@Command
public class CatsCommand extends AbstractCommand {
    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        String[] cats = getInput(event.getMessage());
        if (check(cats)) {
            Message reference = event.getMessage();

            String[] subreddits = new String[]{"cats"};
            sendRandomPost(event, reference, subreddits);
        }
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        String[] subreddits = new String[]{"cats"};
        sendRandomPost(event, subreddits);
    }

    @Override
    public String getName() {
        return "cat";
    }

    @Override
    public OptionData[] getArgs() {
        return new OptionData[] {};
    }

    @Override
    public String getCommandDescription() {
        return "Cats!";
    }

    @Override
    public String getCategory() {
        return FUN;
    }
}
