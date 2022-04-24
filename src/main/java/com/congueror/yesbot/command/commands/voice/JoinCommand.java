package com.congueror.yesbot.command.commands.voice;

import com.congueror.yesbot.command.AbstractCommand;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.AudioChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.managers.AudioManager;

import java.util.Objects;

public class JoinCommand implements AbstractCommand {
    @Override
    public void handle(MessageReceivedEvent event) {
        String[] join = getInput(event);
        if (check(join)) {
            if (event.getGuild().getSelfMember().hasPermission(event.getGuildChannel(), Permission.VOICE_CONNECT)) {
                AudioChannel connectedChannel = Objects.requireNonNull(Objects.requireNonNull(event.getMember()).getVoiceState()).getChannel();
                if (connectedChannel == null) {
                    event.getChannel().sendMessage("not in channel").queue();
                    return;
                }
                AudioManager audioManager = event.getGuild().getAudioManager();
                audioManager.openAudioConnection(connectedChannel);
                event.getChannel().sendMessage("Connected to the voice channel!").queue();
            }
        }
    }

    @Override
    public String getCategory() {
        return ":loud_sound: Voice";
    }

    @Override
    public String getName() {
        return "join";
    }

    @Override
    public String[] getArgs() {
        return new String[] {};
    }

    @Override
    public String getDescription() {
        return "Make the bot join the voice channel you're in.";
    }
}
