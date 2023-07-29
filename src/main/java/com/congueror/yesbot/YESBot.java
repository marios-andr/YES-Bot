package com.congueror.yesbot;

import com.congueror.yesbot.command.AbstractCommand;
import com.congueror.yesbot.command.Command;
import com.congueror.yesbot.command.chess.ChessBoardDecor;
import com.congueror.yesbot.command.chess.ChessPieceDecor;
import com.congueror.yesbot.mongodb.Mongo;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;
import java.util.Set;

public class YESBot {

    public static void main(String[] args) {
        try {
            Constants.init();

            setupSystemTray();

            Mongo.initialize();
            Reddit.initialize();

            JDA jda = createJDA();
            setupCommands(jda, args);

            var guilds = jda.getGuilds();

            WebInterface.initialize(jda, guilds);

            TaskScheduler.initialize(jda);

        } catch (Exception e) {
            Constants.LOG.error("There was an error initializing the application", e);
            System.exit(-1);
        }
    }

    public static JDA createJDA() throws InterruptedException {
        JDA jda = JDABuilder.createDefault(Constants.getSettings().token(),
                        GatewayIntent.GUILD_MESSAGES,
                        GatewayIntent.MESSAGE_CONTENT,
                        GatewayIntent.DIRECT_MESSAGES,
                        GatewayIntent.GUILD_VOICE_STATES,
                        GatewayIntent.GUILD_EMOJIS_AND_STICKERS,
                        GatewayIntent.GUILD_MESSAGE_REACTIONS,
                        GatewayIntent.GUILD_MEMBERS,
                        GatewayIntent.GUILD_PRESENCES)
                .enableCache(CacheFlag.VOICE_STATE)
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
                Constants.LOG.error("There was a problem instantiating a command: ", e);
            }
        }

        //if (args.length >= 1 && args[0].equals("--createCommands"))
        var guild = Constants.COMMANDS.stream().filter(command -> Objects.equals(command.getScope(), AbstractCommand.Scope.GUILD)).toList();
        var global = Constants.COMMANDS.stream().filter(command -> Objects.equals(command.getScope(), AbstractCommand.Scope.GLOBAL)).toList();

        jda.getGuilds().forEach(g -> {
            var commands = g.updateCommands();
            guild.forEach(c -> commands.addCommands(c.createCommand()));
            commands.queue();
        });

        var commands = jda.updateCommands();
        global.forEach(c -> commands.addCommands(c.createCommand()).queue());
        commands.queue();
    }

    private static void setupShopEntries() {
        int i = 0;
        for (ChessBoardDecor value : ChessBoardDecor.values()) {
            Constants.SHOP_ENTRIES.put(value.idGroup() + i, value);
            i++;
        }
        i = 0;
        for (ChessPieceDecor value : ChessPieceDecor.values()) {
            Constants.SHOP_ENTRIES.put(value.idGroup() + i, value);
            i++;
        }
    }

    private static void setupSystemTray() {
        if (!SystemTray.isSupported())
            return;

        SystemTray t = SystemTray.getSystemTray();
        BufferedImage image = null;

        try {
            //noinspection ConstantConditions
            image = ImageIO.read(YESBot.class.getClassLoader().getResource("web/images/send.png"));
        } catch (IOException e3) {
            e3.printStackTrace();
        }

        TrayIcon ic = new TrayIcon(image);
        ic.setImageAutoSize(true);
        PopupMenu menu = new PopupMenu();

        MenuItem show = new MenuItem("Shutdown");
        show.addActionListener(e1 -> {
            Constants.LOG.info("System was manually shutdown.");

            System.exit(0);
        });

        menu.add(show);
        ic.setPopupMenu(menu);
        try {
            t.add(ic);
        } catch (AWTException ex) {
            throw new RuntimeException(ex);
        }
    }
}