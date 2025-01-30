package com.congueror.yesbot.command.commands;

import com.congueror.yesbot.command.Command;
import com.congueror.yesbot.database.DatabaseHandler;
import com.congueror.yesbot.database.Mongo;
import com.congueror.yesbot.command.AbstractCommand;
import com.google.gson.JsonObject;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.bson.Document;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

@Command
public class ProfileCommand extends AbstractCommand {

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        String[] profile = getInput(event.getMessage());
        if (check(profile)) {
            Message reference = event.getMessage();
            User player;
            if (hasMentions(reference)) {
                player = reference.getMentions().getMembers().get(0).getUser();
            } else {
                player = reference.getAuthor();
            }

            EmbedBuilder embed = new EmbedBuilder();
            embed.setTitle("**__" + player.getAsTag() + "__**");
            embed.setThumbnail(player.getEffectiveAvatarUrl());
            embed.setColor(Color.RED);
            JsonObject doc = DatabaseHandler.getUserJson(player.getId());
            for (var a : doc.keySet()) {
                if (a.equals("_id") || a.equals("id"))
                    continue;
                embed.addField(a + ":", doc.get(a).getAsString(), true);
            }
            event.getChannel().sendMessageEmbeds(embed.build()).setMessageReference(reference).queue();
        }
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        var op = event.getOption("target");

        User player;
        if (op == null) {
            player = event.getUser();
        } else {
            player = op.getAsUser();
        }

        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("**__" + player.getAsTag() + "__**");
        embed.setThumbnail(player.getEffectiveAvatarUrl());
        embed.setColor(Color.RED);
        JsonObject doc = DatabaseHandler.getUserJson(player.getId());
        for (var a : doc.keySet()) {
            if (a.equals("_id") || a.equals("id"))
                continue;
            embed.addField(a + ":", doc.get(a).getAsString(), true);
        }
        event.getHook().sendMessageEmbeds(embed.build()).queue();
    }

    @Override
    public String getName() {
        return "profile";
    }

    @Override
    public OptionData[] getArgs() {
        return new OptionData[]{
                new OptionData(OptionType.USER, "target", "Target for this command", false)
        };
    }

    @Override
    public String getCommandDescription() {
        return "Displays relevant information about the player.";
    }
}
