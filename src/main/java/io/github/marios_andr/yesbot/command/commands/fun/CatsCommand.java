package io.github.marios_andr.yesbot.command.commands.fun;

import io.github.marios_andr.yesbot.command.AbstractCommand;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;

//@Command
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
    public Category getCategory() {
        return Category.FUN;
    }
}
