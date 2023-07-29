package com.congueror.yesbot.command.commands.voice;

import com.congueror.yesbot.command.AbstractCommand;
import com.congueror.yesbot.command.Command;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;

@Command
public class UnmuteAllCommand extends AbstractCommand {

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        String[] mute_all = getInput(event.getMessage());
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
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
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
            event.getHook().sendMessage("You do not have permission to access this command.").setEphemeral(true).queue();
        }
    }

    @Override
    public String getName() {
        return "unmute_all";
    }

    @Override
    public OptionData[] getArgs() {
        return new OptionData[] {};
    }

    @Override
    public String getCommandDescription() {
        return "Unmute all people in the voice channel.";
    }

    @Override
    public String getCategory() {
        return VOICE;
    }
}