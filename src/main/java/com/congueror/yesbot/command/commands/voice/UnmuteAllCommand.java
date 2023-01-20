package com.congueror.yesbot.command.commands.voice;

import com.congueror.yesbot.command.Command;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class UnmuteAllCommand implements Command {

    @Override
    public void handle(MessageReceivedEvent event) {
        String[] mute_all = getInput(event);
        if (check(mute_all)) {
            var eventMember = event.getMember();
            if (eventMember != null && eventMember.hasPermission(Permission.VOICE_MUTE_OTHERS)) {
                var channel = joinVC(event);
                if (channel != null) {
                    channel.getMembers().forEach(member -> {
                        if (!member.getUser().isBot())
                            member.mute(false).queue();
                    });
                }
            } else {
                event.getChannel().sendMessage("You do not have permission to access this command.").queue();
            }
        }
    }

    @Override
    public String getCategory() {
        return VOICE;
    }

    @Override
    public String getName() {
        return "unmute_all";
    }

    @Override
    public String[] getArgs() {
        return new String[] {};
    }

    @Override
    public String getDescription() {
        return "Unmute all people in the voice channel.";
    }
}