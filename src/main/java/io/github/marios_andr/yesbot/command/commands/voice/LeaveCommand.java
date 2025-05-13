package io.github.marios_andr.yesbot.command.commands.voice;

import io.github.marios_andr.yesbot.command.AbstractCommand;
import io.github.marios_andr.yesbot.command.Command;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@Command
public class LeaveCommand extends AbstractCommand {

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        String[] leave = getInput(event.getMessage());
        if (check(leave)) {
            AudioChannel connectedChannel = Objects.requireNonNull(event.getGuild().getSelfMember().getVoiceState()).getChannel();
            if (connectedChannel == null) {
                event.getChannel().sendMessage("I am not connected to a voice channel!").queue();
                return;
            }
            event.getGuild().getAudioManager().closeAudioConnection();
            event.getChannel().sendMessage("Disconnected from the voice channel!").queue();
        }
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        AudioChannel connectedChannel = Objects.requireNonNull(event.getGuild().getSelfMember().getVoiceState()).getChannel();
        if (connectedChannel == null) {
            event.getHook().sendMessage("I am not connected to a voice channel!").queue();
            return;
        }
        event.getGuild().getAudioManager().closeAudioConnection();
        event.getHook().sendMessage("Disconnected from the voice channel!").queue();
    }

    @Override
    public String getName() {
        return "leave";
    }

    @Override
    public String getCommandDescription() {
        return "Make the bot leave the voice channel you're in.";
    }

    @Override
    public OptionData[] getArgs() {
        return new OptionData[] {};
    }

    @Override
    public String getCategory() {
        return VOICE;
    }
}
