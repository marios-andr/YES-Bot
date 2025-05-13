package io.github.marios_andr.yesbot.command.commands.voice;

import io.github.marios_andr.yesbot.command.AbstractCommand;
import io.github.marios_andr.yesbot.command.Command;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;

@Command
public class JoinCommand extends AbstractCommand {
    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        String[] join = getInput(event.getMessage());
        if (check(join)) {
            var channel = joinVC(event);
            if (channel != null)
                event.getChannel().sendMessage("Connected to the voice channel!").queue();
        }
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        var channel = joinVC(event);
        if (channel != null)
            event.getHook().sendMessage("Connected to the voice channel!").queue();
    }

    @Override
    public String getName() {
        return "join";
    }

    @Override
    public OptionData[] getArgs() {
        return new OptionData[] {};
    }

    @Override
    public String getCommandDescription() {
        return "Make the bot join the voice channel you're in.";
    }

    @Override
    public String getCategory() {
        return VOICE;
    }
}
