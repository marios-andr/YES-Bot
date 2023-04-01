package com.congueror.yesbot;

import com.congueror.yesbot.command.AbstractCommand;
import com.congueror.yesbot.command.Command;
import com.congueror.yesbot.mongodb.Mongo;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Set;

public class YESBot {

    public static final org.slf4j.Logger LOG = LoggerFactory.getLogger(YESBot.class);

    public static void main(String[] args) {
        try {
            Mongo.initialize();
            RedditUser.initialize();

            JDA jda = createJDA();
            setupCommands(jda, args);

            var guilds = jda.getGuilds();

            WebInterface.initialize(jda, guilds);

            //SetupWindow.setup(jda, guilds);

            TaskScheduler.initialize(jda);

        } catch (Exception e) {
            LOG.error("There was an error initializing the application", e);
            System.exit(-1);
        }
    }

    public static JDA createJDA() throws InterruptedException {
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
        return jda;
    }

    private static void setupCommands(JDA jda, String[] args) {
        Reflections reflections = new Reflections("com.congueror.yesbot.command.commands");

        Set<Class<?>> annotated = reflections.get(Scanners.TypesAnnotated.of(Scanners.TypesAnnotated.with(Command.class)).asClass());
        for (Class<?> clazz : annotated) {
            try {
                AbstractCommand command = (AbstractCommand) clazz.newInstance();
                Constants.COMMANDS.add(command);
            } catch (Exception e) {
                LOG.error("There was a problem instantiating a command: ", e);
            }
        }

        if (args.length >= 1 && args[0].equals("--createCommands")) {
            var guild = Constants.COMMANDS.stream().filter(command -> Objects.equals(command.getScope(), AbstractCommand.Scope.GUILD)).toList();
            var global = Constants.COMMANDS.stream().filter(command -> Objects.equals(command.getScope(), AbstractCommand.Scope.GLOBAL)).toList();

            jda.getGuilds().forEach(g -> {
                //g.updateCommands().queue();

                var commands = g.updateCommands();
                guild.forEach(c -> commands.addCommands(c.createCommand()));
                commands.queue();
            });

            //jda.updateCommands().queue();
            var commands = jda.updateCommands();
            global.forEach(c -> {
                commands.addCommands(c.createCommand()).queue();
            });
            commands.queue();
        }
    }

    public static void onLogMessage(String out) {
        WebInterface.sendToConsole(out);
    }
}