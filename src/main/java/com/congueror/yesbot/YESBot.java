package com.congueror.yesbot;

import com.congueror.yesbot.command.AbstractCommand;
import com.congueror.yesbot.command.Command;
import com.congueror.yesbot.mongodb.Mongo;
import com.congueror.yesbot.window.CustomPrintStream;
import javassist.*;
import javassist.bytecode.LocalVariableAttribute;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.apache.commons.io.IOUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.event.Level;
import org.slf4j.simple.SimpleLoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;

public class YESBot {

    public static final org.slf4j.Logger LOG = LoggerFactory.getLogger(YESBot.class);

    public static void main(String[] args) {
        try {
            //initializeASM();
            test();

            Mongo.initialize();
            RedditUser.initialize();

            JDA jda = createJDA();
            setupCommands(jda, args);

            var guilds = jda.getGuilds();

            WebInterface.initialize(jda, guilds);

            //SetupWindow.setup(jda, guilds);

            MessageScheduler.initialize(jda);

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
                var commands = g.updateCommands();
                guild.forEach(c -> commands.addCommands(c.createCommand()).queue());
            });

            var commands = jda.updateCommands();
            global.forEach(c -> {
                commands.addCommands(c.createCommand()).queue();
            });
        }
    }


    private static void test() {
        System.setOut(new CustomPrintStream(System.out, s -> test2()));
    }

    public static void test2() {
        int ha = 1;
        ha = 2;
    }

    private static void initializeASM() throws IOException, NotFoundException, CannotCompileException {

        ClassPool pool = new ClassPool();
        pool.appendSystemPath();
        pool.importPackage("org.slf4j");
        pool.importPackage("org.slf4j.helpers");
        pool.importPackage("org.slf4j.simple");
        pool.importPackage("org.slf4j.event");
        pool.importPackage("java.util");
        pool.importPackage("com.congueror.yesbot.YESBot");
        CtClass clazz = pool.get("org.slf4j.simple.SimpleLogger");
        CtMethod method = clazz.getDeclaredMethod("innerHandleNormalizedLoggingCall");

        var info = method.getMethodInfo();
        LocalVariableAttribute table = (LocalVariableAttribute) info.getCodeAttribute().getAttribute(LocalVariableAttribute.tag);

        int frame = table.nameIndex(2);
        String a = info.getConstPool().getUtf8Info(frame);


        method.setBody(
                """
                {
                StringBuilder buf = new StringBuilder(32);
                if (CONFIG_PARAMS.showDateTime) {
                    if (CONFIG_PARAMS.dateFormatter != null) {
                        buf.append(this.getFormattedDate());
                        buf.append(SP);
                    } else {
                        buf.append(System.currentTimeMillis() - START_TIME);
                        buf.append(SP);
                    }
                }

                if (CONFIG_PARAMS.showThreadName) {
                    buf.append('[');
                    buf.append(Thread.currentThread().getName());
                    buf.append("] ");
                }

                if (CONFIG_PARAMS.showThreadId) {
                    buf.append("tid=");
                    buf.append(Thread.currentThread().getId());
                    buf.append(SP);
                }

                if (CONFIG_PARAMS.levelInBrackets) {
                    buf.append('[');
                }

                String levelStr = $1.name();
                buf.append(levelStr);
                if (CONFIG_PARAMS.levelInBrackets) {
                    buf.append(']');
                }

                buf.append(SP);
                if (CONFIG_PARAMS.showShortLogName) {
                    if (this.shortLogName == null) {
                        this.shortLogName = this.computeShortName();
                    }

                    buf.append(String.valueOf(this.shortLogName)).append(" - ");
                } else if (CONFIG_PARAMS.showLogName) {
                    buf.append(String.valueOf(this.name)).append(" - ");
                }

                if ($2 != null) {
                    buf.append(SP);
                    Iterator var8 = $2.iterator();

                    while(var8.hasNext()) {
                        Marker marker = (Marker)var8.next();
                        buf.append(marker.getName()).append(SP);
                    }
                }

                String formattedMessage = MessageFormatter.basicArrayFormat($3, $4);
                buf.append(formattedMessage);
                com.congueror.yesbot.YESBot.test2();
                this.write(buf, $5);
                }
                """.replace("\n", ""));
        System.out.println("A");
        clazz.toClass(SimpleLoggerFactory.class);
    }

    private void innerHandleNormalizedLoggingCall(Level level, List<Marker> markers, String messagePattern, Object[] arguments, Throwable t) {
        /*
        StringBuilder buf = new StringBuilder(32);
        if (CONFIG_PARAMS.showDateTime) {
            if (CONFIG_PARAMS.dateFormatter != null) {
                buf.append(this.getFormattedDate());
                buf.append(SP);
            } else {
                buf.append(System.currentTimeMillis() - START_TIME);
                buf.append(SP);
            }
        }

        if (CONFIG_PARAMS.showThreadName) {
            buf.append('[');
            buf.append(Thread.currentThread().getName());
            buf.append("] ");
        }

        if (CONFIG_PARAMS.showThreadId) {
            buf.append("tid=");
            buf.append(Thread.currentThread().getId());
            buf.append(SP);
        }

        if (CONFIG_PARAMS.levelInBrackets) {
            buf.append('[');
        }

        String levelStr = level.name();
        buf.append(levelStr);
        if (CONFIG_PARAMS.levelInBrackets) {
            buf.append(']');
        }

        buf.append(SP);
        if (CONFIG_PARAMS.showShortLogName) {
            if (this.shortLogName == null) {
                this.shortLogName = this.computeShortName();
            }

            buf.append(String.valueOf(this.shortLogName)).append(" - ");
        } else if (CONFIG_PARAMS.showLogName) {
            buf.append(String.valueOf(this.name)).append(" - ");
        }

        if (markers != null) {
            buf.append(SP);
            Iterator var8 = markers.iterator();

            while(var8.hasNext()) {
                Marker marker = (Marker)var8.next();
                buf.append(marker.getName()).append(SP);
            }
        }

        String formattedMessage = MessageFormatter.basicArrayFormat(messagePattern, arguments);
        buf.append(formattedMessage);
        this.write(buf, t);*/
    }
}