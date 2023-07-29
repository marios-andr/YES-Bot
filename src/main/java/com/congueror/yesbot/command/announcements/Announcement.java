package com.congueror.yesbot.command.announcements;

import com.congueror.yesbot.Constants;
import com.congueror.yesbot.mongodb.Mongo;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.bson.Document;

import java.util.*;

public interface Announcement {
    Map<String, String> DESCRIPTIONS = new HashMap<>() {{
        put("epic_free", "The weekly free games Epic Games releases.");
        put("steam_[ID]", "Any updates the given game releases, ID being the game's steamId. (e.g. steam_1158310 would post the Crusader Kings 3 updates)");
    }};
    Map<String, Class<?>> CLASSES = new HashMap<>() {{
        put("epic_free", EpicStoreAnnouncement.class);
        put("steam_[ID]", SteamGameAnnouncement.class);
    }};

    MessageEmbed buildEmbed();

    static Announcement of(Document doc) {
        try {
            Class<?> clazz = Class.forName(doc.getString("class"));
            Document ann = doc.get("announcement", Document.class);

            if (!Arrays.stream(clazz.getInterfaces()).anyMatch(aClass -> aClass.equals(Announcement.class)))
                return null;

            List<Object> fields = new ArrayList<>();
            Arrays.stream(clazz.getDeclaredFields()).forEach(field -> {
                fields.add(ann.get(field.getName(), field.getType()));
            });
            return (Announcement) clazz.getDeclaredConstructors()[0].newInstance(fields.toArray());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static String find(String t) {
        for (String s : Announcement.DESCRIPTIONS.keySet()) {
            int i = s.indexOf('[');
            if (i == -1 && s.equals(t)) {
                return s;
            } else if (s.substring(0, i - 1).equals(t.substring(0, i - 1))) {
                return s;
            }
        }
        return null;
    }

    static List<Announcement> parse(String type) {
        List<List<Announcement>> lists = new ArrayList<>();
        for (String s : CLASSES.keySet()) {
            Class<?> c = CLASSES.get(s);
            try {
                var m = c.getDeclaredMethod("parse", String.class);
                lists.add((List<Announcement>) m.invoke(null, type));
            } catch (Exception e) {
                Constants.LOG.error("Could not invoke method parse in class" + c.getName(), e);
            }
        }
        return lists.stream().flatMap(Collection::stream).toList();
    }

    static void update(Guild guild, JDA jda) {
        String sf = guild.getId();
        var announcements = Mongo.getAnnouncements(sf);
        for (String s : announcements.keySet()) {
            if (announcements.get(s) == 0)
                continue;
            TextChannel channel = jda.getTextChannelById(announcements.get(s));

            List<Announcement> lastAnn = Mongo.getLastAnnouncements(sf);
            List<Announcement> ann = parse(s);

            List<Announcement> newAnn = new ArrayList<>(ann);
            newAnn.removeAll(lastAnn);
            for (Announcement a : newAnn) {
                Constants.LOG.info("New announcement found in guild {}({}) of type {}", guild.getName(), sf, s);
                channel.sendMessageEmbeds(a.buildEmbed()).queue();
            }

            Mongo.setLastAnnouncements(sf, ann);
        }
    }
}
