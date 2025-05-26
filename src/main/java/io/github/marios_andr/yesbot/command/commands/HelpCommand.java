package io.github.marios_andr.yesbot.command.commands;

import io.github.marios_andr.yesbot.Constants;
import io.github.marios_andr.yesbot.command.AbstractCommand;
import io.github.marios_andr.yesbot.command.Command;
import io.github.marios_andr.yesbot.util.ListMap;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.ArrayList;

@Command
public class HelpCommand extends AbstractCommand {

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        String[] help = getInput(event.getMessage());
        if (check(help)) {
            Message reference = event.getMessage();
            EmbedBuilder embed = new EmbedBuilder();
            if (help.length == 1) {
                ArrayList<AbstractCommand> commands = new ArrayList<>(Constants.COMMANDS);

                embed.setTitle("**__Commands__**");
                embed.setColor(Color.RED);

                ListMap<Category, String> fields = new ListMap<>();
                for (AbstractCommand command : commands) {
                    fields.addEntry(command.getCategory(), command.getCommandAndArgs());
                }
                fields.forEach((s, strings) -> {
                    String v = StringUtils.join(strings, "\n");
                    embed.addField(s.txt, v, true);
                });

                event.getChannel().sendMessageEmbeds(embed.build()).setMessageReference(reference).queue();
            } else if (help.length == 2) {
                AbstractCommand command = AbstractCommand.getCommand(help[1]);
                if (command != null) {
                    String desc = command.getCommandDescription();
                    embed.setTitle("**" + command.getCommandAndArgs() + "**");
                    embed.setColor(Color.RED);
                    embed.setDescription(desc);
                    event.getChannel().sendMessageEmbeds(embed.build()).queue();
                } else {
                    event.getChannel().sendMessage("Can't recognize command.").queue();
                }
            } else {
                event.getChannel().sendMessage("I don't know what that means.").queue();
            }
        }
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        var option = event.getOption("command");

        EmbedBuilder embed = new EmbedBuilder();
        if (option != null) {
            AbstractCommand command = AbstractCommand.getCommand(option.getAsString());
            if (command != null) {
                String desc = command.getCommandDescription();
                embed.setTitle("**" + command.getCommandAndArgs() + "**");
                embed.setColor(Color.RED);
                embed.setDescription(desc);
                event.getHook().sendMessageEmbeds(embed.build()).queue();
            } else {
                event.getHook().sendMessage("Can't recognize command.").queue();
            }
        } else {
            embed.setTitle("**__Commands__**");
            embed.setColor(Color.RED);

            ListMap<Category, String> fields = new ListMap<>();
            for (AbstractCommand command : Constants.COMMANDS) {
                fields.addEntry(command.getCategory(), command.getCommandAndArgs());
            }
            fields.forEach((s, strings) -> {
                String v = StringUtils.join(strings, "\n");
                embed.addField(s.txt, v, true);
            });

            event.getHook().sendMessageEmbeds(embed.build()).queue();
        }
    }

    @Override
    public String getName() {
        return "help";
    }

    @Override
    public OptionData[] getArgs() {
        return new OptionData[] {
                new OptionData(OptionType.STRING, "command", "The command you want to view.", false)
        };
    }

    @Override
    public String getCommandDescription() {
        return "Get all commands or alternatively put a command of your choice after 'help' to get a description of that command.";
    }
}