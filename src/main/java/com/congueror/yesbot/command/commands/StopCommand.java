package com.congueror.yesbot.command.commands;

import com.congueror.yesbot.Constants;
import com.congueror.yesbot.command.AbstractCommand;
import com.congueror.yesbot.command.Command;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;

@Command
public class StopCommand extends AbstractCommand {
    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (check(getInput(event.getMessage()))) {
            var m = event.getMember();
            if (m.isOwner())
                Constants.STOP = true;
        }
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        var m = event.getMember();
        if (m.isOwner())
            Constants.STOP = true;
    }

    @Override
    public String getName() {
        return "stop";
    }

    @Override
    public OptionData[] getArgs() {
        return new OptionData[]{};
    }

    @Override
    public String getCommandDescription() {
        return "Stop any repeating commands";
    }
}
