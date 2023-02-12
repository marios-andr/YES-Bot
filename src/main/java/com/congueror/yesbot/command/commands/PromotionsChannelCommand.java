package com.congueror.yesbot.command.commands;

import com.congueror.yesbot.Mongo;
import com.congueror.yesbot.command.Command;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class PromotionsChannelCommand implements Command {
    @Override
    public void handle(MessageReceivedEvent e) {
        String[] in = getInput(e);
        if (check(in) && e.getMember().hasPermission(Permission.MANAGE_WEBHOOKS)) {
            Channel channel = e.getMessage().getMentions().getChannels().get(0);
            if (channel instanceof TextChannel && ((TextChannel) channel).canTalk()) {
                Mongo.setPromotionsChannel(e.getGuild().getId(), channel.getIdLong());
                e.getChannel().sendMessage("Promotions Channel set.").setMessageReference(e.getMessage()).queue();
            } else {
                e.getChannel().sendMessage("Cannot set that channel as promotions channel.").setMessageReference(e.getMessage()).queue();
            }
        }
    }

    @Override
    public String getName() {
        return "setPromotionsChannel";
    }

    @Override
    public String[] getArgs() {
        return new String[] {"channel"};
    }

    @Override
    public String getDescription() {
        return "Set the promotions announcement channel.";
    }

    @Override
    public String getCategory() {
        return Command.UTILITY;
    }
}
