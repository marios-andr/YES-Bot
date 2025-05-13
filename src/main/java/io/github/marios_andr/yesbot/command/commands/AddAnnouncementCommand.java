package io.github.marios_andr.yesbot.command.commands;

import io.github.marios_andr.yesbot.command.AbstractCommand;
import io.github.marios_andr.yesbot.command.Command;
import io.github.marios_andr.yesbot.command.announcements.Announcement;
import io.github.marios_andr.yesbot.database.DatabaseHandler;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;

@Command
public class AddAnnouncementCommand extends AbstractCommand {

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        Member m = event.getMember();
        if (m.hasPermission(Permission.MANAGE_WEBHOOKS)) {
            var op = event.getOption("announcement_type");
            var op1 = event.getOption("announcement_channel");

            if (op == null || Announcement.find(op.getAsString()) == null) {
                event.getHook().sendMessage("Invalid announcement type entered.").setEphemeral(true).queue();
                return;
            }
            if (op1 == null || !(op1.getAsChannel() instanceof TextChannel && ((TextChannel) op1.getAsChannel()).canTalk())) {
                event.getHook().sendMessage("Cannot set that channel as promotions channel.").setEphemeral(true).queue();
                return;
            }

            Channel channel = op1.getAsChannel();
            DatabaseHandler.addPromotionsChannel(event.getGuild().getId(), op.getAsString(), channel.getId());
            event.getHook().sendMessage(op.getAsString() + " announcement added to " + channel.getAsMention()).queue();
        }
    }

    @Override
    public String getName() {
        return "add_announcement";
    }

    @Override
    public OptionData[] getArgs() {
        var type = new OptionData(OptionType.STRING, "announcement_type", "Type of the Announcement. Do /help add_announcement to see types.", true);
        for (String s : Announcement.DESCRIPTIONS.keySet()) {
            type.addChoice(s, s);
        }

        return new OptionData[]{
                type,
                new OptionData(OptionType.CHANNEL, "announcement_channel", "The Channel in which the announcement will be announced.", true)
        };
    }

    @Override
    public String getCommandDescription() {
        StringBuilder desc = new StringBuilder("""
                Adds an announcement type in the given channel to be announced whenever an update comes.
                Announcement types include:
                                
                """);
        for (String s : Announcement.DESCRIPTIONS.keySet()) {
            desc.append("* ").append(s).append(": ").append(Announcement.DESCRIPTIONS.get(s)).append("\n");
        }
        return desc.toString();
    }
}
