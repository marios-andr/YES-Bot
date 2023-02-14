package com.congueror.yesbot;

import com.congueror.yesbot.command.Command;
import com.congueror.yesbot.mongodb.Mongo;
import com.congueror.yesbot.window.SetupWindow;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;

import java.util.Set;

public class YESBot {

    public static void main(String[] args) {
        Mongo.initialize();
        RedditUser.initialize();

        Reflections reflections = new Reflections("com.congueror.yesbot.command.commands");

        Set<Class<?>> annotated = reflections.get(Scanners.SubTypes.of(Scanners.SubTypes.with(Command.class)).asClass());
        for (Class<?> clazz : annotated) {
            try {
                Command command = (Command) clazz.newInstance();
                Constants.COMMANDS.add(command);
            } catch (Exception e) {
                System.out.println("There was a problem instantiating a command.");
            }
        }

        try {
            JDA jda = JDABuilder.createDefault(Constants.getEnv("token"),
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


            SetupWindow.setup(jda, jda.getGuilds());

            MessageScheduler.initialize(jda);

            if (args.length >= 1 && args[0].equals("--createCommands")) {
                for (Command a : Constants.COMMANDS) {
                    jda.upsertCommand(a.getName(), "test").complete();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}