package com.congueror.yesbot.command.commands;

import com.congueror.yesbot.Constants;
import com.congueror.yesbot.ListMap;
import com.congueror.yesbot.command.Command;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.apache.commons.lang3.StringUtils;

import java.awt.*;
import java.util.ArrayList;

public class HelpCommand implements Command {

    @Override
    public void handle(MessageReceivedEvent event) {
        String[] help = getInput(event);
        if (check(help)) {
            Message reference = event.getMessage();
            EmbedBuilder embed = new EmbedBuilder();
            if (help.length == 1) {
                ArrayList<Command> commands = new ArrayList<>(Constants.COMMANDS);

                embed.setTitle("**__Commands__**");
                embed.setColor(Color.RED);

                ListMap<String, String> fields = new ListMap<>();
                for (Command command : commands) {
                    fields.addEntry(command.getCategory(), command.getCommandAndArgs());
                }
                fields.forEach((s, strings) -> {
                    String v = StringUtils.join(strings, "\n");
                    embed.addField(s, v, true);
                });

                event.getChannel().sendMessageEmbeds(embed.build()).setMessageReference(reference).queue();
            } else if (help.length == 2) {
                Command command = Command.getCommand(help[1]);
                if (command != null) {
                    String desc = command.getDescription();
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
    public String getName() {
        return "help";
    }

    @Override
    public String[] getArgs() {
        return new String[]{"command"};
    }

    @Override
    public String getDescription() {
        ArrayList<String> desc = new ArrayList<>();
        desc.add("Get all commands or alternatively put a command of your choice after 'help' to get a description of that command.");
        desc.add("command: The command you want to view.");
        return StringUtils.join(desc, String.format("%n", ""));
    }
}