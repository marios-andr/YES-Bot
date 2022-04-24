package com.congueror.yesbot;

import com.congueror.yesbot.command.AbstractCommand;
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
        MongoUser.initialize();

        Reflections reflections = new Reflections("com.congueror.yesbot.command.commands");

        Set<Class<?>> annotated = reflections.get(Scanners.SubTypes.of(Scanners.SubTypes.with(AbstractCommand.class)).asClass());
        for (Class<?> clazz : annotated) {
            try {
                AbstractCommand command = (AbstractCommand) clazz.newInstance();
                BotListenerAdapter.COMMANDS.add(command);
            } catch (Exception e) {
                System.out.println("There was a problem instantiating a command.");
            }
        }

        try {
            JDA jda = JDABuilder.createDefault(Config.get("token"),
                            GatewayIntent.GUILD_MESSAGES,
                            GatewayIntent.DIRECT_MESSAGES,
                            GatewayIntent.GUILD_VOICE_STATES,
                            GatewayIntent.GUILD_EMOJIS,
                            GatewayIntent.GUILD_MESSAGE_REACTIONS,
                            GatewayIntent.GUILD_MEMBERS,
                            GatewayIntent.GUILD_PRESENCES)
                    .enableCache(CacheFlag.VOICE_STATE, CacheFlag.EMOTE, CacheFlag.MEMBER_OVERRIDES)
                    .setMemberCachePolicy(MemberCachePolicy.ALL)
                    .addEventListeners(new BotListenerAdapter())
                    .setActivity(Activity.watching("Star Wars: Episode III - Revenge of the Sith"))
                    .build();
            jda.awaitReady();

            if (args.length >= 1 && args[0].equals("createCommands")) {
                for (AbstractCommand a : BotListenerAdapter.COMMANDS) {
                    //TODO: slash commands
                    //jda.upsertCommand(a.getName(), a.getDescription()).complete();
                }
            }

            SetupWindow.setup(jda.getGuilds());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}