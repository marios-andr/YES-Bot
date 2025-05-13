package io.github.marios_andr.yesbot;

import io.github.marios_andr.yesbot.util.LogFile;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import io.javalin.websocket.WsContext;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.managers.AudioManager;

import java.io.IOException;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static io.github.marios_andr.yesbot.Constants.LOG;

public final class WebInterface {

    private WebInterface() {
    }

    private static Javalin APP;
    private static JDA JDA;
    private static List<Guild> GUILDS;
    private static final List<WsContext> CONTEXTS = new ArrayList<>();

    public static final Runnable PERIODIC_PING = () -> {
        for (var iter = CONTEXTS.iterator(); iter.hasNext(); ) {
            WsContext ctx = iter.next();
            if (!ctx.session.isOpen()) {
                iter.remove();
                continue;
            }
            ctx.sendPing();
        }
    };

    private static final Map<String, Message> REQUESTS = new HashMap<>() {{
            put("console_msgs", WebInterface::consoleMsgs);
            put("log_files", WebInterface::logFilesMsg);
            put("guilds", WebInterface::guildsMsg);
            put("channels", WebInterface::channelsMsg);
            put("send_msg", WebInterface::sendMsg);
            put("channel_users", WebInterface::channelUsersMsg);
            put("log_file_select", WebInterface::logFileSelectMsg);
            put("voice_join", WebInterface::voiceJoinMsg);
            put("voice_leave", WebInterface::voiceLeaveMsg);
            put("voice_mute_all", WebInterface::voiceMuteAllMsg);
            put("voice_unmute_all", WebInterface::voiceUnmuteAllMsg);
            put("lock", WebInterface::lockMsg);
            put("shutdown", WebInterface::shutdownMsg);
            put("refresh", WebInterface::refreshMsg);
    }};

    public static void initialize(JDA jda, List<Guild> guilds) {
        JDA = jda;
        GUILDS = guilds;
        APP = Javalin.create(config -> {
                    config.staticFiles.add("/web", Location.CLASSPATH);
                })
                .start(7070);


        APP.ws("/bot", ws -> {
            ws.onConnect(ctx -> {
                if (!Constants.checkCredentials(ctx.queryParam("name"), ctx.queryParam("password"))) {
                    ctx.closeSession();
                } else {
                    Constants.LOG.info("Successfully connected to client.");
                    ctx.session.setIdleTimeout(Duration.of(1, ChronoUnit.MINUTES));
                    CONTEXTS.add(ctx);
                    ctx.send(Map.of("type", "initialize"));
                }
            });
            ws.onClose(ctx -> {
                Constants.LOG.info("Connection was closed.");
            });
            ws.onMessage(ctx -> {
                JsonObject msg = Constants.GSON.fromJson(ctx.message(), JsonObject.class);
                if (ctx.session.isOpen())
                    REQUESTS.get(msg.get("type").getAsString()).execute(ctx, msg);
            });
        });

        APP.get("/calculator/{send}", ctx -> {
            if (!ctx.pathParam("send").equals("something") || ctx.queryParam("in") == null)
                return;


        });
    }

    public static void sendToConsole(String out) {
        for (var iter = CONTEXTS.iterator(); iter.hasNext(); ) {
            WsContext ctx = iter.next();
            if (!ctx.session.isOpen()) {
                iter.remove();
                continue;
            }

            JsonObject json = createMessage("console");
            json.addProperty("message", out);
            ctx.send(Constants.GSON.toJson(json));
        }
    }

    private static Stream<Guild> getGuild(String guildId) {
        return GUILDS.stream().filter(guild -> guild.getId().equals(guildId));
    }

    private static Stream<TextChannel> getTextChannel(String guildId, String channelId) {
        return getGuild(guildId).flatMap(guild -> guild.getTextChannels().stream()).filter(guildChannel -> guildChannel.getId().equals(channelId));
    }

    private static Stream<VoiceChannel> getVoiceChannel(String guildId, String channelId) {
        return getGuild(guildId).flatMap(guild -> guild.getVoiceChannels().stream()).filter(voiceChannel -> voiceChannel.getId().equals(channelId));
    }

    private static JsonObject createMessage(String type) {
        JsonObject obj = new JsonObject();
        obj.addProperty("type", type);
        return obj;
    }

    private static void consoleMsgs(WsContext ctx, JsonObject obj) {
        JsonObject json = createMessage("console");

        try {
            json.addProperty("message", Constants.LOG_FILE.read());
        } catch (IOException ignored) {

        }

        ctx.send(Constants.GSON.toJson(json));
    }

    private static void logFilesMsg(WsContext ctx, JsonObject obj) {
        JsonObject json = createMessage("log_files");

        JsonArray array = new JsonArray();
        for (String s : LogFile.getAll()) {
            if (Constants.LOG_FILE.getName().equals(s))
                array.add(s + "(current)");
            else
                array.add(s);
        }
        json.add("files", array);

        ctx.send(Constants.GSON.toJson(json));
    }

    private static void guildsMsg(WsContext ctx, JsonObject obj) {
        JsonObject json = createMessage("guilds");

        JsonArray guilds = new JsonArray();
        String def = "https://assets-global.website-files.com/6257adef93867e50d84d30e2/636e0a6ca814282eca7172c6_icon_clyde_white_RGB.svg";
        for (Guild guild : GUILDS) {
            JsonObject o = new JsonObject();
            o.addProperty("id", guild.getId());
            o.addProperty("name", guild.getName());
            o.addProperty("image", guild.getIconUrl() == null ? def : guild.getIconUrl());
            guilds.add(o);
        }
        json.add("guilds", guilds);

        ctx.send(Constants.GSON.toJson(json));
    }

    private static void channelsMsg(WsContext ctx, JsonObject obj) {
        JsonObject json = createMessage("channels");

        JsonArray channels = new JsonArray();
        String guildId = obj.get("guildId").getAsString();
        for (TextChannel channel : getGuild(guildId)
                .flatMap(guild -> guild.getTextChannels().stream())
                .filter(GuildMessageChannel::canTalk).toList()) {

            JsonObject o = new JsonObject();
            o.addProperty("id", channel.getId());
            o.addProperty("name", channel.getName());
            o.addProperty("type", "text");
            channels.add(o);
        }
        for (VoiceChannel channel : getGuild(guildId)
                .flatMap(guild -> guild.getVoiceChannels().stream())
                .filter(GuildMessageChannel::canTalk).toList()) {

            JsonObject o = new JsonObject();
            o.addProperty("id", channel.getId());
            o.addProperty("name", channel.getName());
            o.addProperty("type", "voice");
            channels.add(o);
        }
        json.add("channels", channels);

        ctx.send(Constants.GSON.toJson(json));
    }

    private static void sendMsg(WsContext ctx, JsonObject obj) {
        String msg = obj.get("msg").getAsString();
        String channelId = obj.get("channelId").getAsString();
        String guildId = obj.get("guildId").getAsString();

        getGuild(guildId).flatMap(guild -> guild.getTextChannels().stream()).filter(textChannel -> textChannel.getId().equals(channelId)).forEach(textChannel -> {
            textChannel.sendMessage(msg).queue();
        });
    }

    private static void channelUsersMsg(WsContext ctx, JsonObject obj) {
        JsonObject json = createMessage("channel_users");

        JsonArray users = new JsonArray();
        String channelId = obj.get("channelId").getAsString();
        String guildId = obj.get("guildId").getAsString();
        getTextChannel(guildId, channelId).flatMap(guild -> guild.getMembers().stream()).forEach(member -> {
            JsonObject o = new JsonObject();
            o.addProperty("name", member.getUser().getName());
            o.addProperty("mention", member.getAsMention());
            users.add(o);
        });
        json.add("users", users);

        ctx.send(Constants.GSON.toJson(json));
    }

    private static void logFileSelectMsg(WsContext ctx, JsonObject obj) {
        String name = obj.get("name").getAsString();
        if (name.endsWith("(current)"))
            name = name.replace("(current)", "");


        JsonObject json = createMessage("log_file_select");

        try {
            json.addProperty("message", LogFile.read(name));
        } catch (IOException ignored) {

        }

        ctx.send(Constants.GSON.toJson(json));
    }

    private static void voiceJoinMsg(WsContext ctx, JsonObject obj) {
        String guildId = obj.get("guildId").getAsString();
        String channelId = obj.get("channelId").getAsString();

        try {
            Guild guild = getGuild(guildId).findFirst().get();
            getVoiceChannel(guildId, channelId).forEach(voiceChannel -> {
                AudioManager audioManager = guild.getAudioManager();
                audioManager.openAudioConnection(voiceChannel);
            });
        } catch (NullPointerException e) {
            Constants.LOG.info("Wrong guildId was sent through voice_join message!");
        }
    }

    private static void voiceLeaveMsg(WsContext ctx, JsonObject obj) {
        String guildId = obj.get("guildId").getAsString();
        String channelId = obj.get("channelId").getAsString();

        try {
            Guild guild = getGuild(guildId).findFirst().get();
            getVoiceChannel(guildId, channelId).forEach(voiceChannel -> {
                AudioManager audioManager = guild.getAudioManager();
                audioManager.closeAudioConnection();
            });
        } catch (NullPointerException e) {
            Constants.LOG.info("Wrong guildId was sent through voice_leave message!");
        }
    }

    private static void voiceMuteAllMsg(WsContext ctx, JsonObject obj) {
        String guildId = obj.get("guildId").getAsString();
        String channelId = obj.get("channelId").getAsString();

        try {
            getVoiceChannel(guildId, channelId).flatMap(voiceChannel -> voiceChannel.getMembers().stream()).forEach(member -> {
                if (!member.getUser().isBot())
                    member.mute(true).queue();
            });
        } catch (NullPointerException e) {
            Constants.LOG.info("Wrong guildId was sent through voice_mute_all message!");
        }
    }

    private static void voiceUnmuteAllMsg(WsContext ctx, JsonObject obj) {
        String guildId = obj.get("guildId").getAsString();
        String channelId = obj.get("channelId").getAsString();

        try {
            getVoiceChannel(guildId, channelId).flatMap(voiceChannel -> voiceChannel.getMembers().stream()).forEach(member -> {
                if (!member.getUser().isBot())
                    member.mute(false).queue();
            });
        } catch (NullPointerException e) {
            Constants.LOG.info("Wrong guildId was sent through voice_unmute_all message!");
        }
    }

    private static void lockMsg(WsContext ctx, JsonObject obj) {
        Constants.LOCKED = obj.get("lock").getAsBoolean();
    }

    private static void shutdownMsg(WsContext ctx, JsonObject obj) {
        if (JDA == null || JDA.getStatus().equals(net.dv8tion.jda.api.JDA.Status.SHUTDOWN)) {
            try {
                LOG.info("An administrator is manually booting up the bot...");
                JDA = YESBot.createJDA();
                TaskScheduler.refresh(JDA);
            } catch (Exception e) {
                Constants.LOG.error("Something went wrong while starting the JDA", e);
            }
        } else {
            LOG.info("An administrator is manually shutting down the bot...");
            JDA.shutdown();
            TaskScheduler.refresh(JDA);
            JDA = null;
        }
    }

    private static void refreshMsg(WsContext ctx, JsonObject obj) {
        LOG.info("An administrator is manually refreshing schedules...");
        TaskScheduler.refresh(JDA);
    }

    @FunctionalInterface
    private interface Message {
        void execute(WsContext ctx, JsonObject data);
    }
}
