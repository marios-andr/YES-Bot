package com.congueror.yesbot;

import com.congueror.yesbot.command.Command;
import com.congueror.yesbot.command.shop.Shop;
import com.congueror.yesbot.window.SetupWindow;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;

import javax.annotation.Nullable;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Set;

public class YESBot {

    public static final String SNOWFLAKE_ID = "727830791664697395";

    public static final ArrayList<Shop.ShopEntry> SHOP_ENTRIES = new ArrayList<>();

    public static void main(String[] args) {
        MongoUser.initialize();
        RedditUser.initialize();

        Reflections reflections = new Reflections("com.congueror.yesbot.command.commands");

        Set<Class<?>> annotated = reflections.get(Scanners.SubTypes.of(Scanners.SubTypes.with(Command.class)).asClass());
        for (Class<?> clazz : annotated) {
            try {
                Command command = (Command) clazz.newInstance();
                BotListenerAdapter.COMMANDS.add(command);
            } catch (Exception e) {
                System.out.println("There was a problem instantiating a command.");
            }
        }

        try {
            JDA jda = JDABuilder.createDefault(Config.get("token"),
                            GatewayIntent.GUILD_MESSAGES,
                            GatewayIntent.MESSAGE_CONTENT,
                            GatewayIntent.DIRECT_MESSAGES,
                            GatewayIntent.GUILD_VOICE_STATES,
                            GatewayIntent.GUILD_EMOJIS_AND_STICKERS,
                            GatewayIntent.GUILD_MESSAGE_REACTIONS,
                            GatewayIntent.GUILD_MEMBERS,
                            GatewayIntent.GUILD_PRESENCES)
                    .enableCache(CacheFlag.VOICE_STATE, CacheFlag.EMOJI, CacheFlag.MEMBER_OVERRIDES)
                    .setMemberCachePolicy(MemberCachePolicy.ALL)
                    .addEventListeners(new BotListenerAdapter())
                    .setActivity(Activity.watching("Star Wars: Episode III - Revenge of the Sith"))
                    .build();
            jda.awaitReady();

            SetupWindow.setup(jda.getGuilds());

            if (args.length >= 1 && args[0].equals("--createCommands")) {
                for (Command a : BotListenerAdapter.COMMANDS) {
                    jda.upsertCommand(a.getName(), "test").complete();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static JsonObject getJson(String url) {
        try (InputStream input = new URL(url).openStream()) {
            InputStreamReader reader = new InputStreamReader(input, StandardCharsets.UTF_8);
            var json = JsonParser.parseReader(reader);
            return json.getAsJsonObject();
        } catch (Exception e) {
            return null;
        }
    }

    @Nullable
    public static String optionalString(JsonElement element) {
        return element == null ? null : element.getAsString();
    }
}