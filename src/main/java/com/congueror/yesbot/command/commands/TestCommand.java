package com.congueror.yesbot.command.commands;

import com.congueror.yesbot.command.AbstractCommand;
import com.congueror.yesbot.command.Command;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;

@Command
public class TestCommand extends AbstractCommand {
    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        String[] test = getInput(event.getMessage());
        if (check(test)) {
            //event.getChannel().sendMessage("Fuck you \u0030\ufe0f\u20e3").queue(message -> message.addReaction("\u0030\ufe0f\u20e3").queue());
        }
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {

    }

    @Override
    public String getName() {
        return "test";
    }

    @Override
    public OptionData[] getArgs() {
        return new OptionData[]{};
    }

    @Override
    public String getCommandDescription() {
        return "go away";
    }

    @Override
    public String getCategory() {
        return TESTING;
    }
}
