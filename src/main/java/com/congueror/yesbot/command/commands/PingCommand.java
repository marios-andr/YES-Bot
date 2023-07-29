package com.congueror.yesbot.command.commands;

import com.congueror.yesbot.command.AbstractCommand;
import com.congueror.yesbot.command.Command;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;

@Command
public class PingCommand extends AbstractCommand {
    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        String[] ping = getInput(event.getMessage());
        if(check(ping)) {
            event.getJDA().getRestPing().queue(ping1 -> event.getChannel().sendMessageFormat("Reset ping: %sms\nWS ping: %sms", ping1, event.getJDA().getGatewayPing()).queue());
        }
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        event.getJDA().getRestPing().queue(ping1 -> event.getChannel().sendMessageFormat("Reset ping: %sms\nWS ping: %sms", ping1, event.getJDA().getGatewayPing()).queue());
    }

    @Override
    public String getName() {
        return "ping";
    }

    @Override
    public OptionData[] getArgs() {
        return new OptionData[] {};
    }

    @Override
    public String getCommandDescription() {
        return "The ping of the bot.";
    }
}
