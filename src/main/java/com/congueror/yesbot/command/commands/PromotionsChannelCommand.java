package com.congueror.yesbot.command.commands;

import com.congueror.yesbot.command.Command;
import com.congueror.yesbot.mongodb.Mongo;
import com.congueror.yesbot.command.AbstractCommand;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;

@Command
public class PromotionsChannelCommand extends AbstractCommand {
    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        String[] in = getInput(event.getMessage());
        if (check(in) && event.getMember().hasPermission(Permission.MANAGE_WEBHOOKS)) {
            Channel channel = event.getMessage().getMentions().getChannels().get(0);
            if (channel instanceof TextChannel && ((TextChannel) channel).canTalk()) {
                Mongo.setPromotionsChannel(event.getGuild().getId(), channel.getIdLong());
                event.getChannel().sendMessage("Promotions Channel set.").setMessageReference(event.getMessage()).queue();
            } else {
                event.getChannel().sendMessage("Cannot set that channel as promotions channel.").setMessageReference(event.getMessage()).queue();
            }
        }
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        Member m = event.getMember();
        if (m.hasPermission(Permission.MANAGE_WEBHOOKS)) {
            var op = event.getOption("channel");

            if (op == null) {
                event.getHook().sendMessage("Cannot set that channel as promotions channel.").setEphemeral(true).queue();
                return;
            }

            Channel channel = op.getAsChannel();
            if (channel instanceof TextChannel && ((TextChannel) channel).canTalk()) {
                Mongo.setPromotionsChannel(event.getGuild().getId(), channel.getIdLong());
                event.getHook().sendMessage("Promotions Channel set.").queue();
            } else {
                event.getHook().sendMessage("Cannot set that channel as promotions channel.").setEphemeral(true).queue();
            }
        }
    }

    @Override
    public String getName() {
        return "set_promotions_channel";
    }

    @Override
    public OptionData[] getArgs() {
        return new OptionData[] {
                new OptionData(OptionType.CHANNEL, "channel", "The channel that the promotions will be sent to.", true)
        };
    }

    @Override
    public String getCommandDescription() {
        return "Set the promotions announcement channel.";
    }

    @Override
    public String getCategory() {
        return AbstractCommand.UTILITY;
    }
}
