package com.congueror.yesbot.command.commands.voice;

import com.congueror.yesbot.command.Command;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Objects;

public class LeaveCommand implements Command {
    @Override
    public void handle(MessageReceivedEvent event) {
        String[] leave = getInput(event);
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
    public String getCategory() {
        return VOICE;
    }

    @Override
    public String getName() {
        return "leave";
    }

    @Override
    public String getDescription() {
        return "Make the bot leave the voice channel you're in.";
    }

    @Override
    public String[] getArgs() {
        return new String[] {};
    }
}
