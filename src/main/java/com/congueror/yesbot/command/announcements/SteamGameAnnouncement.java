package com.congueror.yesbot.command.announcements;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.util.ArrayList;
import java.util.List;

public record SteamGameAnnouncement() implements Announcement {
    @Override
    public MessageEmbed buildEmbed() {
        return new EmbedBuilder().build();
    }

    public static List<Announcement> parse(String type) {

        return new ArrayList<>();
    }
}
