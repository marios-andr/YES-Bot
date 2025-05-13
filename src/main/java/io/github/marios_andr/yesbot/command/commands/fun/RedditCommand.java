package io.github.marios_andr.yesbot.command.commands.fun;

import io.github.marios_andr.yesbot.command.AbstractCommand;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;

//@Command
public class RedditCommand extends AbstractCommand {
    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        String[] reddit = getInput(event.getMessage());
        if (check(reddit)) {
            Message reference = event.getMessage();
            sendRandomPost(event, reference, reddit[1]);
        }
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        sendRandomPost(event, event.getOption("subreddit").getAsString());
    }

    @Override
    public String getName() {
        return "reddit";
    }

    @Override
    public OptionData[] getArgs() {
        return new OptionData[]{
                new OptionData(OptionType.STRING, "subreddit", "Target subreddit.", true)
        };
    }

    @Override
    public String getCommandDescription() {
        return "Send a random post from a subreddit";
    }

    @Override
    public String getCategory() {
        return FUN;
    }
}
